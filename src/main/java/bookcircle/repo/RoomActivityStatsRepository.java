package bookcircle.repo;

import bookcircle.entity.RoomActivityStats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Map;
import java.util.Optional;

public interface RoomActivityStatsRepository extends JpaRepository<RoomActivityStats, Long> {

}
