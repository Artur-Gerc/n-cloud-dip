package net.cloud.controller;

import io.minio.errors.*;
import net.cloud.dto.NewFileNameDto;
import net.cloud.dto.ResourceResponseDto;
import net.cloud.model.FileStorage;
import net.cloud.security.UsersDetails;
import net.cloud.service.ResourceService;
import net.cloud.util.PathUtil;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/file")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResponseEntity<ResourceResponseDto> upload(@RequestParam(value = "filename") String filename,
                                                      @AuthenticationPrincipal UsersDetails personDetails,
                                                      @RequestPart("file") MultipartFile file) throws IOException {

        FileStorage fileStorage = FileStorage.builder()
                .fileName(filename)
                .fileContent(file.getBytes())
                .fileType(file.getContentType())
                .size(file.getSize())
                .userId(personDetails.getUser().getId())
                .build();

        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(resourceService.upload(fileStorage));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteResource(@RequestParam(value = "filename") String filename,
                                            @AuthenticationPrincipal UsersDetails personDetails) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        FileStorage fileStorage = FileStorage.builder()
                .fileName(filename)
                .userId(personDetails.getUser().getId())
                .build();

        resourceService.delete(fileStorage);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> downloadResource(@RequestParam("filename") String filename,
                                              @AuthenticationPrincipal UsersDetails personDetails) {


        FileStorage fileStorage = FileStorage.builder()
                .fileName(filename)
                .userId(personDetails.getUser().getId())
                .build();

        InputStreamResource isr = resourceService.download(fileStorage);
        String fileName = PathUtil.getNameForDownload(filename);

        ContentDisposition contentDisposition = ContentDisposition
                .builder("attachment")
                .filename(fileName)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .body(isr);
    }

    @PutMapping
    public ResponseEntity<ResourceResponseDto> moveResource(@RequestParam("filename") String filename,
                                                            @RequestBody NewFileNameDto newFileNameDto,
                                                            @AuthenticationPrincipal UsersDetails personDetails) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        FileStorage fileStorage = FileStorage.builder()
                .fileName(filename)
                .userId(personDetails.getUser().getId())
                .build();

        return ResponseEntity.ok().body(resourceService.move(fileStorage, newFileNameDto.getFileName()));
    }
}
