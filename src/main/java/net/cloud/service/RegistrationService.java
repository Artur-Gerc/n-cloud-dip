package net.cloud.service;

import net.cloud.exception.authException.UserExistsException;
import net.cloud.model.User;
import net.cloud.repository.UserRepository;
import net.cloud.security.JWTUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RegistrationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final DirectoryService directoryService;


    public RegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTUtil jwtUtil, DirectoryService directoryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.directoryService = directoryService;
    }

    @Transactional
    public String registerUser(User user) {
        Optional<User> person = userRepository.findByUsername(user.getUsername());
        if (person.isPresent()) {
            throw new UserExistsException("User with username: " + user.getUsername() + " already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);

        try {
            directoryService.createUserDirectory(savedUser.getId());
        } catch (Exception e) {
            throw new RuntimeException("Create user directory failed: " + savedUser.getUsername());
        }

        return jwtUtil.generateToken(user.getUsername());
    }
}
