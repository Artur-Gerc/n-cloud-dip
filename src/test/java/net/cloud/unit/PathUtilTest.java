package net.cloud.unit;

import net.cloud.util.PathUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class PathUtilTest {

    @Test
    public void getUserPathTest() {
        String path = PathUtil.getUserPath(1L);

        Assertions.assertEquals("user-1-files/", path);
    }

    @ParameterizedTest
    @CsvSource({
            "123, docs/photo.jpg, user-123-files/docs/photo.jpg",
            "1, /docs//file.txt, user-1-files/docs/file.txt",
            "999, subdir/, user-999-files/subdir/",
            "456, '', user-456-files/"
    })
    void getCompletePath(Long userId, String path, String expected) {

        String result = PathUtil.getCompletePath(userId, path);

        Assertions.assertEquals(expected, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/docs/", "user-123-files/", "folder//", "mixed///"})
    @DisplayName("isDirectory: Должен возвращать true, если путь заканчивается на /")
    void isDirectory_ShouldReturnTrue_WhenPathEndsWithSlash(String path) {
        Assertions.assertTrue(PathUtil.isDirectory(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {"file.txt", "user-1-files/file.png"})
    @DisplayName("isDirectory: Должен возвращать false, если путь НЕ заканчивается на /")
    void isDirectory_ShouldReturnFalse_WhenPathDoesNotEndWithSlash(String path) {
        Assertions.assertFalse(PathUtil.isDirectory(path));
    }

    @ParameterizedTest
    @CsvSource({
            "/docs/photo.jpg, photo.jpg",
            "justfile.txt, justfile.txt",
            "/, /",
            "//extra//path//to//file.xml, file.xml",
            "simple, simple"
    })
    @DisplayName("getFileName: Должен возвращать имя файла")
    void getFileName(String path, String expected) {

        String result = PathUtil.getFileName(path);

        Assertions.assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
            "/docs/files/, files/",
            "user-123-files/docs/sub/, sub/",
            "folder//, folder/",
            "/, /",
            "//extra///, extra/"
    })
    @DisplayName("getDirectoryName: Должен возвращать имя директории с завершающим /")
    void getDirectoryName_ShouldReturnDirNameWithSlash(String path, String expected) {
        String result = PathUtil.getDirectoryName(path);

        Assertions.assertEquals(expected, result);
    }

    @ParameterizedTest
    @CsvSource({
            "user-1-files/photo.jpg, photo.jpg",
            "file.txt, file.txt",
            "/root/data.xml, data.xml"
    })
    @DisplayName("getNameForDownload: Для файла должен возвращать только имя файла")
    void getNameForDownload_ShouldReturnFileName_WhenFile(String path, String expected) {

        String result = PathUtil.getNameForDownload(path);

        Assertions.assertEquals(expected, result);
    }
}
