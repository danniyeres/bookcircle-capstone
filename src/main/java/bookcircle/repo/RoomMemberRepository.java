package bookcircle.repo;

import bookcircle.entity.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    Optional<RoomMember> findByRoom_IdAndUser_Id(Long roomId, Long userId);
}
