package com.plakhotnikov.cloudkeeper.storage.repository;

import com.plakhotnikov.cloudkeeper.storage.exception.repository.DeleteException;
import com.plakhotnikov.cloudkeeper.storage.exception.repository.MinioRepositoryException;
import com.plakhotnikov.cloudkeeper.storage.exception.repository.ObjectDoesNotExistException;
import com.plakhotnikov.cloudkeeper.storage.exception.repository.UploadException;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@Profile("minio")
public class MinioRepository {

    private final String bucketName;
    private final MinioClient minioClient;

    public MinioRepository(MinioClient minioClient,
                           @Value("${minio.bucket-name}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    public void upload(String objPath, InputStream fileStream, long size) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objPath)
                            .stream(fileStream, size, -1)
                            .build());
        } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            log.error("During uploading single object an exception occurred: {}", e.getMessage());
            throw new UploadException(e.getMessage());
        }
    }

    public void upload(List<SnowballObject> objects) {
        try {
            minioClient.uploadSnowballObjects(
                    UploadSnowballObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objects)
                            .build());
        } catch (IOException | ErrorResponseException | InsufficientDataException | InternalException |
                 InvalidKeyException | InvalidResponseException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            log.error("During uploading multiple objects an exception occurred: {}", e.getMessage());
            throw new UploadException(e.getMessage());
        }
    }


    public InputStream get(String objPath) {

        if (!isObjectExist(objPath)) {
            log.error("Object {} does not exist", objPath);
            throw new MinioRepositoryException("File %s not found".formatted(objPath));
        }

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objPath)
                            .build());

        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            log.error("During accessing object {} an exception occurred: {}", objPath, e.getMessage());
            throw new ObjectDoesNotExistException(e.getMessage());
        }

    }

    public Map<Item, byte[]> getAfterPath(String path) {
        List<Item> objects = listRecursively(path);
        Map<Item, byte[]> resultMap = new HashMap<>();
        for (Item obj : objects) {
            try (InputStream stream = get(obj.objectName())) {
                resultMap.put(obj, stream.readAllBytes());
            } catch (IOException e) {
                log.error("During handling object {} inside getAfterPath an exception occurred: {}",
                        obj.objectName(), e.getMessage());
                throw new MinioRepositoryException(e.getMessage());
            }
        }
        return resultMap;
    }


    public void delete(String objPath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objPath)
                            .build());

        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            log.error("During deleting object {} an exception occurred: {}", objPath, e.getMessage());
            throw new DeleteException(e.getMessage());
        }
    }

    public void delete(Iterable<DeleteObject> objects) {
        try {
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucketName)
                            .objects(objects)
                            .build());
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.error("During deleting object {} an exception occurred: {}", error.objectName(), error.message());
            }

        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            log.error("During deleting multiple objects an exception occurred: {}", e.getMessage());
            throw new DeleteException(e.getMessage());
        }
    }


    public void copy(String objPath, String copyPath) {
        try {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucketName)
                            .object(copyPath)
                            .source(
                                    CopySource.builder()
                                            .bucket(bucketName)
                                            .object(objPath)
                                            .build())
                            .build());

        } catch (ServerException | InsufficientDataException | ErrorResponseException | IOException |
                 NoSuchAlgorithmException | InvalidKeyException | InvalidResponseException | XmlParserException |
                 InternalException e) {
            log.error("During copying '{}' to '{}' an exception occurred: {}", objPath, copyPath, e.getMessage());
            throw new MinioRepositoryException(e.getMessage());
        }
    }

    public void rename(String objPath, String newFilepath) {
        copy(objPath, newFilepath);
        delete(objPath);
    }

    public List<Item> list(String prefix) {
        return extractItems(minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(prefix)
                        .build()));
    }

// --Commented out by Inspection START (16.12.2024 13:28):
//    public List<Item> list(String prefix, String startsAfter) {
//        return extractItems(minioClient.listObjects(
//                ListObjectsArgs.builder()
//                        .bucket(bucketName)
//                        .prefix(prefix)
//                        .startAfter(startsAfter)
//                        .build()));
//    }
// --Commented out by Inspection STOP (16.12.2024 13:28)


    public List<Item> listRecursively(String prefix) {
        return extractItems(minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .recursive(true)
                        .prefix(prefix)
                        .build()));
    }


    private List<Item> extractItems(Iterable<Result<Item>> resultIterable) {
        List<Item> items = new ArrayList<>();

        try {
            for (Result<Item> result : resultIterable) {
                items.add(result.get());
            }
        } catch (ErrorResponseException | InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            log.error("During extracting items an exception occurred: {}", e.getMessage());
            throw new MinioRepositoryException(e.getMessage());
        }

        return items;
    }

    public boolean isObjectDir(String objPath) {
        if (objPath.endsWith("/")) {
            objPath = objPath.substring(0, objPath.length() - 1);
        }

        if (!isObjectExist(objPath)) {
            for (Item item : list(objPath)) {
                if (item.objectName().equals(objPath + "/")) {
                    log.debug("'{}' is folder", objPath);
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isObjectExist(String objPath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objPath)
                            .build());
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                log.debug("Requested object '{}' not found", objPath);
            } else {
                log.warn("During checking existence of '{}' an exception occurred: {}", objPath, e.getMessage());
            }
            return false;
        } catch (InsufficientDataException | InternalException | InvalidKeyException |
                 InvalidResponseException | IOException | NoSuchAlgorithmException | ServerException |
                 XmlParserException e) {
            log.error("During checking existence of '{}' an exception occurred: {}", objPath, e.getMessage());
            return false;
        }

    }

    public void createDefaultBucket(){
        createBucket(bucketName);
    }

    public void createBucket(String bucketName) {
        try {
            if (!isBucketExist(bucketName)) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build());
                log.info("Default bucket '{}' has been created successfully", bucketName);
            } else {
                log.info("Default bucket '{}' exists", bucketName);
            }

        } catch (Exception e) {
            log.error("During creating default bucket '{}' an exception occurred: {}", bucketName, e.getMessage());
            throw new MinioRepositoryException(e.getMessage());
        }
    }

    public boolean isDefaultBucketExist(){
        return isBucketExist(bucketName);
    }

    public boolean isBucketExist(String bucketName) {
        try {
            return minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());
        } catch (InsufficientDataException | InternalException | InvalidKeyException | InvalidResponseException |
                 IOException | NoSuchAlgorithmException | ServerException | XmlParserException |
                 ErrorResponseException e) {
            log.error("During checking existence of bucket '{}' an exception occurred: {}", bucketName, e.getMessage());
            return false;
        }

    }

}
