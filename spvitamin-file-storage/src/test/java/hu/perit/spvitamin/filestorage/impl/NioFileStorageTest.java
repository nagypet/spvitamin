/*
 * Copyright 2020-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hu.perit.spvitamin.filestorage.impl;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import hu.perit.spvitamin.filestorage.FileInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the NioFileStorage class using jimfs for in-memory file system testing.
 * These tests verify the behavior of all public methods in the NioFileStorage class,
 * including edge cases and normal usage scenarios.
 */
class NioFileStorageTest
{

    private FileSystem fileSystem;
    private NioFileStorage nioFileStorage;
    private Path rootPath;


    @BeforeEach
    void setUp()
    {
        // Create an in-memory file system using jimfs
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        nioFileStorage = new NioFileStorage(fileSystem);
        rootPath = fileSystem.getPath("/test");
        try
        {
            Files.createDirectories(rootPath);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to create test directory", e);
        }
    }


    @AfterEach
    void tearDown() throws IOException
    {
        fileSystem.close();
    }


    @Test
    void testNewInputStream() throws IOException
    {
        // Create a test file
        Path testFile = rootPath.resolve("test.txt");
        Files.write(testFile, "test content".getBytes(StandardCharsets.UTF_8));

        // Test reading from the file
        try (InputStream is = nioFileStorage.newInputStream(FilePath.of(testFile.toString())))
        {
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(content).isEqualTo("test content");
        }

        // Test with non-existent file
        FilePath nonExistentFile = FilePath.of(rootPath.toString(), "non-existent.txt");
        assertThatThrownBy(() -> nioFileStorage.newInputStream(nonExistentFile))
                .isInstanceOf(IOException.class);
    }


    @Test
    void testNewOutputStream() throws IOException
    {
        // Test creating a new file
        FilePath testFile = FilePath.of(rootPath.toString(), "output.txt");
        try (OutputStream os = nioFileStorage.newOutputStream(testFile))
        {
            os.write("output content".getBytes(StandardCharsets.UTF_8));
        }

        // Verify the file was created and has the correct content
        Path path = fileSystem.getPath(testFile.toString());
        assertThat(Files.exists(path)).isTrue();
        assertThat(Files.readString(path)).isEqualTo("output content");

        // Test appending to an existing file
        try (OutputStream os = nioFileStorage.newOutputStream(testFile, StandardOpenOption.APPEND))
        {
            os.write(" appended".getBytes(StandardCharsets.UTF_8));
        }

        // Verify the content was appended
        assertThat(Files.readString(path)).isEqualTo("output content appended");
    }


    @Test
    void testCreateDirectories() throws IOException
    {
        // Test creating a directory structure
        FilePath dirPath = FilePath.of(rootPath.toString(), "dir1/dir2/dir3");
        FilePath createdPath = nioFileStorage.createDirectories(dirPath);

        // Verify the directories were created
        assertThat(createdPath).isEqualTo(dirPath);
        Path path = fileSystem.getPath(dirPath.toString());
        assertThat(Files.exists(path)).isTrue();
        assertThat(Files.isDirectory(path)).isTrue();

        // Test creating an already existing directory
        FilePath existingDir = nioFileStorage.createDirectories(dirPath);
        assertThat(existingDir).isEqualTo(dirPath);
    }


    @Test
    void testCopy() throws IOException
    {
        // Create a source input stream
        byte[] data = "test data for copy".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(data);

        // Test copying to a file
        FilePath targetPath = FilePath.of(rootPath.toString(), "copied.txt");
        long bytesCopied = nioFileStorage.copy(inputStream, targetPath);

        // Verify the file was created and has the correct content
        assertThat(bytesCopied).isEqualTo(data.length);
        Path path = fileSystem.getPath(targetPath.toString());
        assertThat(Files.exists(path)).isTrue();
        assertThat(Files.readString(path)).isEqualTo("test data for copy");
    }


