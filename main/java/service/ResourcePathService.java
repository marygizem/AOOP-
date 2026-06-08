package service;

import java.nio.file.Paths;

/**
 * Centralized path builder for project resource files.
 */
public final class ResourcePathService {
    private ResourcePathService() {
    }

    public static String resourceFile(String fileName) {
        return Paths.get(System.getProperty("user.dir"), "src", "main", "resource", fileName).toString();
    }

    public static String imageFile(String fileName) {
        return Paths.get(System.getProperty("user.dir"), "images", fileName).toString();
    }
}
