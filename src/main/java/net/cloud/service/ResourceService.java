package net.cloud.service;

import io.minio.StatObjectResponse;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import net.cloud.dto.ResourceResponseDto;
import net.cloud.exception.resourceException.InternalServerErrorException;
import net.cloud.exception.resourceException.NoDataException;
import net.cloud.exception.resourceException.ResourceExistException;
import net.cloud.exception.resourceException.UploadResourceException;
import net.cloud.model.FileStorage;
import net.cloud.util.PathUtil;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class ResourceService {

    private final MinioService minioService;

    public ResourceService(MinioService minioService) {
        this.minioService = minioService;
    }

    public void delete(FileStorage fileStorage) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        log.info("Start delete file {}", fileStorage.getFileName());

        validateFileStorage(fileStorage);

        String completePath = PathUtil.getCompletePath(fileStorage.getUserId(), fileStorage.getFileName());

        validateResourceExistence(completePath);

        try {
            minioService.delete(completePath);
        } catch (MinioException e) {
            throw new InternalServerErrorException("Error delete file");
        }

        log.info("Success delete file {}", fileStorage.getFileName());
    }

    public InputStreamResource download(FileStorage fileStorage) {

        log.info("Start download file {}", fileStorage.getFileName());

        validateFileStorage(fileStorage);

        String completePath = PathUtil.getCompletePath(fileStorage.getUserId(), fileStorage.getFileName());

        validateResourceExistence(completePath);

        return downloadFile(completePath);

    }

    private InputStreamResource downloadFile(String path) {
        try {

            InputStream object = minioService.downloadFile(path);

            log.info("Success download file {}", path);
            return new InputStreamResource(object);

        } catch (Exception e) {
            throw new InternalServerErrorException("Error downloading file", e);
        }

    }

    public ResourceResponseDto move(FileStorage fileStorage, String newFileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        Long userId = fileStorage.getUserId();
        String oldFileName = fileStorage.getFileName();

        validateFileStorage(fileStorage);

        log.info("Start move file {} to {}", oldFileName, newFileName);

        String completeOldPath = PathUtil.getCompletePath(userId, oldFileName);

        validateResourceExistence(completeOldPath);

        String completeNewPath = PathUtil.getCompletePath(userId, newFileName);

        minioService.copy(completeOldPath, completeNewPath);

        minioService.delete(completeOldPath);

        StatObjectResponse stat = minioService.getStatObject(completeNewPath);

        log.info("Success move file {} to {}", oldFileName, newFileName);

        return ResourceResponseDto.builder()
                .name(PathUtil.getFileName(completeNewPath))
                .size(stat.size())
                .build();
    }

    public ResourceResponseDto upload(FileStorage file) {

        log.info("Uploading file: {} for user: {}", file.getFileName(), file.getUserId());

        if (file.getFileContent() == null) {
            throw new NoDataException("File is empty");
        }

        StringBuilder sb = new StringBuilder();
        String userPath = PathUtil.getUserPath(file.getUserId());
        sb.append(userPath);

        ResourceResponseDto resourceResponseDto;

        String filePath = sb + file.getFileName();

        if (minioService.isResourceExists(filePath)) {
            throw new ResourceExistException("Resource already exists: " + file.getFileName());
        }

        try {
            minioService.uploadFile(filePath, file);

            resourceResponseDto = ResourceResponseDto.builder().name(file.getFileName()).size(file.getSize()).build();

        } catch (Exception e) {
            log.warn("Error uploading file", e);
            throw new UploadResourceException(e.getMessage());
        }

        log.info("File uploaded successfully: {} for user: {}", file.getFileName(), file.getUserId());

        return resourceResponseDto;
    }

    private void validateResourceExistence(String oldCompletePath) {
        boolean isDirectory = PathUtil.isDirectory(oldCompletePath);

        if (!isDirectory && !minioService.isResourceExists(oldCompletePath)) {
            throw new NoDataException("Resource does not exist");
        }

        if (isDirectory && !minioService.isFolderExists(oldCompletePath)) {
            throw new NoDataException("Directory does not exist");
        }
    }

    private void validateFileStorage(FileStorage fileStorage) {
        if (fileStorage.getUserId() == null)
            throw new NoDataException("User id is empty");
        if (fileStorage.getFileName() == null || fileStorage.getFileName().isBlank())
            throw new NoDataException("File name is empty");
    }
}
