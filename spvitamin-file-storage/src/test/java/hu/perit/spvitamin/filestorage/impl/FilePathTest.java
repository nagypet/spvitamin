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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the FilePath class.
 * These tests verify the behavior of all public methods in the FilePath class,
 * including edge cases and normal usage scenarios.
 */
class FilePathTest
{
    @Test
    void testGetParent()
    {
        assertThat(new FilePath("C:\\Users\\user\\Documents\\test.txt").getParent())
                .hasToString("C:/Users/user/Documents");
        assertThat(new FilePath("C:\\Users\\user\\Documents\\test").getParent())
                .hasToString("C:/Users/user/Documents");
        assertThat(new FilePath("/var/sftp/upload").getParent())
                .hasToString("/var/sftp");
    }


    @Test
    void testConstructor()
    {
        // Test with null path
        assertThat(new FilePath(null)).hasToString("");

        // Test with empty path
        assertThat(new FilePath("")).hasToString("");

        // Test with trailing slash
        assertThat(new FilePath("/path/to/dir/")).hasToString("/path/to/dir");

        // Test with backslashes (Windows path)
        assertThat(new FilePath("C:\\path\\to\\file.txt")).hasToString("C:/path/to/file.txt");
    }


    /**
     * Parameterized test for the FilePath constructor with various input paths.
     * This demonstrates how to test multiple inputs efficiently.
     *
     * @param input    The input path string
     * @param expected The expected normalized path string
     */
    @ParameterizedTest
    @CsvSource({
            "null,''",
            "'',''",
            "'/path/to/dir/','/path/to/dir'",
            "'/path//to///dir/','/path/to/dir'",
            "'C:\\\\path\\\\to\\\\file.txt','C:/path/to/file.txt'",
            "'C:/path/to/file.txt','C:/path/to/file.txt'"
    })
    void testConstructorParameterized(String input, String expected)
    {
        String actualInput = "null".equals(input) ? null : input;
        assertThat(new FilePath(actualInput)).hasToString(expected);
    }


    @Test
    void testOfPath()
    {
        // Test with null Path
        assertThat(FilePath.of((Path) null)).hasToString("");

        // Test with Path object
        Path path = Path.of("/path/to/file.txt");
        assertThat(FilePath.of(path)).hasToString("/path/to/file.txt");
    }


    @Test
    void testOfStringVarargs()
    {
        assertThat(FilePath.of("/path")).hasToString("/path");

        // Test with multiple path segments (leading slashes are removed)
        assertThat(FilePath.of("/path", "to", "file.txt")).hasToString("/path/to/file.txt");

        // Test with empty segments
        assertThat(FilePath.of("path", "", "file.txt")).hasToString("path/file.txt");

        // Test with null segments
        assertThat(FilePath.of("path", null, "file.txt")).hasToString("path/file.txt");
    }


    @Test
    void testToString()
    {
        // Test toString returns the path
        FilePath filePath = new FilePath("/path/to/file.txt");
        assertThat(filePath).hasToString("/path/to/file.txt");
    }


    @Test
    void testGetParentEdgeCases()
    {
        // Test with empty path
        assertThat(new FilePath("").getParent()).isNull();

        // Test with path having no parent
        assertThat(new FilePath("file.txt").getParent()).isNull();

        // Test with root path
        assertThat(new FilePath("/").getParent()).isNull();
    }


    @Test
    void testCompareTo()
    {
        FilePath path1 = new FilePath("/path/to/file1.txt");
        FilePath path2 = new FilePath("/path/to/file2.txt");
        FilePath path3 = new FilePath("/PATH/TO/FILE1.TXT");

        // Test comparison with different paths
        assertThat(path1.compareTo(path2)).isLessThan(0);
        assertThat(path2.compareTo(path1)).isGreaterThan(0);

        // Test case-insensitive comparison
        assertThat(path1).isEqualByComparingTo(path3);
    }


    @Test
    void testEqualsAndHashCode()
    {
        FilePath path1 = new FilePath("/path/to/file.txt");
        FilePath path2 = new FilePath("/path/to/file.txt");
        FilePath path3 = new FilePath("/different/path.txt");

        // Test equals
        assertThat(path1).isEqualTo(path2).isNotEqualTo(path3);

        // Test hashCode
        assertThat(path1.hashCode()).isEqualTo(path2.hashCode()).isNotEqualTo(path3.hashCode());
    }


