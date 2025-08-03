package net.cloud.service;

import net.cloud.security.JWTUtil;
import net.cloud.security.JwtBlackList;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class LogoutService {
    private final JWTUtil jwtUtil;
    private final JwtBlackList jwtBlackList;

    public LogoutService(JWTUtil jwtUtil, JwtBlackList jwtBlackList) {
        this.jwtUtil = jwtUtil;
        this.jwtBlackList = jwtBlackList;
    }

    public void logout(UserDetails usersDetails, String authHeader) {
        String authToken = jwtUtil.getAuthToken(authHeader);
        jwtBlackList.add(usersDetails.getUsername(), authToken);
    }
}
