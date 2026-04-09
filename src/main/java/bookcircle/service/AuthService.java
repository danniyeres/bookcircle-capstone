package bookcircle.service;

import bookcircle.domain.Role;
import bookcircle.dto.AuthDtos;
import bookcircle.entity.User;
import bookcircle.exception.ApiException;
import bookcircle.repo.UserRepository;
import bookcircle.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
        String normalizedEmail = req.email().toLowerCase();
        log.info("Register attempt email={}", normalizedEmail);

        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("Register rejected: email already exists email={}", normalizedEmail);
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }
        User u = new User();
        u.setEmail(normalizedEmail);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRole(Role.USER);
        u = userRepository.save(u);

        String token = jwtService.generateAccessToken(u);
        log.info("User registered userId={} email={}", u.getId(), u.getEmail());
        return new AuthDtos.AuthResponse(token, "Bearer", u.getId(), u.getRole().name());
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        String normalizedEmail = req.email().toLowerCase();
        log.info("Login attempt email={}", normalizedEmail);

        User u = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Login rejected: user not found email={}", normalizedEmail);
                    return new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                });
        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            log.warn("Login rejected: password mismatch userId={} email={}", u.getId(), normalizedEmail);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String token = jwtService.generateAccessToken(u);
        log.info("Login success userId={} role={}", u.getId(), u.getRole());
        return new AuthDtos.AuthResponse(token, "Bearer", u.getId(), u.getRole().name());
    }
}
