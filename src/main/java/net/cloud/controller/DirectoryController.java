package net.cloud.controller;

import io.minio.errors.*;
import net.cloud.security.UsersDetails;
import net.cloud.service.DirectoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
public class DirectoryController {

    private final DirectoryService directoryService;

    public DirectoryController(DirectoryService directoryService) {
        this.directoryService = directoryService;
    }

    @GetMapping("/list")
    public ResponseEntity<?> getDirectoryItems(@RequestParam(value = "limit") String limit,
                                               @AuthenticationPrincipal UsersDetails personDetails) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        return ResponseEntity.ok().body(directoryService.getDirectoryItems(limit, personDetails.getUser().getId()));
    }
}
