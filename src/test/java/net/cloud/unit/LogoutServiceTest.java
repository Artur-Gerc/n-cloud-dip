package net.cloud.unit;

import net.cloud.model.User;
import net.cloud.security.JWTUtil;
import net.cloud.security.JwtBlackList;
import net.cloud.service.LogoutService;
import net.cloud.security.UsersDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class LogoutServiceTest {

    @Mock
    private JWTUtil jwtUtil;

    @Mock
    private JwtBlackList jwtBlackList;

    @InjectMocks
    private LogoutService logoutService;

    private UsersDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = UsersDetails.builder().user(new User(1L, "test-user", "password")).build();
    }

    @Test
    void logoutTest() {
        String authHeader = "Bearer abc123";
        String authToken = "abc123";
        Mockito.when(jwtUtil.getAuthToken(authHeader)).thenReturn(authToken);

        logoutService.logout(userDetails, authHeader);

        verify(jwtUtil, times(1)).getAuthToken(authHeader);
        verify(jwtBlackList,times(1)).add("test-user", authToken);
    }
}
