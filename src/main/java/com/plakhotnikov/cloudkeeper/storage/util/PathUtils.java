package com.plakhotnikov.cloudkeeper.storage.util;

import com.plakhotnikov.cloudkeeper.storage.model.dto.StorageDto;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@UtilityClass
public class PathUtils {

    public static String normalize(@NotNull String path) {

        path = removeSpaces(path);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        try {
            return URI.create(path).normalize().toString();
        } catch (IllegalArgumentException | NullPointerException e) {
            log.debug("Caught exception when trying to normalize path '{}' as uri: {}", path, e.getMessage());
            return path;
        }

    }

    public static String removeSpaces(String s){
        return s.trim().replaceAll("\\s+", " ");
    }

    public static String getEncodedPath(StorageDto reqDto) {
        return URLEncoder.encode(reqDto.getPath(), StandardCharsets.UTF_8);
    }


    public static String formFullPath(StorageDto reqDto) {
        return "user-%d-files/%s/".formatted(reqDto.getUserId(), reqDto.getPath()).replace("//", "/");
    }

    public static String removeUserPrefix(String prefixedPath) {
        return prefixedPath.replaceFirst("user-[0-9]{1,18}-files/", "");
    }

    public static String extractOrigName(String fullObjPath) {
        return fullObjPath.substring(fullObjPath.lastIndexOf("/") + 1);
    }

    public static String extractNameFromPath(String fullObjPath, String path) {
        fullObjPath = fullObjPath.substring(path.length());
        return removeUserPrefix(fullObjPath);
    }

    public static String handleFileExtension(String oldName, String newName) {
        int lastDotIndex = oldName.lastIndexOf(".");
        if (lastDotIndex != -1) {
            return newName + oldName.substring(lastDotIndex);
        }

        return newName;
    }
}