    @Test
    void testMove() throws IOException
    {
        // Create a source file
        Path sourcePath = rootPath.resolve("source.txt");
        Files.write(sourcePath, "move test".getBytes(StandardCharsets.UTF_8));

        // Test moving the file
        FilePath source = FilePath.of(sourcePath.toString());
        FilePath target = FilePath.of(rootPath.toString(), "target.txt");
        FilePath movedPath = nioFileStorage.move(source, target);

        // Verify the file was moved
        assertThat(movedPath).isEqualTo(target);
        Path targetNioPath = fileSystem.getPath(target.toString());
        assertThat(Files.exists(targetNioPath)).isTrue();
        assertThat(Files.readString(targetNioPath)).isEqualTo("move test");
        assertThat(Files.exists(sourcePath)).isFalse();

        // Test moving to a non-existent directory (should create parent directories)
        FilePath newSource = FilePath.of(targetNioPath.toString());
        FilePath newTarget = FilePath.of(rootPath.toString(), "new/path/moved.txt");
        FilePath newMovedPath = nioFileStorage.move(newSource, newTarget);

        // Verify the file was moved and parent directories were created
        assertThat(newMovedPath).isEqualTo(newTarget);
        Path newTargetNioPath = fileSystem.getPath(newTarget.toString());
        assertThat(Files.exists(newTargetNioPath)).isTrue();
        assertThat(Files.readString(newTargetNioPath)).isEqualTo("move test");
        assertThat(Files.exists(targetNioPath)).isFalse();
    }


    @Test
    void testDelete() throws IOException
    {
        // Create a test file
        Path testFile = rootPath.resolve("to-delete.txt");
        Files.write(testFile, "delete me".getBytes(StandardCharsets.UTF_8));

        // Test deleting the file
        FilePath fileToDelete = FilePath.of(testFile.toString());
        nioFileStorage.delete(fileToDelete);

        // Verify the file was deleted
        assertThat(Files.exists(testFile)).isFalse();

        // Test deleting a directory with contents
        Path dirPath = rootPath.resolve("dir-to-delete");
        Files.createDirectories(dirPath);
        Path fileInDir = dirPath.resolve("file-in-dir.txt");
        Files.write(fileInDir, "delete me too".getBytes(StandardCharsets.UTF_8));

        // Delete the directory
        FilePath dirToDelete = FilePath.of(dirPath.toString());
        nioFileStorage.delete(dirToDelete);

        // Verify the directory and its contents were deleted
        assertThat(Files.exists(dirPath)).isFalse();
        assertThat(Files.exists(fileInDir)).isFalse();

        // Test deleting a non-existent file (should not throw an exception)
        FilePath nonExistentFile = FilePath.of(rootPath.toString(), "non-existent.txt");
        assertThatThrownBy(() -> nioFileStorage.delete(nonExistentFile))
                .isInstanceOf(IOException.class);
    }


    @Test
    void testWalk() throws IOException
    {
        // Create a directory structure
        Path dir1 = rootPath.resolve("walk-test");
        Path dir2 = dir1.resolve("subdir1");
        Path dir3 = dir1.resolve("subdir2");
        Files.createDirectories(dir2);
        Files.createDirectories(dir3);

        // Create some files
        Path file1 = dir1.resolve("file1.txt");
        Path file2 = dir2.resolve("file2.txt");
        Path file3 = dir3.resolve("file3.txt");
        Files.write(file1, "file1".getBytes(StandardCharsets.UTF_8));
        Files.write(file2, "file2".getBytes(StandardCharsets.UTF_8));
        Files.write(file3, "file3".getBytes(StandardCharsets.UTF_8));

        // Test walking the directory tree
        FilePath startPath = FilePath.of(dir1.toString());
        List<FilePath> paths = nioFileStorage.walk(startPath).collect(Collectors.toList());

        // Verify all files and directories were found
        assertThat(paths).hasSize(6); // dir1, dir2, dir3, file1, file2, file3
        assertThat(paths).contains(
                FilePath.of(dir1.toString()),
                FilePath.of(dir2.toString()),
                FilePath.of(dir3.toString()),
                FilePath.of(file1.toString()),
                FilePath.of(file2.toString()),
                FilePath.of(file3.toString())
        );
    }


