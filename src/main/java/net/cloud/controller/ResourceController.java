package net.cloud.controller;

import io.minio.errors.*;
import net.cloud.dto.NewFileNameDto;
import net.cloud.dto.ResourceResponseDto;
import net.cloud.security.UsersDetails;
import net.cloud.service.ResourceService;
import net.cloud.util.PathUtil;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
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
                                                      @RequestPart("file") MultipartFile file) {

        return ResponseEntity.status(HttpStatusCode.valueOf(200)).body(resourceService.upload(personDetails.getUser().getId(), file, filename));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteResource(@RequestParam(value = "filename") String filename,
                                            @AuthenticationPrincipal UsersDetails personDetails) throws IOException, NoSuchAlgorithmException, InvalidKeyException {

        resourceService.delete(filename, personDetails.getUser().getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<?> downloadResource(@RequestParam("filename") String filename,
                                              @AuthenticationPrincipal UsersDetails personDetails) {

        InputStreamResource isr = resourceService.download(filename, personDetails.getUser().getId());
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

        return ResponseEntity.ok().body(resourceService.move(filename, newFileNameDto.getFileName(), personDetails.getUser().getId()));
    }
}
