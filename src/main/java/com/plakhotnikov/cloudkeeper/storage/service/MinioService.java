package com.plakhotnikov.cloudkeeper.storage.service;

import com.plakhotnikov.cloudkeeper.storage.exception.service.MinioServiceException;
import com.plakhotnikov.cloudkeeper.storage.exception.service.NoSuchObjectException;
import com.plakhotnikov.cloudkeeper.storage.exception.service.NotEnoughFreeSpaceException;
import com.plakhotnikov.cloudkeeper.storage.exception.service.SuchFolderAlreadyExistsException;
import com.plakhotnikov.cloudkeeper.storage.mapper.BreadcrumbMapper;
import com.plakhotnikov.cloudkeeper.storage.model.StorageInfo;
import com.plakhotnikov.cloudkeeper.storage.model.dto.*;
import com.plakhotnikov.cloudkeeper.storage.repository.MinioRepository;
import com.plakhotnikov.cloudkeeper.storage.util.PathUtils;
import io.minio.SnowballObject;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@Profile("minio")
public class MinioService {

    private final MinioRepository minioRepository;
    private final BreadcrumbMapper breadcrumbMapper;
    private final long maxFileSize;
    private final long maxRequestSize;
    private final DataSize userSpaceSize;

    @Autowired
    public MinioService(MinioRepository minioRepository,
                        BreadcrumbMapper breadcrumbMapper,
                        @Value("${spring.servlet.multipart.max-file-size}") DataSize maxFileSize,
                        @Value("${spring.servlet.multipart.max-request-size}") DataSize maxRequestSize,
                        @Value("${spring.application.user-space-size}") DataSize userSpaceSize) {
        this.minioRepository = minioRepository;
        this.breadcrumbMapper = breadcrumbMapper;
        this.maxFileSize = maxFileSize.toBytes();
        this.maxRequestSize = maxRequestSize.toBytes();
        this.userSpaceSize = userSpaceSize;
    }


    public void upload(UploadDto uploadDto) {
        List<MultipartFile> files = uploadDto.getFiles();
        String fullPath = PathUtils.formFullPath(uploadDto);
        long totalSize = calcTotalSize(files);
        if (isTotalSizeExceeded(totalSize)) {
            throw new MaxUploadSizeExceededException(maxRequestSize);
        }
        if (isUserSpaceExceeded(uploadDto)) {
            throw new NotEnoughFreeSpaceException(userSpaceSize.toMegabytes());
        }

        try {
            if (files.size() == 1) {
                MultipartFile file = files.get(0);
                String fullObjPath = fullPath + file.getOriginalFilename();
                minioRepository.upload(fullObjPath, file.getInputStream(), file.getSize());
            } else {
                minioRepository.upload(multipartToSnowball(files, fullPath));
            }

            createIntermediateFolders(files, uploadDto.getUserId(), uploadDto.getPath());

        } catch (SuchFolderAlreadyExistsException e) {
            log.error("During uploading files an exception occurred: {}", e.getMessage());
            throw e;
        } catch (IOException | MinioServiceException e) {
            log.error("During uploading files an exception occurred: {}", e.getMessage());
            throw new MinioServiceException("Failed to upload files");
        }
    }


    public InputStreamResource download(BaseReqDto downloadDto) {
        String parentFolderPath = PathUtils.formFullPath(downloadDto);
        String fullObjPath = parentFolderPath + downloadDto.getObjName();

        if (!minioRepository.isObjectExist(fullObjPath) && !minioRepository.isObjectDir(fullObjPath)){
            throw new NoSuchObjectException(fullObjPath);
        }

        if (minioRepository.isObjectDir(fullObjPath)) {
            return new InputStreamResource(getZippedFolder(fullObjPath + "/", parentFolderPath));
        } else {
            if (!minioRepository.isObjectExist(fullObjPath)) {
                log.error("Attempted to download '{}' but it does not exist", fullObjPath);
                throw new NoSuchObjectException(fullObjPath);
            }
            return new InputStreamResource(minioRepository.get(fullObjPath));
        }

    }


