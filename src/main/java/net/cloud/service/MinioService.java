package net.cloud.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import net.cloud.config.MinioConfig;
import net.cloud.exception.resourceException.NoDataException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final String homeBucket;

    public MinioService(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
        this.homeBucket = minioConfig.getBucketName();
    }

    @PostConstruct
    public void initHomeBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        boolean isBucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build());

        if (!isBucketExists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(homeBucket).build());
        }


    }

    public void uploadFile(String fileName, MultipartFile multipartFile) {

        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(homeBucket)
                    .object(fileName)
                    .stream(multipartFile.getInputStream(), multipartFile.getSize(), -1)
                    .contentType(multipartFile.getContentType())
                    .build());

        } catch (MinioException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isFolderExists(String folder) {

        Iterable<Result<Item>> results = minioClient
                .listObjects(ListObjectsArgs
                        .builder()
                        .bucket(homeBucket)
                        .prefix(folder)
                        .build());

        return results.iterator().hasNext();
    }

    public boolean isResourceExists(String resource) {
        try {
            getStatObject(resource);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void directoryCreate(String directory) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(homeBucket)
                .object(directory)
                .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                .build());
    }

    public StatObjectResponse getStatObject(String objectPath) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        try {
            return minioClient.statObject(StatObjectArgs
                    .builder()
                    .bucket(homeBucket)
                    .object(objectPath)
                    .build());
        } catch (MinioException e) {
            throw new NoDataException(e.getMessage());
        }
    }

    public void delete(String objectPath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeObject(RemoveObjectArgs
                .builder()
                .bucket(homeBucket)
                .object(objectPath)
                .build());
    }

    public Iterable<Result<Item>> getListObject(String objectPath, boolean recursive){
        return minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(homeBucket)
                .prefix(objectPath)
                .recursive(recursive)
                .build());
    }

    public InputStream downloadFile(String filePath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.getObject(GetObjectArgs.builder().bucket(homeBucket).object(filePath).build());
    }

    public void copy(String oldCompletePath, String newCompletePath) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.copyObject(CopyObjectArgs.builder().bucket(homeBucket).object(newCompletePath)
                .source(CopySource.builder().bucket(homeBucket).object(oldCompletePath).build()).build());
    }
}
