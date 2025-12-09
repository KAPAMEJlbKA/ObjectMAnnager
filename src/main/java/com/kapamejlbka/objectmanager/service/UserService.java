package com.kapamejlbka.objectmanager.service;

import com.kapamejlbka.objectmanager.domain.user.AppRole;
import com.kapamejlbka.objectmanager.domain.user.AppUser;
import com.kapamejlbka.objectmanager.domain.user.dto.UserCreateRequest;
import com.kapamejlbka.objectmanager.domain.user.dto.UserUpdateRequest;
import com.kapamejlbka.objectmanager.domain.user.repository.AppRoleRepository;
import com.kapamejlbka.objectmanager.domain.user.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final AppRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            AppUserRepository userRepository,
            AppRoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AppUser> findAll() {
        return userRepository.findAll();
    }

    public List<AppRole> findAllRoles() {
        return roleRepository.findAll();
    }

    public Optional<AppUser> findByUsername(String username) {
        return userRepository.findByUsernameIgnoreCase(username);
    }

    public AppUser getByUsername(String username) {
        return findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    public AppUser getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    @Transactional
    public AppUser createUser(UserCreateRequest dto) {
        validateUsername(dto.getUsername(), null);
        validateEmail(dto.getEmail(), null);
        AppUser user = new AppUser();
        user.setUsername(dto.getUsername());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setEnabled(dto.isEnabled());

        Set<String> roleNames = dto.getRoles().isEmpty()
                ? Set.of("VIEWER")
                : dto.getRoles();
        Set<AppRole> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roles.add(ensureRole(roleName));
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Transactional
    public AppUser createUser(String username, String rawPassword, boolean admin) {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername(username);
        request.setPassword(rawPassword);
        request.setFullName(username);
        request.setEmail(username + "@example.com");
        request.setEnabled(true);
        request.setRoles(admin ? Set.of("ADMIN") : Set.of("VIEWER"));
        return createUser(request);
    }

    @Transactional
    public AppUser updateUser(Long id, UserUpdateRequest dto) {
        AppUser user = getById(id);
        validateEmail(dto.getEmail(), id);

        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setEnabled(dto.isEnabled());

        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        }

        Set<String> roleNames = dto.getRoles().isEmpty()
                ? Set.of("VIEWER")
                : dto.getRoles();
        Set<AppRole> roles = new HashSet<>();
        for (String roleName : roleNames) {
            roles.add(ensureRole(roleName));
        }
        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Transactional
    public AppUser updateStatus(Long id, boolean enabled) {
        AppUser user = getById(id);
        user.setEnabled(enabled);
        return userRepository.save(user);
    }

    @Transactional
    public void createAdminIfNotExists() {
        ensureRole("ADMIN");
        ensureRole("ENGINEER");
        ensureRole("VIEWER");
        if (userRepository.count() == 0) {
            createUser("admin", "admin", true);
        }
    }

    @Transactional
    public void ensureDefaultUsers() {
        ensureRole("ADMIN");
        ensureRole("ENGINEER");
        ensureRole("VIEWER");
        if (userRepository.findByUsernameIgnoreCase("admin").isEmpty()) {
            createUser("admin", "admin", true);
        }
        if (userRepository.findByUsernameIgnoreCase("engineer").isEmpty()) {
            createUser("engineer", "engineer", false);
        }
        if (userRepository.findByUsernameIgnoreCase("viewer").isEmpty()) {
            UserCreateRequest viewer = new UserCreateRequest();
            viewer.setUsername("viewer");
            viewer.setPassword("viewer");
            viewer.setFullName("Viewer");
            viewer.setEmail("viewer@example.com");
            viewer.setRoles(Set.of("VIEWER"));
            createUser(viewer);
        }
    }

    private void validateUsername(String username, Long excludeId) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Логин не может быть пустым");
        }
        userRepository.findByUsernameIgnoreCase(username)
                .filter(user -> excludeId == null || !excludeId.equals(user.getId()))
                .ifPresent(user -> { throw new IllegalArgumentException("Пользователь с таким логином уже существует"); });
    }

    private void validateEmail(String email, Long excludeId) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        userRepository.findByEmailIgnoreCase(email)
                .filter(user -> excludeId == null || !excludeId.equals(user.getId()))
                .ifPresent(user -> { throw new IllegalArgumentException("Пользователь с таким email уже существует"); });
    }

    private AppRole ensureRole(String name) {
        String normalized = name == null ? "" : name.trim().toUpperCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Role name must not be empty");
        }
        return roleRepository.findByNameIgnoreCase(normalized)
                .orElseGet(() -> roleRepository.save(new AppRole(normalized, null)));
    }
}
