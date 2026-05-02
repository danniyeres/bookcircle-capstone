package bookcircle.entity;

import bookcircle.domain.RoomMemberRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Setter
@Getter
@Table(name = "room_members", uniqueConstraints = {
        @UniqueConstraint(name = "uq_room_user", columnNames = {"room_id", "user_id"})
})
public class RoomMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Room room;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomMemberRole role = RoomMemberRole.MEMBER;

    @Column(nullable = false)
    private Instant joinedAt = Instant.now();


}
