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

import hu.perit.spvitamin.core.StackTracer;
import hu.perit.spvitamin.filestorage.FileInfo;
import hu.perit.spvitamin.filestorage.FileStorage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Implementation of the FileStorage interface using the Java NIO Files API.
 * <p>
 * This implementation provides file operations on the local file system by delegating
 * to the java.nio.file.Files class. It supports all operations defined in the FileStorage
 * interface and provides detailed logging for each operation.
 * <p>
 * This class is thread-safe as it relies on the thread-safety of the underlying
 * java.nio.file.Files methods.
 */
@Slf4j
public class NioFileStorage implements FileStorage
{
    private final FileSystem fs;


    public NioFileStorage()
    {
        this.fs = FileSystems.getDefault();
    }


    public NioFileStorage(FileSystem fs)
    {
        this.fs = fs;
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link Files#newInputStream(Path, OpenOption...)}.
     */
    @Override
    public InputStream newInputStream(FilePath path, OpenOption... options) throws IOException
    {
        log.debug("Creating new input stream for path: {}", path);
        return Files.newInputStream(this.fs.getPath(path.toString()), options);
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link Files#newOutputStream(Path, OpenOption...)}.
     */
    @Override
    public OutputStream newOutputStream(FilePath path, OpenOption... options) throws IOException
    {
        log.debug("Creating new output stream for path: {}", path);
        return Files.newOutputStream(this.fs.getPath(path.toString()), options);
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link Files#createDirectories(Path, FileAttribute[])}.
     */
    @Override
    public FilePath createDirectories(FilePath dir, FileAttribute<?>... attrs) throws IOException
    {
        log.debug("Creating directories for path: {}", dir);
        return FilePath.of(Files.createDirectories(this.fs.getPath(dir.toString()), attrs));
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link Files#copy(InputStream, Path, CopyOption...)}.
     */
    @Override
    public long copy(InputStream in, FilePath target, CopyOption... options) throws IOException
    {
        log.debug("Copying input stream to path: {}", target);
        return Files.copy(in, this.fs.getPath(target.toString()), options);
    }


    @Override
    public FilePath move(FilePath source, FilePath target, CopyOption... options) throws IOException
    {
        log.debug("Moving {} => {}", source, target);
        createDirectories(target.getParent());
        return FilePath.of(Files.move(this.fs.getPath(source.toString()), this.fs.getPath(target.toString())));
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link Files#delete(Path)}.
     */
    @Override
    public void delete(FilePath path) throws IOException
    {
        log.debug("Deleting path: {}", path);
        if (isDirectory(path))
        {
            // For directories, we need to delete recursively
            deleteFolder(this.fs.getPath(path.toString()));
        }
        else
        {
            // For files, we can just remove
            Files.delete(this.fs.getPath(path.toString()));
        }
    }


    private void deleteFolder(Path folder) throws IOException
    {
        try (Stream<Path> walk = Files.walk(folder))
        {
            walk.sorted(Comparator.reverseOrder()).forEach(this::deleteFile);
        }
    }


    private void deleteFile(Path path)
    {
        try
        {
            Files.delete(this.fs.getPath(path.toString()));
        }
        catch (IOException e)
        {
            log.error("Error deleting file: {}. {}", path, StackTracer.toString(e));
        }
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link Files#walk(Path, FileVisitOption...)}.
     */
    @Override
    public Stream<FilePath> walk(FilePath start, FileVisitOption... options) throws IOException
    {
        log.debug("Walking file tree starting at: {}", start);
        return Files.walk(this.fs.getPath(start.toString()), options).map(path -> FilePath.of(path.toString()));
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses {@link Files#list(Path)} to get the entries in the directory,
     * then calls {@link #stat(FilePath)} on each entry to get its file information.
     */
    @Override
    public List<FileInfo> list(FilePath dir) throws IOException
    {
        log.debug("Listing directory: {}", dir);
        try (Stream<Path> stream = Files.list(this.fs.getPath(dir.toString())))
        {
            return stream
                    .map(path -> {
                        try
                        {
                            return stat(FilePath.of(path));
                        }
                        catch (IOException e)
                        {
                            log.error("Error getting file info for {}: {}", path, e.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        }
        catch (NoSuchFileException e)
        {
            return List.of();
        }
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation uses {@link Files#readAttributes(Path, Class, LinkOption...)} to get
     * the basic file attributes, {@link Files#isDirectory(Path, LinkOption...)} to check if the path
     * is a directory, and {@link Files#isRegularFile(Path, LinkOption...)} to check if the path is a
     * regular file.
     */
    @Override
    public FileInfo stat(FilePath path) throws IOException
    {
        log.debug("Getting file info for path: {}", path);
        Path nioPath = this.fs.getPath(path.toString());
        BasicFileAttributes attrs = Files.readAttributes(nioPath, BasicFileAttributes.class);
        return new FileInfo(
                path,
                Files.isDirectory(nioPath),
                Files.isRegularFile(nioPath),
                attrs.size(),
                attrs.lastModifiedTime().toInstant(),
                attrs.creationTime().toInstant()
        );
    }


    /**
     * {@inheritDoc}
     * <p>
     * This implementation delegates to {@link Files#isDirectory(Path, LinkOption...)}.
     */
    @Override
    public boolean isDirectory(FilePath path, LinkOption... options)
    {
        log.debug("Checking if path is a directory: {}", path);
        return Files.isDirectory(this.fs.getPath(path.toString()), options);
    }
}
