package bookcircle.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Setter
@Getter
@Table(name = "room_activity_stats")
public class RoomActivityStats {
    @Id
    private Long roomId;

    @Column(nullable = false)
    private long commentsCount = 0;

    @Column(nullable = false)
    private long progressUpdatesCount = 0;

    @Column(nullable = false)
    private Instant lastEventAt = Instant.now();


}
