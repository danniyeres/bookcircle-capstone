package bookcircle.util;

import bookcircle.domain.Role;
import bookcircle.entity.User;
import bookcircle.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String email = "admin@bookcircle";
        if (!userRepository.existsByEmail(email)) {
            User u = new User();
            u.setEmail(email);
            u.setPasswordHash(passwordEncoder.encode("admin123"));
            u.setRole(Role.ADMIN);
            userRepository.save(u);
        }
    }
}