    @Test
    void testList() throws IOException
    {
        // Create a directory with some files
        Path dir = rootPath.resolve("list-test");
        Files.createDirectories(dir);
        Path file1 = dir.resolve("file1.txt");
        Path file2 = dir.resolve("file2.txt");
        Path subdir = dir.resolve("subdir");
        Files.createDirectories(subdir);
        Files.write(file1, "file1".getBytes(StandardCharsets.UTF_8));
        Files.write(file2, "file2".getBytes(StandardCharsets.UTF_8));

        // Test listing the directory
        FilePath dirPath = FilePath.of(dir.toString());
        List<FileInfo> fileInfos = nioFileStorage.list(dirPath);

        // Verify all files and directories were found
        assertThat(fileInfos).hasSize(3); // file1, file2, subdir

        // Verify file information
        FileInfo file1Info = fileInfos.stream()
                .filter(info -> info.getPath().toString().endsWith("file1.txt"))
                .findFirst()
                .orElseThrow();
        assertThat(file1Info.isRegularFile()).isTrue();
        assertThat(file1Info.isDirectory()).isFalse();
        assertThat(file1Info.getSize()).isEqualTo(5); // "file1" is 5 bytes

        // Verify directory information
        FileInfo subdirInfo = fileInfos.stream()
                .filter(info -> info.getPath().toString().endsWith("subdir"))
                .findFirst()
                .orElseThrow();
        assertThat(subdirInfo.isDirectory()).isTrue();
        assertThat(subdirInfo.isRegularFile()).isFalse();

        // Test listing a non-existent directory
        FilePath nonExistentDir = FilePath.of(rootPath.toString(), "non-existent-dir");
        List<FileInfo> emptyList = nioFileStorage.list(nonExistentDir);
        assertThat(emptyList).isEmpty();
    }


    @Test
    void testStat() throws IOException
    {
        // Create a test file
        Path testFile = rootPath.resolve("stat-test.txt");
        Files.write(testFile, "stat test".getBytes(StandardCharsets.UTF_8));

        // Test getting file information
        FilePath filePath = FilePath.of(testFile.toString());
        FileInfo fileInfo = nioFileStorage.stat(filePath);

        // Verify file information
        assertThat(fileInfo.getPath()).isEqualTo(filePath);
        assertThat(fileInfo.isRegularFile()).isTrue();
        assertThat(fileInfo.isDirectory()).isFalse();
        assertThat(fileInfo.getSize()).isEqualTo(9); // "stat test" is 9 bytes
        assertThat(fileInfo.getLastModifiedTime()).isNotNull();
        assertThat(fileInfo.getCreationTime()).isNotNull();

        // Test getting directory information
        FilePath dirPath = FilePath.of(rootPath.toString());
        FileInfo dirInfo = nioFileStorage.stat(dirPath);

        // Verify directory information
        assertThat(dirInfo.getPath()).isEqualTo(dirPath);
        assertThat(dirInfo.isDirectory()).isTrue();
        assertThat(dirInfo.isRegularFile()).isFalse();

        // Test with non-existent file
        FilePath nonExistentFile = FilePath.of(rootPath.toString(), "non-existent.txt");
        assertThatThrownBy(() -> nioFileStorage.stat(nonExistentFile))
                .isInstanceOf(IOException.class);
    }


    @Test
    void testIsDirectory() throws IOException
    {
        // Create a test directory
        Path testDir = rootPath.resolve("is-directory-test");
        Files.createDirectories(testDir);

        // Create a test file
        Path testFile = rootPath.resolve("is-directory-test.txt");
        Files.write(testFile, "not a directory".getBytes(StandardCharsets.UTF_8));

        // Test with a directory
        FilePath dirPath = FilePath.of(testDir.toString());
        assertThat(nioFileStorage.isDirectory(dirPath)).isTrue();

        // Test with a file
        FilePath filePath = FilePath.of(testFile.toString());
        assertThat(nioFileStorage.isDirectory(filePath)).isFalse();

        // Test with a non-existent path
        FilePath nonExistentPath = FilePath.of(rootPath.toString(), "non-existent");
        assertThat(nioFileStorage.isDirectory(nonExistentPath)).isFalse();
    }
}
