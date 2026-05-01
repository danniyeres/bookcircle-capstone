package bookcircle.repo;

import bookcircle.entity.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {
    Optional<ReadingProgress> findByRoom_IdAndUser_Id(Long roomId, Long userId);
    void deleteByRoom_Id(Long roomId);
}
