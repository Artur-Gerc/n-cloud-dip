package net.cloud.service;

import io.minio.Result;
import io.minio.StatObjectResponse;
import io.minio.errors.*;
import io.minio.messages.Item;
import net.cloud.dto.ResourceResponseDto;
import net.cloud.exception.resourceException.InvalidDataException;
import net.cloud.exception.resourceException.NoDataException;
import net.cloud.util.PathUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DirectoryService {

    private final MinioService minioService;

    public DirectoryService(MinioService minioService) {
        this.minioService = minioService;
    }


    public void createUserDirectory(Long userId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        String userPath = PathUtil.getUserPath(userId);

        boolean isUserFolderExists = minioService.isFolderExists(userPath);

        if (!isUserFolderExists) {
            minioService.directoryCreate(userPath);
        }
    }

    public List<ResourceResponseDto> getDirectoryItems(String limit, Long personId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        if (limit.chars().anyMatch(Character::isLetter)) {
            throw new InvalidDataException("The limit must be indicated as a number");
        }

        int dataLimit = Integer.parseInt(limit);
        String completePath = PathUtil.getUserPath(personId);

        if (!minioService.isFolderExists(completePath)) {
            throw new NoDataException("Directory does not exist");
        }

        Iterable<Result<Item>> results = minioService.getListObject(completePath, false);

        List<ResourceResponseDto> foundElements = new ArrayList<>();
        for (Result<Item> item : results) {

            if (foundElements.size() >= dataLimit) {
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
        return foundElements;
    }
}
