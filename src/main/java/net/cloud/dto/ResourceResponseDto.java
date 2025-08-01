package net.cloud.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceResponseDto {

    @JsonProperty("filename")
    private String name;

    private Long size;

}
