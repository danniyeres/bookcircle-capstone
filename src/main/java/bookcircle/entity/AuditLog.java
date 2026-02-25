package bookcircle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Setter
@Getter
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long actorUserId;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String entityType;

    @Column
    private Long entityId;

    @Column(nullable = false)
    private Instant ts = Instant.now();

    @Column(length = 2000)
    private String details;


}
