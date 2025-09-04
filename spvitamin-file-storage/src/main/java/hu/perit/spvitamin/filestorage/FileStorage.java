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

package hu.perit.spvitamin.filestorage;

import hu.perit.spvitamin.filestorage.impl.FilePath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitOption;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.stream.Stream;

/**
 * Interface for file storage operations that abstracts the underlying storage mechanism.
 * This interface provides methods for common file operations such as reading, writing,
 * creating directories, copying files, and retrieving file information.
 * <p>
 * Implementations of this interface can use different storage backends such as
 * local file system, SFTP, cloud storage, etc.
 */
public interface FileStorage
{
    /**
     * Opens a file for reading, returning an input stream to read from the file.
     * 
     * <p>The caller is responsible for closing the returned input stream.</p>
     *
     * <p>Edge cases:</p>
     * <ul>
     *   <li>If the file does not exist, a {@link java.nio.file.NoSuchFileException} is thrown</li>
     *   <li>If the file exists but is a directory, a {@link java.nio.file.FileSystemException} is thrown</li>
     *   <li>If the file cannot be opened for reading due to insufficient permissions, a {@link java.nio.file.AccessDeniedException} is thrown</li>
     * </ul>
     *
     * @param path    the path to the file to open
     * @param options options specifying how the file is opened
     * @return a new input stream that must be closed by the caller
     * @throws IOException if an I/O error occurs
     */
    InputStream newInputStream(FilePath path, OpenOption... options) throws IOException;

    /**
     * Opens or creates a file for writing, returning an output stream to write to the file.
     * 
     * <p>The caller is responsible for closing the returned output stream.</p>
     *
     * <p>Edge cases:</p>
     * <ul>
     *   <li>If the file exists but is a directory, a {@link java.nio.file.FileSystemException} is thrown</li>
     *   <li>If the file cannot be created or opened for writing due to insufficient permissions, a {@link java.nio.file.AccessDeniedException} is thrown</li>
     *   <li>If the parent directory does not exist, a {@link java.nio.file.NoSuchFileException} is thrown unless the {@link java.nio.file.StandardOpenOption#CREATE} option is specified</li>
     * </ul>
     *
     * @param path    the path to the file to open or create
     * @param options options specifying how the file is opened
     * @return a new output stream that must be closed by the caller
     * @throws IOException if an I/O error occurs
     */
    OutputStream newOutputStream(FilePath path, OpenOption... options) throws IOException;

    /**
     * Creates a directory by creating all nonexistent parent directories first.
     * 
     * <p>Edge cases:</p>
     * <ul>
     *   <li>If the directory already exists, the method returns the existing directory path</li>
     *   <li>If a file (not a directory) exists at the specified path, a {@link java.nio.file.FileAlreadyExistsException} is thrown</li>
     *   <li>If the operation fails due to insufficient permissions, a {@link java.nio.file.AccessDeniedException} is thrown</li>
     *   <li>If any parent directory cannot be created because a file with the same name exists, a {@link java.nio.file.FileAlreadyExistsException} is thrown</li>
     * </ul>
     *
     * @param dir   the directory to create
     * @param attrs an optional list of file attributes to set atomically when creating the directory
     * @return the directory path
     * @throws IOException if an I/O error occurs
     */
    FilePath createDirectories(FilePath dir, FileAttribute<?>... attrs) throws IOException;

    /**
     * Copies all bytes from an input stream to a file.
     * 
     * <p>The caller is responsible for closing the input stream. This method does not close the input stream.</p>
     *
     * <p>Edge cases:</p>
     * <ul>
     *   <li>If the target file already exists, it will be replaced unless the {@link java.nio.file.StandardCopyOption#REPLACE_EXISTING} option is not specified</li>
     *   <li>If the target file exists but is a directory, a {@link java.nio.file.FileSystemException} is thrown</li>
     *   <li>If the parent directory of the target file does not exist, a {@link java.nio.file.NoSuchFileException} is thrown</li>
     *   <li>If the input stream is closed or reaches end-of-file before any bytes are read, a file of size 0 bytes is created at the target location</li>
     * </ul>
     *
     * @param in      the input stream to read from (not closed by this method)
     * @param target  the path to the file to write to
     * @param options options specifying how the copy should be done
     * @return the number of bytes copied
     * @throws IOException if an I/O error occurs
     */
    long copy(InputStream in, FilePath target, CopyOption... options) throws IOException;

    /**
     * Moves a file or directory from a source path to a target path.
     * If the move operation is unsuccessful for any reason (e.g., target already exists, I/O error),
     * an {@code IOException} is thrown. The behavior of the move operation can be influenced
     * using the {@code options} parameter.
     *
     * <p>Edge cases:</p>
     * <ul>
     *   <li>If the source does not exist, a {@link java.nio.file.NoSuchFileException} is thrown</li>
     *   <li>If the target already exists and the {@link java.nio.file.StandardCopyOption#REPLACE_EXISTING} option is not specified, a {@link java.nio.file.FileAlreadyExistsException} is thrown</li>
     *   <li>If the source and target are the same file, the method completes without moving the file</li>
     *   <li>If the source is a directory, the entire directory tree is moved</li>
     *   <li>If the parent directory of the target does not exist, it will be created automatically</li>
     *   <li>If the move operation fails due to insufficient permissions, a {@link java.nio.file.AccessDeniedException} is thrown</li>
     * </ul>
     *
     * @param source  the path representing the source file or directory to move
     * @param target  the path representing the target location for the move operation
     * @param options options specifying how the move should be performed
     * @return the path to the target file or directory after the move operation
     * @throws IOException if an I/O error occurs during the move operation
     */
    FilePath move(FilePath source, FilePath target, CopyOption... options) throws IOException;