    public void rename(RenameDto renameDto) {
        String fullPath = PathUtils.formFullPath(renameDto);
        String objName = renameDto.getObjName();
        String newObjName = renameDto.getNewObjName();
        String fullObjPath = fullPath + objName;
        String fullNewObjPath = fullPath + newObjName;

        if (objName.equals(newObjName)) {
            return;
        }

        if (!minioRepository.isObjectExist(fullObjPath) && !minioRepository.isObjectDir(fullObjPath)){
            throw new MinioServiceException("Folder or file '%s' does not exist".formatted(objName));
        }

        if (minioRepository.isObjectExist(fullNewObjPath) || minioRepository.isObjectDir(fullNewObjPath)){
            throw new MinioServiceException("Folder or file '%s' already exists".formatted(newObjName));
        }

        if (isDir(fullObjPath)) {
            List<Item> objectsMeta = minioRepository.listRecursively(fullObjPath);
            for (Item item : objectsMeta) {
                String internalObjPath = item.objectName();
                String newInternalObjPath = fullNewObjPath + internalObjPath.substring(fullObjPath.length());
                minioRepository.rename(internalObjPath, newInternalObjPath);
            }

        } else {
            String newObjPath = fullPath + PathUtils.handleFileExtension(objName, newObjName);
            minioRepository.rename(fullObjPath, newObjPath);
        }
    }


    public void delete(BaseReqDto deleteDto) {
        String fullPath = PathUtils.formFullPath(deleteDto);
        String objName = deleteDto.getObjName();
        String fullObjPath = fullPath + objName;
        String folderSearchPath = (fullObjPath + "/").replace("//", "/");

        if (isDir(fullObjPath)) {
            List<DeleteObject> delObjects = new LinkedList<>();
            List<Item> objectsMeta = minioRepository.listRecursively(folderSearchPath);

            if (objectsMeta.isEmpty()) {
                log.warn("Attempted to delete folder '{}' but it does not exist", folderSearchPath);
                throw new NoSuchObjectException(folderSearchPath);
            }
            objectsMeta.forEach(x -> delObjects.add(new DeleteObject(x.objectName())));
            minioRepository.delete(delObjects);

        } else {

            if (!minioRepository.isObjectExist(fullObjPath)) {
                log.warn("Attempted to delete object '{}' but it does not exist", fullObjPath);
                throw new NoSuchObjectException(fullObjPath);
            }
            minioRepository.delete(fullObjPath);
        }

    }


    public List<BaseRespDto> list(BaseReqDto reqDto) {
        String fullPath = PathUtils.formFullPath(reqDto);
        List<BaseRespDto> objects = new ArrayList<>();
        for (Item obj : minioRepository.list(fullPath)) {
            if (isDir(obj) && obj.objectName().equals(fullPath)) {
                continue;
            }

            String objName = PathUtils.extractOrigName(obj.objectName().replaceAll("/$", ""));
            BaseRespDto objInfo = new BaseRespDto(reqDto.getUserId(), reqDto.getPath(), objName, obj.isDir());
            objects.add(objInfo);
        }

        objects.sort(new BaseRespDtoComparator());

        return objects;
    }


    public List<BaseRespDto> search(BaseReqDto searchDto) {
        String fullPath = PathUtils.formFullPath(searchDto);
        String query = searchDto.getObjName().trim().toLowerCase();
        List<BaseRespDto> objects = new ArrayList<>();
        List<Item> userObjects = minioRepository.listRecursively(fullPath);
        for (Item obj : userObjects) {

            if (isDir(obj) && obj.objectName().equals(fullPath)) {
                continue;
            }
            String shortObjName = breadcrumbMapper.mapToModel(obj.objectName()).getLastPart().toLowerCase();
            if (!shortObjName.contains(query)) {
                continue;
            }

            String objName = PathUtils.removeUserPrefix(obj.objectName().replaceAll("/$", ""));
            BaseRespDto resultDto = new BaseRespDto(searchDto.getUserId(), searchDto.getPath(), objName, obj.isDir());
            objects.add(resultDto);
        }

        return objects;
    }


    public boolean isObjectExist(ExtendedStorageDto checkDto) {
        String fullObjPath = PathUtils.formFullPath(checkDto) + checkDto.getObjName();
        return minioRepository.isObjectExist(fullObjPath);
    }


