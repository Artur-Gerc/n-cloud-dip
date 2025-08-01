package net.cloud.service;

import jakarta.annotation.PostConstruct;
import net.cloud.model.User;
import net.cloud.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TestUserRegistrationService {

    private final RegistrationService registrationService;
    private final UserRepository userRepository;

    public TestUserRegistrationService(RegistrationService registrationService, UserRepository userRepository) {
        this.registrationService = registrationService;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void initTestUser() {
        String userName = "test-user";
        Optional<User> person = userRepository.findByUsername(userName);
        if (person.isEmpty()) {
            User user = new User();
            user.setUsername(userName);
            user.setPassword("password");

            registrationService.registerUser(user);
        }
    }
}
