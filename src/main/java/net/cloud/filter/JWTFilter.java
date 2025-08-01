package net.cloud.filter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.cloud.exception.authException.UnauthorizedRequestException;
import net.cloud.security.CustomAuthEntryPoint;
import net.cloud.security.JWTUtil;
import net.cloud.security.JwtBlackList;
import net.cloud.security.UsersDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UsersDetailsService usersDetailsService;
    private final JwtBlackList jwtBlackList;
    private final CustomAuthEntryPoint authenticationEntryPoint;

    public JWTFilter(JWTUtil jwtUtil, UsersDetailsService usersDetailsService, JwtBlackList jwtBlackList, CustomAuthEntryPoint authenticationEntryPoint) {
        this.jwtUtil = jwtUtil;
        this.usersDetailsService = usersDetailsService;
        this.jwtBlackList = jwtBlackList;
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = httpServletRequest.getHeader("auth-token");

        String path = httpServletRequest.getServletPath();

        if (!path.equals("/login") && !path.equals("/sign-up")) {
            if (authHeader == null || authHeader.isBlank()) {
                authenticationEntryPoint.commence(httpServletRequest, httpServletResponse,
                        new UnauthorizedRequestException("Missing JWT token"));
            }
        }

        if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
            String jwt = jwtUtil.getAuthToken(authHeader);

            if (jwt.isBlank()) {
                authenticationEntryPoint.commence(httpServletRequest, httpServletResponse, new UnauthorizedRequestException("Invalid JWT token in Bearer header"));
            } else {
                try {
                    String username = jwtUtil.verifyTokenAndRetrieveClaim(jwt);

                    if (jwtBlackList.containsUsernameAndToken(username, jwt)) {
                        throw new JWTVerificationException("JWT token in blacklist");
                    }

                    UserDetails userDetails = usersDetailsService.loadUserByUsername(username);


                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails,
                                    userDetails.getPassword(),
                                    userDetails.getAuthorities());

                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                } catch (JWTVerificationException e) {
                    authenticationEntryPoint.commence(httpServletRequest, httpServletResponse, new UnauthorizedRequestException("Invalid JWT token"));
                }
            }
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
