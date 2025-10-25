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

import hu.perit.spvitamin.core.filename.FileNameUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Path;
import java.util.Objects;

@Getter
@EqualsAndHashCode(of = "path")
public final class FilePath implements Comparable<FilePath>
{
    private final String path;


    public static FilePath of(Path path)
    {
        return of(path != null ? path.toString() : null);
    }


    public static FilePath of(String path, String... morePaths)
    {
        if (morePaths.length == 0)
        {
            return new FilePath(path);
        }
        String merged = path + "/" + FileNameUtils.getPath(morePaths);
        return new FilePath(merged);
    }


    public static FilePath of(FilePath path, String... morePaths)
    {
        if (morePaths.length == 0)
        {
            return path;
        }
        String merged = path.toString() + "/" + FileNameUtils.getPath(morePaths);
        return new FilePath(merged);
    }


    public FilePath(String path)
    {
        if (path == null)
        {
            this.path = "";
        }
        else
        {
            // Replace backslashes with forward slashes
            path = path.replace('\\', '/');

            // Remove trailing slash if present
            if (path.endsWith("/"))
            {
                path = path.substring(0, path.length() - 1);
            }

            // Normalize consecutive slashes
            path = path.replaceAll("/+", "/");

            this.path = path;
        }
    }


    @Override
    public String toString()
    {
        return this.path;
    }


    /**
     * Returns the <em>parent path</em>, or {@code null} if this path does not
     * have a parent.
     *
     * <p> The parent of this path object consists of this path's root
     * component, if any, and each element in the path except for the
     * <em>farthest</em> from the root in the directory hierarchy. This method
     * does not access the file system; the path or its parent may not exist.
     * Furthermore, this method does not eliminate special names such as "."
     * and ".." that may be used in some implementations. On UNIX for example,
     * the parent of "{@code /a/b/c}" is "{@code /a/b}", and the parent of
     * {@code "x/y/.}" is "{@code x/y}".
     *
     * @return a path representing the path's parent
     */
    public FilePath getParent()
    {
        // Get the folder part of the path
        if (StringUtils.isBlank(this.path))
        {
            return null;
        }

        int lastSeparatorIndex = this.path.lastIndexOf("/");
        if (lastSeparatorIndex == -1)
        {
            return null;
        }
        return new FilePath(this.path.substring(0, lastSeparatorIndex));
    }


    @Override
    public int compareTo(FilePath o)
    {
        return Objects.compare(this.path, o.path, String.CASE_INSENSITIVE_ORDER);
    }


    /**
     * For example, on UNIX, if this path is {@code "/a/b"} and the given path is {@code "/a/b/c/d"}
     * then the resulting relative path would be {@code "c/d"}.
     * <p>
     * For Windows paths with drive letters, if this path is {@code "C:/a/b"} and the given path is {@code "C:/a/b/c/d"}
     * then the resulting relative path would be {@code "c/d"}.
     *
     * @param other the target FilePath to make relative to this path
     * @return a FilePath representing the relative path from this to {@code other}
     * @throws NullPointerException     if {@code other} is null
     * @throws IllegalArgumentException if one path is absolute and the other is relative,
     *                                  or if the paths have different drive letters
     */
    public FilePath relativize(FilePath other)
    {
        java.util.Objects.requireNonNull(other, "other");

        String base = this.path == null ? "" : this.path;
        String target = other.path == null ? "" : other.path;

        // Extract drive letters if present
        String baseDrive = extractDriveLetter(base);
        String targetDrive = extractDriveLetter(target);

        // If one has a drive letter and the other doesn't, or they have different drive letters, throw exception
        if (!Objects.equals(baseDrive, targetDrive))
        {
            throw new IllegalArgumentException("Paths with different drive letters cannot be relativized against each other");
        }

        // Remove drive letter from paths for further processing
        if (baseDrive != null)
        {
            base = base.substring(baseDrive.length());
            target = target.substring(targetDrive.length());
        }

        // For Windows paths with drive letters, they are always considered absolute
        // So we don't need to check if they're both absolute or both relative
        boolean baseAbs = base.startsWith("/");
        boolean targetAbs = target.startsWith("/");

        // Only check for mixed absolute/relative if there's no drive letter
        if (baseDrive == null && baseAbs != targetAbs)
        {
            throw new IllegalArgumentException("Both paths must be either absolute or relative");
        }

        // Strip leading slash for consistent splitting
        String baseTrim = baseAbs && !base.isEmpty() ? base.substring(1) : base;
        String targetTrim = targetAbs && !target.isEmpty() ? target.substring(1) : target;

        String[] baseSegs = baseTrim.isEmpty() ? new String[0] : baseTrim.split("/");
        String[] targetSegs = targetTrim.isEmpty() ? new String[0] : targetTrim.split("/");

        int i = 0;
        int max = Math.min(baseSegs.length, targetSegs.length);
        while (i < max && baseSegs[i].equals(targetSegs[i]))
        {
            i++;
        }

        java.util.List<String> parts = new java.util.ArrayList<>();

        // For each remaining segment in base, we need a ".."
        for (int j = i; j < baseSegs.length; j++)
        {
            if (!baseSegs[j].isEmpty())
            {
                parts.add("..");
            }
        }

        // Then append the remaining segments from target
        for (int j = i; j < targetSegs.length; j++)
        {
            if (!targetSegs[j].isEmpty())
            {
                parts.add(targetSegs[j]);
            }
        }

        String rel = String.join("/", parts);
        return new FilePath(rel);
    }


    /**
     * Extracts the drive letter from a Windows path, if present.
     * For example, "C:/path" would return "C:", and "/path" would return an empty string.
     *
     * @param path the path to extract the drive letter from
     * @return the drive letter with colon, or null if no drive letter is present
     */
    private static String extractDriveLetter(String path)
    {
        if (path.length() >= 2 && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':')
        {
            return path.substring(0, 2);
        }
        return null;
    }


    private static boolean hasDriveLetter(String path)
    {
        return StringUtils.isNotBlank(extractDriveLetter(path));
    }


    public boolean isAbsolute()
    {
        if (StringUtils.isBlank(this.path))
        {
            return false;
        }

        return this.path.startsWith("/") || hasDriveLetter(this.path);
    }


    public boolean isEmpty()
    {
        return StringUtils.isBlank(this.path);
    }


    public String plus(String path)
    {
        return FilePath.of(this.path, path).getPath();
    }
}
