package hu.perit.spvitamin.filestorage.impl;

import hu.perit.spvitamin.filestorage.FileInfo;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.common.SftpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.LinkOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SftpFileStorageTest
{
    @Mock
    DefaultSftpSessionFactory sessionFactory;

    SftpFileStorage storage;


    @BeforeEach
    void setUp()
    {
        storage = new SftpFileStorage(sessionFactory);
    }


    @Test
    void newInputStream_readsAllBytesViaSessionRead() throws Exception
    {
        // Arrange
        SftpSession session = mock(SftpSession.class);
        when(sessionFactory.getSession()).thenReturn(session);

        byte[] data = "hello world".getBytes(StandardCharsets.UTF_8);
        doAnswer(invocation -> {
            // args: path, outputStream
            java.io.OutputStream os = invocation.getArgument(1);
            os.write(data);
            return null;
        }).when(session).read(eq("/remote/file.txt"), any());

        // Act
        try (InputStream is = storage.newInputStream(FilePath.of("/remote/file.txt")))
        {
            byte[] read = is.readAllBytes();
            // Assert
            assertArrayEquals(data, read);
        }

        // Session should be closed by try-with-resources in storage
        verify(sessionFactory, times(1)).getSession();
        verify(session, times(1)).read(eq("/remote/file.txt"), any());
        verify(session, times(1)).close();
    }


    @Test
    void delete_removesFile_whenNotDirectory() throws Exception
    {
        // Arrange: Spy storage to bypass isDirectory internals
        SftpFileStorage spyStorage = spy(storage);
        doReturn(false).when(spyStorage).isDirectory(any());

        // Session used for delete
        SftpSession sessionForDelete = mock(SftpSession.class);
        when(sessionFactory.getSession()).thenReturn(sessionForDelete);

        // Act
        spyStorage.delete(FilePath.of("/path/to/file.txt"));

        // Assert
        verify(sessionForDelete).remove("/path/to/file.txt");
        verify(sessionForDelete).close();
    }


    @Test
    void delete_recursivelyDeletesDirectory_andSkipsDotEntries() throws Exception
    {
        // Arrange: spy isDirectory to true
        SftpFileStorage spyStorage = spy(storage);
        doReturn(true).when(spyStorage).isDirectory(any());

        // Session used during recursive deletion
        SftpSession session = mock(SftpSession.class);

        // Build entries: ".", "..", subdir, file
        SftpClient.DirEntry dot = mock(SftpClient.DirEntry.class);
        when(dot.getFilename()).thenReturn(".");

        SftpClient.DirEntry dotdot = mock(SftpClient.DirEntry.class);
        when(dotdot.getFilename()).thenReturn("..");

        SftpClient.DirEntry subdir = mock(SftpClient.DirEntry.class);
        when(subdir.getFilename()).thenReturn("sub");
        SftpClient.Attributes subdirAttrs = mock(SftpClient.Attributes.class);
        when(subdirAttrs.isDirectory()).thenReturn(true);
        when(subdir.getAttributes()).thenReturn(subdirAttrs);

        SftpClient.DirEntry file = mock(SftpClient.DirEntry.class);
        when(file.getFilename()).thenReturn("file.txt");
        SftpClient.Attributes fileAttrs = mock(SftpClient.Attributes.class);
        when(fileAttrs.isDirectory()).thenReturn(false);
        when(file.getAttributes()).thenReturn(fileAttrs);

        // When listing /dir -> returns entries with dot and dotdot, subdir and file
        when(session.list("/dir")).thenReturn(new SftpClient.DirEntry[]{dot, dotdot, subdir, file});
        // When listing /dir/sub -> empty directory
        when(session.list("/dir/sub")).thenReturn(new SftpClient.DirEntry[]{});

        when(sessionFactory.getSession()).thenReturn(session);

        // Act
        spyStorage.delete(FilePath.of("/dir"));

        // Assert - verify order roughly: remove file, rmdir subdir, then rmdir base
        InOrder inOrder = inOrder(session);
        inOrder.verify(session).list("/dir");
        inOrder.verify(session).list("/dir/sub");
        inOrder.verify(session).rmdir("/dir/sub");
        inOrder.verify(session).remove("/dir/file.txt");
        inOrder.verify(session).rmdir("/dir");
        verify(session).close();
    }


    @Test
    void delete_recursivelyIgnoresSftpStatus2_NoSuchFile() throws Exception
    {
        // Arrange: spy isDirectory to true
        SftpFileStorage spyStorage = spy(storage);
        doReturn(true).when(spyStorage).isDirectory(any());

        // Deletion session: list throws SftpException status 2
        SftpSession deletionSession = mock(SftpSession.class);
        when(deletionSession.list("/missingDir")).thenThrow(new SftpException(2, "No such file"));

        when(sessionFactory.getSession()).thenReturn(deletionSession);

        // Act + Assert: should not throw
        assertDoesNotThrow(() -> spyStorage.delete(FilePath.of("/missingDir")));
        verify(deletionSession, never()).rmdir(anyString());
        verify(deletionSession).close();
    }


    @Test
    void list_returnsMappedFileInfos_andSkipsDotEntries() throws Exception
    {
        // Arrange
        SftpSession session = mock(SftpSession.class);

        SftpClient.DirEntry dot = mock(SftpClient.DirEntry.class);
        when(dot.getFilename()).thenReturn(".");

        SftpClient.DirEntry file = mock(SftpClient.DirEntry.class);
        when(file.getFilename()).thenReturn("f.txt");
        SftpClient.Attributes fileAttrs = mock(SftpClient.Attributes.class);
        when(fileAttrs.isDirectory()).thenReturn(false);
        when(fileAttrs.getSize()).thenReturn(123L);
        FileTime mod = FileTime.from(Instant.parse("2020-01-01T00:00:00Z"));
        FileTime crt = FileTime.from(Instant.parse("2019-12-31T00:00:00Z"));
        when(fileAttrs.getModifyTime()).thenReturn(mod);
        when(fileAttrs.getCreateTime()).thenReturn(crt);
        when(file.getAttributes()).thenReturn(fileAttrs);

        when(session.list("/dir")).thenReturn(new SftpClient.DirEntry[]{dot, file});

        when(sessionFactory.getSession()).thenReturn(session);

        // Act
        List<FileInfo> list = storage.list(FilePath.of("/dir"));

        // Assert
        assertEquals(1, list.size());
        FileInfo info = list.get(0);
        assertEquals(FilePath.of("/dir", "f.txt"), info.getPath());
        assertFalse(info.isDirectory());
        assertTrue(info.isRegularFile());
        assertEquals(123L, info.getSize());
        assertEquals(mod.toInstant(), info.getLastModifiedTime());
        assertEquals(crt.toInstant(), info.getCreationTime());
        verify(session).close();
    }


    @Test
    void list_returnsEmptyList_onSftpStatus2() throws Exception
    {
        // Arrange
        SftpSession session = mock(SftpSession.class);
        when(session.list("/missing")).thenThrow(new SftpException(2, "No such file"));
        when(sessionFactory.getSession()).thenReturn(session);

        // Act
        List<FileInfo> list = storage.list(FilePath.of("/missing"));

        // Assert
        assertNotNull(list);
        assertTrue(list.isEmpty());
        verify(session).close();
    }


    @Test
    void stat_mapsAttributesToFileInfo() throws Exception
    {
        // Arrange
        SftpSession session = mock(SftpSession.class);
        SftpClient client = mock(SftpClient.class);
        when(session.getClientInstance()).thenReturn(client);

        SftpClient.Attributes attrs = mock(SftpClient.Attributes.class);
        when(attrs.isDirectory()).thenReturn(false);
        when(attrs.getSize()).thenReturn(42L);
        FileTime mt = FileTime.from(Instant.parse("2021-06-01T12:34:56Z"));
        FileTime ct = FileTime.from(Instant.parse("2021-05-01T00:00:00Z"));
        when(attrs.getModifyTime()).thenReturn(mt);
        when(attrs.getCreateTime()).thenReturn(ct);

        when(client.stat("/a/b.txt")).thenReturn(attrs);
        when(sessionFactory.getSession()).thenReturn(session);

        // Act
        FileInfo info = storage.stat(FilePath.of("/a/b.txt"));

        // Assert
        assertEquals(FilePath.of("/a/b.txt"), info.getPath());
        assertFalse(info.isDirectory());
        assertTrue(info.isRegularFile());
        assertEquals(42L, info.getSize());
        assertEquals(mt.toInstant(), info.getLastModifiedTime());
        assertEquals(ct.toInstant(), info.getCreationTime());
        verify(session).close();
    }


    @Test
    void isDirectory_returnsTrueOrFalse_andHandlesIOException() throws Exception
    {
        // Case 1: true
        SftpSession session1 = mock(SftpSession.class);
        SftpClient client1 = mock(SftpClient.class);
        when(session1.getClientInstance()).thenReturn(client1);
        SftpClient.Attributes dirAttrs = mock(SftpClient.Attributes.class);
        when(dirAttrs.isDirectory()).thenReturn(true);
        when(client1.stat("/dir")).thenReturn(dirAttrs);

        // Case 2: IOException -> false
        SftpSession session2 = mock(SftpSession.class);
        SftpClient client2 = mock(SftpClient.class);
        when(session2.getClientInstance()).thenReturn(client2);
        when(client2.stat("/nope")).thenThrow(new IOException("not found"));

        when(sessionFactory.getSession()).thenReturn(session1, session2);

        assertTrue(storage.isDirectory(FilePath.of("/dir"), new LinkOption[]{}));
        assertFalse(storage.isDirectory(FilePath.of("/nope"), new LinkOption[]{}));

        verify(session1).close();
        verify(session2).close();
    }


    @Test
    void newOutputStream_overwrite_createsParentDirs_andWritesBufferedData() throws Exception
    {
        // Arrange
        SftpSession session = mock(SftpSession.class);
        when(sessionFactory.getSession()).thenReturn(session);

        // Parent directories should be created: /a and /a/b
        when(session.exists("/a")).thenReturn(false);
        when(session.exists("/a/b")).thenReturn(false);
        // File doesn't exist -> no remove()
        when(session.exists("/a/b/c.txt")).thenReturn(false);

        byte[] written = new byte[0];
        final byte[][] holder = new byte[1][];
        doAnswer(inv -> {
            InputStream is = inv.getArgument(0);
            holder[0] = is.readAllBytes();
            return null;
        }).when(session).write(any(InputStream.class), eq("/a/b/c.txt"));

        // Act
        OutputStream os = storage.newOutputStream(FilePath.of("/a/b/c.txt"));
        os.write("DATA".getBytes(StandardCharsets.UTF_8));
        os.close();

        // Assert
        verify(session).mkdir("/a");
        verify(session).mkdir("/a/b");
        verify(session, never()).remove(anyString());
        assertArrayEquals("DATA".getBytes(StandardCharsets.UTF_8), holder[0]);
        verify(session).close();
    }


    @Test
    void newOutputStream_append_readsExisting_andWritesCombined_withoutRemove() throws Exception
    {
        // Arrange
        SftpSession session = mock(SftpSession.class);
        when(sessionFactory.getSession()).thenReturn(session);

        // Parent directories exist
        when(session.exists("/dir")).thenReturn(true);
        when(session.exists("/dir/file.txt")).thenReturn(true);

        byte[] oldData = "OLD".getBytes(StandardCharsets.UTF_8);
        doAnswer(inv -> {
            java.io.OutputStream os = inv.getArgument(1);
            os.write(oldData);
            return null;
        }).when(session).read(eq("/dir/file.txt"), any());

        final byte[][] holder = new byte[1][];
        doAnswer(inv -> {
            InputStream is = inv.getArgument(0);
            holder[0] = is.readAllBytes();
            return null;
        }).when(session).write(any(InputStream.class), eq("/dir/file.txt"));

        // Act
        OutputStream os = storage.newOutputStream(FilePath.of("/dir/file.txt"), StandardOpenOption.APPEND);
        os.write("NEW".getBytes(StandardCharsets.UTF_8));
        os.close();

        // Assert
        assertArrayEquals("OLDNEW".getBytes(StandardCharsets.UTF_8), holder[0]);
        verify(session, never()).remove(anyString());
        verify(session).close();
    }


    @Test
    void createDirectories_recursivelyCreatesMissingSegments() throws Exception
    {
        // Arrange
        SftpSession session = mock(SftpSession.class);
        when(sessionFactory.getSession()).thenReturn(session);

        // None of the path segments exist
        when(session.exists("/base")).thenReturn(false);
        when(session.exists("/base/path")).thenReturn(false);
        when(session.exists("/base/path/to")).thenReturn(false);

        // Act
        FilePath dir = FilePath.of("/base/path/to");
        FilePath result = storage.createDirectories(dir);

        // Assert
        assertEquals(dir, result);
        InOrder inOrder = inOrder(session);
        inOrder.verify(session).exists("/base");
        inOrder.verify(session).mkdir("/base");
        inOrder.verify(session).exists("/base/path");
        inOrder.verify(session).mkdir("/base/path");
        inOrder.verify(session).exists("/base/path/to");
        inOrder.verify(session).mkdir("/base/path/to");
        verify(session).close();
    }


    @Test
    void copy_readsAllBytes_createsParentDirs_andWrites_returningByteCount() throws Exception
    {
        // Arrange
        SftpSession session = mock(SftpSession.class);
        when(sessionFactory.getSession()).thenReturn(session);

        // Parent dirs do not exist
        when(session.exists("/out")).thenReturn(false);
        when(session.exists("/out/dir")).thenReturn(false);

        byte[] src = "1234567890".getBytes(StandardCharsets.UTF_8);
        final byte[][] holder = new byte[1][];
        doAnswer(inv -> {
            InputStream is = inv.getArgument(0);
            holder[0] = is.readAllBytes();
            return null;
        }).when(session).write(any(InputStream.class), eq("/out/dir/file.bin"));

        // Act
        long count = storage.copy(new java.io.ByteArrayInputStream(src), FilePath.of("/out/dir/file.bin"));

        // Assert
        assertEquals(src.length, count);
        assertArrayEquals(src, holder[0]);
        verify(session).mkdir("/out");
        verify(session).mkdir("/out/dir");
        verify(session).close();
    }


    @Test
    void move_createsParentDirs_andRenames() throws Exception
    {
        // Arrange
        SftpSession session = mock(SftpSession.class);
        when(sessionFactory.getSession()).thenReturn(session);

        when(session.exists("/dst")).thenReturn(false);
        when(session.exists("/dst/dir")).thenReturn(false);

        // Act
        FilePath res = storage.move(FilePath.of("/src/a.txt"), FilePath.of("/dst/dir/a.txt"));

        // Assert
        assertEquals(FilePath.of("/dst/dir/a.txt"), res);
        verify(session).mkdir("/dst");
        verify(session).mkdir("/dst/dir");
        verify(session).rename("/src/a.txt", "/dst/dir/a.txt");
        verify(session).close();
    }
}
