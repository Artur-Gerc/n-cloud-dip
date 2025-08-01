package net.cloud.unit;

import net.cloud.exception.authException.UserExistsException;
import net.cloud.model.User;
import net.cloud.repository.UserRepository;
import net.cloud.security.JWTUtil;
import net.cloud.service.RegistrationService;
import net.cloud.service.DirectoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestUserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private DirectoryService directoryService;

    @InjectMocks
    private RegistrationService registrationService;

    User user;

    @BeforeEach
    public void setUp() {
        user = new User(1L, "test-user", "test-password");
    }

    @Test
    public void testRegisterUser() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());

        when(passwordEncoder.encode(user.getPassword())).thenReturn("encoded-password");
        when(userRepository.save(user)).thenReturn(user);
        when(jwtUtil.generateToken(user.getUsername())).thenReturn("token");

        String token = registrationService.registerUser(user);

        Assertions.assertNotNull(token);
        Assertions.assertEquals("token", token);

        verify(userRepository, times(1)).findByUsername(user.getUsername());
        verify(jwtUtil, times(1)).generateToken(user.getUsername());
        verify(passwordEncoder, times(1)).encode("test-password");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testRegisterExistsUser() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.ofNullable(user));

        UserExistsException ex = Assertions.assertThrows(UserExistsException.class, () -> registrationService.registerUser(user));
        Assertions.assertEquals("User with username: " + user.getUsername() + " already exists", ex.getMessage());
    }
}
