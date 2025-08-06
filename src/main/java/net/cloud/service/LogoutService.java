package net.cloud.service;

import lombok.extern.slf4j.Slf4j;
import net.cloud.security.JWTUtil;
import net.cloud.security.JwtBlackList;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogoutService {
    private final JWTUtil jwtUtil;
    private final JwtBlackList jwtBlackList;

    public LogoutService(JWTUtil jwtUtil, JwtBlackList jwtBlackList) {
        this.jwtUtil = jwtUtil;
        this.jwtBlackList = jwtBlackList;
    }

    public void logout(UserDetails usersDetails, String authHeader) {
        log.info("Logout user: {}", usersDetails.getUsername());
        String authToken = jwtUtil.getAuthToken(authHeader);
        jwtBlackList.add(usersDetails.getUsername(), authToken);
    }
}