    @Test
    void testRelativize()
    {
        // Test with absolute paths having common prefix
        FilePath base1 = new FilePath("/a/b");
        FilePath target1 = new FilePath("/a/b/c/d");
        assertThat(base1.relativize(target1)).hasToString("c/d");

        // Test with relative paths having common prefix
        FilePath base2 = new FilePath("a/b");
        FilePath target2 = new FilePath("a/b/c/d");
        assertThat(base2.relativize(target2)).hasToString("c/d");

        // Test with paths having no common prefix
        FilePath base3 = new FilePath("/x/y");
        FilePath target3 = new FilePath("/a/b");
        assertThat(base3.relativize(target3)).hasToString("../../a/b");

        // Test with identical paths
        FilePath base4 = new FilePath("/a/b/c");
        FilePath target4 = new FilePath("/a/b/c");
        assertThat(base4.relativize(target4)).hasToString("");

        // Test with empty path as base
        FilePath base5 = new FilePath("");
        FilePath target5 = new FilePath("a/b");
        assertThat(base5.relativize(target5)).hasToString("a/b");

        // Test with empty path as target
        FilePath base6 = new FilePath("a/b");
        FilePath target6 = new FilePath("");
        assertThat(base6.relativize(target6)).hasToString("../..");

    }


    @Test
    void testRelativizeErrorCases()
    {
        // Test with null target
        FilePath base = new FilePath("/a/b");
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(NullPointerException.class, () -> {
            base.relativize(null);
        })).isNotNull();

        // Test with mixed absolute and relative paths
        FilePath absolutePath = new FilePath("/a/b");
        FilePath relativePath = new FilePath("c/d");

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            absolutePath.relativize(relativePath);
        })).isNotNull();

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            relativePath.relativize(absolutePath);
        })).isNotNull();
    }


    @Test
    void testRelativizeWithWindowsPaths()
    {
        // Test with Windows paths having the same drive letter
        FilePath base1 = new FilePath("C:\\Users\\user");
        FilePath target1 = new FilePath("C:\\Users\\user\\Documents");
        assertThat(base1.relativize(target1)).hasToString("Documents");

        // Test with deeper paths
        FilePath base2 = new FilePath("C:\\Users\\user");
        FilePath target2 = new FilePath("C:\\Users\\user\\Documents\\file.txt");
        assertThat(base2.relativize(target2)).hasToString("Documents/file.txt");

        // Test with paths having no common prefix except drive letter
        FilePath base3 = new FilePath("C:\\Program Files");
        FilePath target3 = new FilePath("C:\\Users\\user");
        assertThat(base3.relativize(target3)).hasToString("../Users/user");

        // Test with identical paths
        FilePath base4 = new FilePath("C:\\Users\\user\\Documents");
        FilePath target4 = new FilePath("C:\\Users\\user\\Documents");
        assertThat(base4.relativize(target4)).hasToString("");

        // Test with root of drive
        FilePath base5 = new FilePath("C:\\");
        FilePath target5 = new FilePath("C:\\Users\\user");
        assertThat(base5.relativize(target5)).hasToString("Users/user");

        // Test with target at root of drive
        FilePath base6 = new FilePath("C:\\Users\\user");
        FilePath target6 = new FilePath("C:\\");
        assertThat(base6.relativize(target6)).hasToString("../..");
    }


    @Test
    void testRelativizeWithDifferentDriveLetters()
    {
        // Test with different drive letters
        FilePath base = new FilePath("C:\\Users\\user");
        FilePath target = new FilePath("D:\\Documents");

        // This should throw an IllegalArgumentException because paths with different drive letters
        // cannot be relativized against each other
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> {
            base.relativize(target);
        })).isNotNull();
    }


    @ParameterizedTest
    @CsvSource({
            "null,false",
            "'',false",
            "'/path/to/dir/',true",
            "'/path//to///dir/',true",
            "'C:\\\\path\\\\to\\\\file.txt',true",
            "'C:/path/to/file.txt',true",
            "'path/to/dir/',false",
            "'\\server\\share\\path\\to\\file.txt',true",
            "'./path/to/dir/',false",
    })
    void testIsAbsolute(String input, boolean expected)
    {
        String actualInput = "null".equals(input) ? null : input;
        assertThat(new FilePath(actualInput).isAbsolute()).isEqualTo(expected);
    }
}
