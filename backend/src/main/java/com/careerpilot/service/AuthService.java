
package com.careerpilot.service;

import com.careerpilot.dto.AuthResponse;
import com.careerpilot.dto.LoginRequest;
import com.careerpilot.dto.RegisterRequest;
import com.careerpilot.entity.User;
import com.careerpilot.exception.ConflictException;
import com.careerpilot.exception.ResourceNotFoundException;
import com.careerpilot.repository.UserRepository;
import com.careerpilot.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(
                    "An account with this email already exists");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.saveAndFlush(user);

        String token = jwtService.generateToken(savedUser.getEmail());

        return new AuthResponse(
                token,
                savedUser.getEmail(),
                savedUser.getFullName());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Invalid email or password"));

        String rawPassword = request.password();

        if (rawPassword == null
                || !passwordEncoder.matches(
                        rawPassword, user.getPassword())) {
            throw new BadCredentialsException(
                    "Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFullName());
    }

    @Transactional(readOnly = true)
    public User getByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }

    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadCredentialsException(
                    "Invalid email or password");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}