    /**
     * Deletes a file or directory.
     *
     * <p>Edge cases:</p>
     * <ul>
     *   <li>If the path does not exist, a {@link java.nio.file.NoSuchFileException} may be thrown (implementation-dependent)</li>
     *   <li>If the path is a directory, the directory and all its contents are deleted recursively</li>
     *   <li>If the deletion fails due to insufficient permissions, a {@link java.nio.file.AccessDeniedException} is thrown</li>
     *   <li>If the directory is not empty and the implementation does not support recursive deletion, a {@link java.nio.file.DirectoryNotEmptyException} may be thrown</li>
     * </ul>
     *
     * @param path the path to the file or directory to delete
     * @throws IOException if an I/O error occurs
     */
    void delete(FilePath path) throws IOException;

    /**
     * Walks a file tree.
     * 
     * <p>The caller is responsible for closing the returned stream. It is recommended to use
     * try-with-resources to ensure the stream is properly closed.</p>
     *
     * <p>Edge cases:</p>
     * <ul>
     *   <li>If the starting path does not exist, a {@link java.nio.file.NoSuchFileException} is thrown</li>
     *   <li>If the starting path is not a directory, the stream will contain only that file</li>
     *   <li>If an I/O error occurs during traversal, it may be wrapped in an {@link java.io.UncheckedIOException} when accessing stream elements</li>
     *   <li>If the {@link java.nio.file.FileVisitOption#FOLLOW_LINKS} option is specified, symbolic links are followed, which may lead to cycles in the traversal</li>
     * </ul>
     *
     * <p>Example usage with proper resource management:</p>
     * <pre>{@code
     * try (Stream<FilePath> pathStream = fileStorage.walk(startPath)) {
     *     pathStream.forEach(path -> ...);
     * }
     * }</pre>
     *
     * @param start   the starting file
     * @param options options to configure the traversal
     * @return a Stream of FilePath objects that must be closed by the caller
     * @throws IOException if an I/O error occurs
     */
    Stream<FilePath> walk(FilePath start, FileVisitOption... options) throws IOException;

    /**
     * Returns a list of file information for the entries in a directory.
     *
     * <p>Edge cases:</p>
     * <ul>
     *   <li>If the directory does not exist, an empty list may be returned or a {@link java.nio.file.NoSuchFileException} may be thrown (implementation-dependent)</li>
     *   <li>If the path exists but is not a directory, a {@link java.nio.file.NotDirectoryException} may be thrown</li>
     *   <li>If the directory cannot be read due to insufficient permissions, a {@link java.nio.file.AccessDeniedException} is thrown</li>
     *   <li>Hidden files and directories may be included in the results (implementation-dependent)</li>
     *   <li>The returned list does not include the special entries "." and ".." (if supported by the file system)</li>
     * </ul>
     *
     * <p>Note: This method uses streams internally but handles their closure, so the caller does not need to close any resources.</p>
     *
     * @param dir the path to the directory
     * @return a list of file information
     * @throws IOException if an I/O error occurs
     */
    List<FileInfo> list(FilePath dir) throws IOException;

    /**
     * Returns file information for a file or directory.
     *
     * <p>Edge cases:</p>
     * <ul>
     *   <li>If the file or directory does not exist, a {@link java.nio.file.NoSuchFileException} is thrown</li>
     *   <li>If the file or directory cannot be accessed due to insufficient permissions, a {@link java.nio.file.AccessDeniedException} is thrown</li>
     *   <li>If the file is a symbolic link, the information returned may be for the link itself or for the target of the link, depending on the implementation</li>
     *   <li>Some file attributes may not be supported by all file systems, in which case default or estimated values may be provided</li>
     * </ul>
     *
     * @param path the path to the file or directory
     * @return the file information containing details such as size, modification time, and file type
     * @throws IOException if an I/O error occurs
     */
    FileInfo stat(FilePath path) throws IOException;

    /**
     * Tests whether a file is a directory.
     *
     * <p>Edge cases:</p>
     * <ul>
     *   <li>If the file does not exist, the method returns false</li>
     *   <li>If the file exists but cannot be accessed due to insufficient permissions, the method may return false or throw a {@link java.nio.file.AccessDeniedException} (implementation-dependent)</li>
     *   <li>If the path is a symbolic link, the behavior depends on the options provided. By default, symbolic links are followed. To prevent following links, specify the {@link java.nio.file.LinkOption#NOFOLLOW_LINKS} option</li>
     *   <li>If an I/O error occurs while checking the file, the method may return false or throw an exception (implementation-dependent)</li>
     * </ul>
     *
     * @param path    the path to the file
     * @param options options indicating how symbolic links are handled
     * @return true if the file is a directory; false if the file does not exist, is not a directory, or cannot be determined
     */
    boolean isDirectory(FilePath path, LinkOption... options);
}
