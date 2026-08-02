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

import hu.perit.spvitamin.core.exception.ExceptionWrapper;
import hu.perit.spvitamin.filestorage.FileInfo;
import hu.perit.spvitamin.filestorage.FileStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.common.SftpException;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.integration.sftp.session.SftpSession;
import org.springframework.resilience.annotation.Retryable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Implementation of the FileStorage interface using SFTP protocol for remote file operations.
 * <p>
 * This implementation provides file operations on a remote SFTP server by using the
 * Spring Integration SFTP support through DefaultSftpSessionFactory. It supports most
 * operations defined in the FileStorage interface with some limitations:
 * <ul>
 *   <li>The walk method is not supported and throws UnsupportedOperationException</li>
 * </ul>
 * <p>
 * This class includes automatic retry logic for handling transient connection issues,
 * and provides detailed logging for each operation. It also includes a custom OutputStream
 * implementation that buffers data and writes it to SFTP when closed.
 */
@Slf4j
@RequiredArgsConstructor
@Retryable(
        value = {IllegalStateException.class},
        maxRetries = 10,
        delay = 100,
        maxDelay = 1000,
        multiplier = 2.0
)
public class SftpFileStorage implements FileStorage
{
    private final DefaultSftpSessionFactory sftpSessionFactory;


