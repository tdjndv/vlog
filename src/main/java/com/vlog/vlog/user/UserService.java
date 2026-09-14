package com.vlog.vlog.user;

import com.vlog.vlog.common.NotFoundException;
import com.vlog.vlog.user.dto.CreateUserRequest;
import com.vlog.vlog.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> findAll() {
        return repository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse findById(Long id) {
        return repository.findById(id)
                .map(UserResponse::from)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    public UserResponse findByUsername(String username) {
        return repository.findByUsername(username)
                .map(UserResponse::from)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    /** Returns the entity, not a DTO. For internal use by other services. */
    public User getEntityByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (repository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already taken: " + request.username());
        }
        if (repository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered: " + request.email());
        }

        String hash = passwordEncoder.encode(request.password());
        Role role = request.role() != null ? request.role() : Role.USER;

        User user = new User(request.username(), request.email(), hash, role);
        return UserResponse.from(repository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("User not found: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional
    public UserResponse createStandardUser(CreateUserRequest request) {
        CreateUserRequest forced = new CreateUserRequest(
                request.username(), request.email(), request.password(), Role.USER);
        return create(forced);
    }
}