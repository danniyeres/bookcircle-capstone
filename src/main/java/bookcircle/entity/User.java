package bookcircle.entity;

import bookcircle.domain.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Setter
@Getter
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(unique = true, length = 64)
    private String nickname;

    @Column(name = "phone_number", unique = true, length = 32)
    private String phoneNumber;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(name = "home_h3")
    private String homeH3;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

}
