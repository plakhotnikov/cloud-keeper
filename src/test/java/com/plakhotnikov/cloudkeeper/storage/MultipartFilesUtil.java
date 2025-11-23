package com.plakhotnikov.cloudkeeper.storage;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class MultipartFilesUtil {

    public static List<MultipartFile> generateMockMultipartFilesAndFolder(int totalAmount) {
        List<MultipartFile> files = MultipartFilesUtil.generateMockMultipartFiles(totalAmount - 1);
        files.add(MultipartFilesUtil.generateMockMultipartFolder());
        return files;
    }

    public static List<MultipartFile> generateMockMultipartFiles(int amount) {
        ArrayList<MultipartFile> multipartFiles = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            multipartFiles.add(
                    new MockMultipartFile(
                            "files",
                            "MockName" + i + ".txt",
                            "plain/text",
                            new byte[]{1, 2, 3})
            );
        }

        return multipartFiles;

    }

    public static MultipartFile generateMockMultipartFolder() {
        return new MockMultipartFile(
                "files",
                "fakeFolder",
                "content",
                new byte[]{});
    }
}
