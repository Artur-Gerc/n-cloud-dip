package net.cloud.service;

import io.minio.StatObjectResponse;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import net.cloud.dto.ResourceResponseDto;
import net.cloud.exception.resourceException.InternalServerErrorException;
import net.cloud.exception.resourceException.NoDataException;
import net.cloud.exception.resourceException.ResourceExistException;
import net.cloud.exception.resourceException.UploadResourceException;
import net.cloud.util.PathUtil;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public void delete(String path, Long userId) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        String completePath = PathUtil.getCompletePath(userId, path);

        validateResourceExistence(completePath);

        try {
            minioService.delete(completePath);
        } catch (MinioException e) {
            throw new InternalServerErrorException("Error delete file");
        }

    }

    public InputStreamResource download(String filename, Long userId) {
        String completePath = PathUtil.getCompletePath(userId, filename);

        validateResourceExistence(completePath);

        return downloadFile(completePath);

    }

    private InputStreamResource downloadFile(String path) {
        try {

            InputStream object = minioService.downloadFile(path);

            return new InputStreamResource(object);
        } catch (Exception e) {
            throw new InternalServerErrorException("Error downloading file", e);
        }
    }

    public ResourceResponseDto move(String oldPath, String newPath, Long personId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String completeOldPath = PathUtil.getCompletePath(personId, oldPath);

        validateResourceExistence(completeOldPath);

        String completeNewPath = PathUtil.getCompletePath(personId, newPath);

        minioService.copy(completeOldPath, completeNewPath);

        minioService.delete(completeOldPath);

        StatObjectResponse stat = minioService.getStatObject(completeNewPath);
        return ResourceResponseDto.builder()
                .name(PathUtil.getFileName(completeNewPath))
                .size(stat.size())
                .build();
    }

    public ResourceResponseDto upload(Long personId, MultipartFile multipartFile, String fileName) {

        StringBuilder sb = new StringBuilder();
        String userPath = PathUtil.getUserPath(personId);
        sb.append(userPath);

        ResourceResponseDto resourceResponseDto;

        String filePath = sb + fileName;

        if (minioService.isResourceExists(filePath)) {
            throw new ResourceExistException("Resource already exists: " + fileName);
        }

        try {
            minioService.uploadFile(filePath, multipartFile);

            StatObjectResponse stat = minioService.getStatObject(filePath);
            resourceResponseDto = ResourceResponseDto.builder().name(fileName).size(stat.size()).build();

        } catch (Exception e) {
            log.warn("Error uploading file", e);
            throw new UploadResourceException(e.getMessage());
        }

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
}
