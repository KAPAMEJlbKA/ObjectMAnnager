package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.model.UserAccount;
import com.kapamejlbka.objectmanager.repository.UserAccountRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserAccountRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserAccount> findAll() {
        return userRepository.findAll();
    }

    public UserAccount findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    @Transactional
    public UserAccount createUser(String username, String rawPassword, boolean admin) {
        UserAccount user = new UserAccount();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.addRole("ROLE_USER");
        if (admin) {
            user.addRole("ROLE_ADMIN");
        }
        return userRepository.save(user);
    }

    public void ensureDefaultUsers() {
        if (userRepository.findByUsernameIgnoreCase("admin").isEmpty()) {
            createUser("admin", "admin", true);
        }
        if (userRepository.findByUsernameIgnoreCase("user").isEmpty()) {
            createUser("user", "user", false);
        }
    }
}
