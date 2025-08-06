package net.cloud.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DirectoryStorage {

    private Integer limit;
    private Long userId;
}
