package net.cloud.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthenticationRequest {

    @NotBlank(message = "Login cannot be empty")
    @JsonProperty("login")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;
}
