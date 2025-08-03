package net.cloud.controller;

import jakarta.validation.Valid;
import net.cloud.dto.AuthenticationRequest;
import net.cloud.dto.AuthenticationResponse;
import net.cloud.model.User;
import net.cloud.service.AuthenticationService;
import net.cloud.service.LogoutService;
import net.cloud.service.RegistrationService;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private final ModelMapper modelMapper;
    private final RegistrationService registrationService;
    private final AuthenticationService authenticationService;
    private final LogoutService logoutService;

    public AuthController(ModelMapper modelMapper, RegistrationService registrationService, AuthenticationService authenticationService, LogoutService logoutService) {
        this.modelMapper = modelMapper;
        this.registrationService = registrationService;
        this.authenticationService = authenticationService;
        this.logoutService = logoutService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<AuthenticationResponse> registerUser(@RequestBody @Valid AuthenticationRequest authenticationRequest) {

        User user = convertAuthenticationRequestToUser(authenticationRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthenticationResponse(registrationService.registerUser(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody @Valid AuthenticationRequest authenticationRequest) {

        return ResponseEntity.ok(authenticationService.authenticate(convertAuthenticationRequestToUser(authenticationRequest)));
    }

    @PostMapping("/logout")
    public ResponseEntity logout(@RequestHeader(value = "auth-token", required = false) String authHeader,
                                 @AuthenticationPrincipal UserDetails userDetails) {

        logoutService.logout(userDetails, authHeader);

        return new ResponseEntity(HttpStatus.OK);
    }

    public User convertAuthenticationRequestToUser(AuthenticationRequest authenticationRequest) {
        return modelMapper.map(authenticationRequest, User.class);
    }
}
