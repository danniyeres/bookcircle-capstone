package bookcircle.service;

import bookcircle.domain.Role;
import bookcircle.dto.AuthDtos;
import bookcircle.entity.User;
import bookcircle.exception.ApiException;
import bookcircle.repo.UserRepository;
import bookcircle.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException(HttpStatus.CONFLICT, "Email already registered");
        }
        User u = new User();
        u.setEmail(req.email().toLowerCase());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setRole(Role.USER);
        u = userRepository.save(u);

        String token = jwtService.generateAccessToken(u);
        return new AuthDtos.AuthResponse(token, "Bearer", u.getId(), u.getRole().name());
    }

    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest req) {
        var u = userRepository.findByEmail(req.email().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String token = jwtService.generateAccessToken(u);
        return new AuthDtos.AuthResponse(token, "Bearer", u.getId(), u.getRole().name());
    }
}
