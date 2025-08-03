package net.cloud.unit;

import net.cloud.dto.AuthenticationRequest;
import net.cloud.dto.AuthenticationResponse;
import net.cloud.exception.authException.UserNotFoundOrPasswordIncorrectException;
import net.cloud.model.User;
import net.cloud.service.AuthenticationService;
import net.cloud.security.JWTUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(1L, "testuser", "password123");
    }

    @Test
    void authenticate() {
        // Arrange
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "testuser", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        when(jwtUtil.generateToken("testuser")).thenReturn("mocked-jwt-token");

        // Act
        AuthenticationResponse response = authenticationService.authenticate(user);

        // Assert
        assertThat(response.getAuthToken()).isEqualTo("mocked-jwt-token");
        verify(authenticationManager).authenticate(argThat(token ->
                "testuser".equals(token.getPrincipal()) &&
                        "password123".equals(token.getCredentials())
        ));
        verify(jwtUtil).generateToken("testuser");

        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isSameAs(authentication);
    }

    @Test
    void authenticateWithBadCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        UserNotFoundOrPasswordIncorrectException exception =
                Assertions.assertThrows(UserNotFoundOrPasswordIncorrectException.class, () -> authenticationService.authenticate(user));

        Assertions.assertEquals("Username or password is incorrect", exception.getMessage());

        SecurityContext context = SecurityContextHolder.getContext();
        assertThat(context.getAuthentication()).isNull();
    }

    
}
