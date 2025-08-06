package net.cloud.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class FileStorage {

    private String fileName;
    private String fileType;
    private Long size;
    private byte[] fileContent;
    private Long userId;
}
