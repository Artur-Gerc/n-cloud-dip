package net.cloud.service;

import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.errors.*;
import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import net.cloud.dto.ResourceResponseDto;
import net.cloud.exception.resourceException.NoDataException;
import net.cloud.exception.resourceException.ResourceExistException;
import net.cloud.model.DirectoryStorage;
import net.cloud.util.PathUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DirectoryService {

    private final MinioService minioService;

    public DirectoryService(MinioService minioService) {
        this.minioService = minioService;
    }


    public void createUserDirectory(Long userId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        log.info("Creating user directory for user {}", userId);

        String userPath = PathUtil.getUserPath(userId);

        boolean isUserFolderExists = minioService.isFolderExists(userPath);

        if (!isUserFolderExists) {
            minioService.directoryCreate(userPath);
            log.info("Directory created: {}", userPath);
        } else {
            log.warn("Directory for user: {} already exists", userId);
            throw new ResourceExistException("Directory for user " + userId + " already exists");
        }

        log.info("Directory created: {}", userPath);
    }

    public List<ResourceResponseDto> getDirectoryItems(DirectoryStorage directoryStorage) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        Long userId = directoryStorage.getUserId();

        String completePath = PathUtil.getUserPath(userId);

        if (!minioService.isFolderExists(completePath)) {
            throw new NoDataException("Directory does not exist");
        }

        Iterable<Result<Item>> results = minioService.getListObject(completePath, false);

        List<ResourceResponseDto> foundElements = new ArrayList<>();
        for (Result<Item> item : results) {

            if (foundElements.size() >= directoryStorage.getLimit()) {
                return foundElements;
            }

            String fileName = item.get().objectName();

            boolean isDirectory = PathUtil.isDirectory(fileName);
            if (!isDirectory) {
                StatObjectResponse statObjectResponse = minioService.getStatObject(fileName);
                foundElements.add(ResourceResponseDto.builder()
                        .name(PathUtil.getFileName(fileName))
                        .size(statObjectResponse.size())
                        .build());
            } else {
                String directoryName = PathUtil.getDirectoryName(fileName);

                if (completePath.equals(fileName)) {
                    continue;
                }

                foundElements.add(ResourceResponseDto.builder()
                        .name(directoryName)
                        .build());
            }
        }

        log.info("Getting directory items for user {}", directoryStorage.getUserId());

        return foundElements;
    }
}
