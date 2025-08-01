package net.cloud.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PathUtil {

    private static final String SEPARATOR = "/";

    public String getUserPath(Long userId) {

        return String.format("user-%s-files/", userId);
    }

    public String getCompletePath(Long userId, String path) {
        String completePath = getUserPath(userId) + path;

        return completePath.replaceAll("/+", SEPARATOR);
    }

    public boolean isDirectory(String path) {
        return path.endsWith(SEPARATOR);
    }

    public String getFileName(String path) {
        int lastSlash = path.lastIndexOf(SEPARATOR);

        return lastSlash > 0 ? path.substring(lastSlash + 1) : path;
    }

    public String getDirectoryName(String path) {
        String pathWithoutLastSlash = path.replaceAll("/+$", "");

        return getFileName(pathWithoutLastSlash) + SEPARATOR;
    }

    public String getNameForDownload(String path) {
        return getFileName(path);
    }
}
