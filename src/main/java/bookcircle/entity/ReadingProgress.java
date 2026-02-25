package bookcircle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Setter
@Getter
@Table(name = "reading_progress", uniqueConstraints = {
        @UniqueConstraint(name = "uq_progress_room_user", columnNames = {"room_id", "user_id"})
})
public class ReadingProgress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Room room;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private int chapterNumber;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();


}