    public void createFolder(MkDirDto reqDto) {
        String fullObjPath = PathUtils.formFullPath(reqDto) + reqDto.getObjName() + "/";
        if (minioRepository.isObjectExist(fullObjPath)) {
            log.error("Attempt to create folder '{}' which already exists", fullObjPath);
            throw new SuchFolderAlreadyExistsException("Folder '%s' already exists".formatted(reqDto.getObjName()));
        }
        minioRepository.upload(fullObjPath, InputStream.nullInputStream(), 0);
    }


    public StorageInfo getStorageInfo(StorageDto reqDto) {
        DataSize usedSpace = DataSize.ofBytes(calcTotalStoredSize(reqDto));
        return new StorageInfo(usedSpace, userSpaceSize);
    }

    /**
     * Makes explicit folders from intermediate path parts
     *
     * @param files  - uploading files
     * @param userId - uploading user's id
     * @param path   - current path (folder)
     */
    private void createIntermediateFolders(List<MultipartFile> files, long userId, String path) {
        files.stream()
                .map(f -> breadcrumbMapper.mapToModel(f.getOriginalFilename()).getPathItems().values())
                .flatMap(Collection::stream)
                .filter(x -> !x.isEmpty())
                .map(s -> s.replaceFirst("^/", ""))
                .distinct()
                .map(p -> new MkDirDto(userId, path, p))
                .filter(dto -> !isDir(dto))
                .forEach(this::createFolder);
    }

    private ByteArrayInputStream getZippedFolder(String fullPath, String parentFolderPath) {
        ByteArrayOutputStream byteOutStream = new ByteArrayOutputStream();

        try (ZipOutputStream zipOutStream = new ZipOutputStream(byteOutStream)) {

            Map<Item, byte[]> objectsMap = minioRepository.getAfterPath(fullPath);

            for (Map.Entry<Item, byte[]> entry : objectsMap.entrySet()) {
                Item metaInfo = entry.getKey();
                byte[] buffer = entry.getValue();
                String name = PathUtils.extractNameFromPath(metaInfo.objectName(), parentFolderPath);
                zipOutStream.putNextEntry(new ZipEntry(name));
                zipOutStream.write(buffer);
                zipOutStream.closeEntry();
            }

        } catch (IOException e) {
            log.error("Error occurred while zipping folder '{}': {}", fullPath, e.getMessage());
            throw new MinioServiceException("Failed to download folder.");
        }

        return new ByteArrayInputStream(byteOutStream.toByteArray());
    }

    public boolean isDir(String fullObjPath) {
        return minioRepository.isObjectDir(fullObjPath);
    }

    public boolean isDir(ExtendedStorageDto checkDto) {
        String fullObjPath = PathUtils.formFullPath(checkDto) + checkDto.getObjName();
        return minioRepository.isObjectDir(fullObjPath);
    }

    private boolean isDir(Item objMeta) {
        return objMeta.isDir() || objMeta.objectName().endsWith("/");
    }

    private static List<SnowballObject> multipartToSnowball(List<MultipartFile> files, String fullPath) {
        return files.stream()
                .map(file -> {
                    try {
                        return new SnowballObject(
                                fullPath + file.getOriginalFilename(),
                                file.getInputStream(),
                                file.getSize(),
                                null);
                    } catch (IOException e) {
                        throw new MinioServiceException(e);
                    }
                })
                .toList();

    }


    private boolean isTotalSizeExceeded(long size) {
        return size > maxRequestSize || size > maxFileSize;
    }

    private boolean isUserSpaceExceeded(UploadDto uploadDto) {
        long uploadSize = calcTotalSize(uploadDto.getFiles());
        long totalStoredSize = calcTotalStoredSize(uploadDto);
        return totalStoredSize + uploadSize > userSpaceSize.toBytes();
    }

    private long calcTotalStoredSize(StorageDto reqDto) {
        String userFolder = PathUtils.formFullPath(reqDto);
        return minioRepository.listRecursively(userFolder).stream()
                .mapToLong(Item::size)
                .sum();
    }


    private long calcTotalSize(List<MultipartFile> files) {
        return files.stream().mapToLong(MultipartFile::getSize).sum();
    }


}