    /**
     * {@inheritDoc}
     * <p>
     * This implementation reads the entire file into memory using SftpSession.read()
     * and returns a ByteArrayInputStream. The OpenOption parameters are ignored.
     */
    @Override
    public InputStream newInputStream(FilePath path, OpenOption... options) throws IOException
    {
        log.debug("Creating new input stream for path: {}", path);
        try (SftpSession session = sftpSessionFactory.getSession())
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            session.read(path.toString(), outputStream);
            return new ByteArrayInputStream(outputStream.toByteArray());
        }
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns a custom OutputStream that buffers data in memory
     * and writes it to the SFTP server when closed. The OpenOption parameters are ignored.
     *
     * @see SftpOutputStream
     */
    @Override
    public OutputStream newOutputStream(FilePath path, OpenOption... options) throws IOException
    {
        log.debug("Creating new output stream for path: {}", path);
        boolean append = java.util.Arrays.asList(options).contains(StandardOpenOption.APPEND);
        // This is a custom OutputStream that buffers data and writes it to SFTP when closed
        return new SftpOutputStream(path, sftpSessionFactory, append);
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation creates directories recursively by splitting the path
     * and creating each directory in the path if it doesn't exist. The FileAttribute
     * parameters are ignored.
     */
    @Override
    public FilePath createDirectories(FilePath dir, FileAttribute<?>... attrs) throws IOException
    {
        log.debug("Creating directories for path: {}", dir);
        try (SftpSession session = sftpSessionFactory.getSession())
        {
            createDirectoriesRecursively(session, dir.toString());
            return dir;
        }
    }


    /**
     * Helper method to create directories recursively.
     *
     * @param session the SFTP session
     * @param path    the path to create
     * @throws IOException if an I/O error occurs
     */
    private void createDirectoriesRecursively(SftpSession session, String path) throws IOException
    {
        if (session.exists(path))
        {
            return; // Directory already exists
        }

        String[] parts = path.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String part : parts)
        {
            if (part.isEmpty())
            {
                continue;
            }
            currentPath.append("/").append(part);
            String dir = currentPath.toString();
            if (!session.exists(dir))
            {
                session.mkdir(dir);
            }
        }
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation reads the entire input stream into memory, ensures the parent
     * directories exist, and then writes the data to the target file. The CopyOption
     * parameters are ignored.
     */
    @Override
    public long copy(InputStream in, FilePath target, CopyOption... options) throws IOException
    {
        log.debug("Copying input stream to path: {}", target);
        try (SftpSession session = sftpSessionFactory.getSession())
        {
            // Ensure parent directories exist
            String parentDir = target.getParent().toString();
            createDirectoriesRecursively(session, parentDir);

            // Copy the input stream to the target
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;

            while ((bytesRead = in.read(buffer)) != -1)
            {
                baos.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }

            session.write(new ByteArrayInputStream(baos.toByteArray()), target.toString());
            return totalBytes;
        }
    }


    @Override
    public FilePath move(FilePath source, FilePath target, CopyOption... options) throws IOException
    {
        log.debug("Moving {} => {}", source, target);
        try (SftpSession session = sftpSessionFactory.getSession())
        {
            // Ensure parent directories exist
            String parentDir = target.getParent().toString();
            createDirectoriesRecursively(session, parentDir);

            // Moving
            session.rename(source.toString(), target.toString());
            return target;
        }
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation checks if the path is a directory and deletes it recursively
     * if it is, or simply removes the file if it's not a directory.
     */
    @Override
    public void delete(FilePath path) throws IOException
    {
        log.debug("Deleting path: {}", path);
        try (SftpSession session = sftpSessionFactory.getSession())
        {
            if (isDirectory(path))
            {
                // For directories, we need to delete recursively
                deleteRecursively(session, path.toString());
            }
            else
            {
                // For files, we can just remove
                session.remove(path.toString());
            }
        }
    }


    /**
     * Helper method to recursively delete a directory and all its contents.
     *
     * @param session the SFTP session
     * @param path    the path to the directory to delete
     * @throws IOException if an I/O error occurs
     */
    private void deleteRecursively(SftpSession session, String path) throws IOException
    {
        try
        {
            // List all entries in the directory
            var entries = session.list(path);

            for (var entry : entries)
            {
                String name = entry.getFilename();
                if (".".equals(name) || "..".equals(name))
                {
                    continue;
                }

                String fullPath = path.endsWith("/") ? path + name : path + "/" + name;

                if (entry.getAttributes().isDirectory())
                {
                    deleteRecursively(session, fullPath); // Recursively delete subdirectories
                }
                else
                {
                    session.remove(fullPath); // Delete file
                }
            }

            session.rmdir(path); // Delete the empty directory
        }
        catch (SftpException e)
        {
            ExceptionWrapper exception = ExceptionWrapper.of(e);
            if (exception.getFromCauseChain(SftpException.class).map(SftpException::getStatus).orElse(-1) == 2)
            {
                // No such file
                return;
            }
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation does not support walking file trees and always throws
     * UnsupportedOperationException.
     *
     * @throws UnsupportedOperationException always thrown as this operation is not supported
     */
    @Override
    public Stream<FilePath> walk(FilePath start, FileVisitOption... options) throws IOException
    {
        log.debug("Walking file tree starting at: {}", start);
        throw new UnsupportedOperationException("walk operation is not supported by SftpFileStorage");
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation lists the entries in a directory and creates FileInfo objects
     * for each entry. It filters out the "." and ".." entries. If the directory doesn't
     * exist, it returns an empty list.
     */
    @Override
    public List<FileInfo> list(FilePath dir) throws IOException
    {
        log.debug("Listing directory: {}", dir);
        try (SftpSession session = sftpSessionFactory.getSession())
        {
            SftpClient.DirEntry[] entries = session.list(dir.toString());
            List<FileInfo> result = new ArrayList<>();

            for (SftpClient.DirEntry entry : entries)
            {
                String filename = entry.getFilename();
                if (!".".equals(filename) && !"..".equals(filename))
                {
                    try
                    {
                        // Get directory status from entry attributes
                        SftpClient.Attributes attributes = entry.getAttributes();

                        // Converting the returned path to an absolute path
                        result.add(getFileInfo(FilePath.of(dir, filename), attributes));
                    }
                    catch (Exception e)
                    {
                        // Do nothing
                    }
                }
            }

            return result;
        }
        catch (Exception e)
        {
            ExceptionWrapper exception = ExceptionWrapper.of(e);
            if (exception.getFromCauseChain(SftpException.class).map(SftpException::getStatus).orElse(-1) == 2)
            {
                // No such file
                return List.of();
            }
            throw e;
        }
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation gets the file attributes using the SFTP client's stat method
     * and creates a FileInfo object from those attributes.
     */
    @Override
    public FileInfo stat(FilePath path) throws IOException
    {
        log.debug("Getting file info for path: {}", path);
        try (SftpSession session = sftpSessionFactory.getSession())
        {
            SftpClient.Attributes attributes = session.getClientInstance().stat(path.toString());
            return getFileInfo(path, attributes);
        }
    }


    /**
     * Helper method to create a FileInfo object from SFTP attributes.
     *
     * @param path       the path to the file
     * @param attributes the SFTP attributes
     * @return a FileInfo object
     * @throws RuntimeException if an error occurs creating the FileInfo
     */
    private static FileInfo getFileInfo(FilePath path, SftpClient.Attributes attributes)
    {
        try
        {
            boolean isDir = attributes != null && attributes.isDirectory();

            // Get file size from entry attributes (0 for directories)
            long fileSize = 0;
            if (!isDir && attributes != null && attributes.getSize() > 0)
            {
                fileSize = attributes.getSize();
            }

            Instant lastModified = attributes == null ? null : fileTimeToInstant(attributes.getModifyTime());
            Instant creationTime = attributes == null ? null : fileTimeToInstant(attributes.getCreateTime());

            // For SFTP, we assume a non-directory is a regular file
            boolean isRegularFile = !isDir;

            // Create FileInfo directly from entry data
            return new FileInfo(path, isDir, isRegularFile, fileSize, lastModified, creationTime);
        }
        catch (Exception e)
        {
            log.error("Error creating FileInfo for {}: {}", path, e.getMessage());
            throw e;
        }
    }


    private static Instant fileTimeToInstant(FileTime fileTime)
    {
        if (fileTime == null)
        {
            return null;
        }
        return fileTime.toInstant();
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation checks if the path is a directory by calling stat() and
     * checking the directory flag in the returned FileInfo. If an IOException occurs
     * (e.g., the file doesn't exist), it returns false.
     */
    @Override
    public boolean isDirectory(FilePath path, LinkOption... options)
    {
        log.debug("Checking if path is a directory: {}", path);
        try
        {
            FileInfo stat = stat(path);
            return stat.isDirectory();
        }
        catch (IOException e)
        {
            return false;
        }
    }


    /**
     * Custom OutputStream implementation that buffers data in memory and writes it to SFTP
     * when closed. This is necessary because SFTP doesn't support streaming writes.
     * <p>
     * Note that this implementation buffers all data in memory, so it's not suitable for
     * very large files.
     */
    private class SftpOutputStream extends OutputStream
    {
        private final FilePath path;
        private final DefaultSftpSessionFactory factory;
        private final ByteArrayOutputStream buffer;
        private final boolean append;
        private boolean closed = false;


        /**
         * Creates a new SftpOutputStream.
         *
         * @param path    the path to the file to write to
         * @param factory the SFTP session factory to use for creating sessions
         */
        public SftpOutputStream(FilePath path, DefaultSftpSessionFactory factory, Boolean append)
        {
            this.path = path;
            this.factory = factory;
            this.buffer = new ByteArrayOutputStream();
            this.append = BooleanUtils.isTrue(append);
        }


        /**
         * Writes a single byte to the buffer.
         *
         * @param b the byte to write
         */
        @Override
        public void write(int b)
        {
            buffer.write(b);
        }


        /**
         * Writes an array of bytes to the buffer.
         *
         * @param b the bytes to write
         * @throws IOException if an I/O error occurs
         */
        @Override
        public void write(byte[] b) throws IOException
        {
            buffer.write(b);
        }


        /**
         * Writes a portion of an array of bytes to the buffer.
         *
         * @param b   the bytes to write
         * @param off the offset in the array
         * @param len the number of bytes to write
         */
        @Override
        public void write(byte[] b, int off, int len)
        {
            buffer.write(b, off, len);
        }


        /**
         * Closes this output stream and writes the buffered data to the SFTP server.
         * This method ensures that the parent directories exist before writing the file.
         * If append mode is active, existing file content is prepended to the new data.
         * In overwrite mode (default), any existing file is deleted first.
         * This method is idempotent: subsequent calls after the first have no effect.
         *
         * @throws IOException if an I/O error occurs
         */
        @Override
        public void close() throws IOException
        {
            if (closed)
            {
                return;
            }
            closed = true;
            try (SftpSession session = factory.getSession())
            {
                // Ensure parent directories exist
                String parentDir = path.getParent().toString();
                createDirectoriesRecursively(session, parentDir);

                // Write the buffered data to the file
                byte[] dataToWrite;
                if (append && session.exists(path.toString()))
                {
                    // Read existing content and prepend it
                    ByteArrayOutputStream existingContent = new ByteArrayOutputStream();
                    session.read(path.toString(), existingContent);
                    ByteArrayOutputStream combined = new ByteArrayOutputStream();
                    combined.write(existingContent.toByteArray());
                    combined.write(buffer.toByteArray());
                    dataToWrite = combined.toByteArray();
                }
                else
                {
                    // Overwrite mode: delete existing file first to avoid duplicates
                    if (session.exists(path.toString()))
                    {
                        session.remove(path.toString());
                    }
                    dataToWrite = buffer.toByteArray();
                }

                session.write(new ByteArrayInputStream(dataToWrite), path.toString());
            }
            finally
            {
                buffer.close();
            }
        }
    }
}
