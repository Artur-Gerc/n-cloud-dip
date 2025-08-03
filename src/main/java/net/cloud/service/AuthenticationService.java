package net.cloud.service;

import net.cloud.dto.AuthenticationResponse;
import net.cloud.exception.authException.UserNotFoundOrPasswordIncorrectException;
import net.cloud.model.User;
import net.cloud.security.JWTUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    public AuthenticationService(JWTUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse authenticate(User user) throws AuthenticationException {
        Authentication authentication;
        try {
            authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            user.getUsername(), user.getPassword()));

        } catch (AuthenticationException e) {
            throw new UserNotFoundOrPasswordIncorrectException("Username or password is incorrect");
        }

        SecurityContext context = securityContextHolderStrategy.createEmptyContext();
        context.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(context);

        return new AuthenticationResponse(jwtUtil.generateToken(user.getUsername()));
    }
}
