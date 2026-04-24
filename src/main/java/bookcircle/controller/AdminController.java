package bookcircle.controller;


import bookcircle.dto.RoomStatsResponse;
import bookcircle.entity.RoomActivityStats;
import bookcircle.repo.AuditLogRepository;
import bookcircle.repo.RoomActivityStatsRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AuditLogRepository auditLogRepository;
    private final RoomActivityStatsRepository statsRepository;

    public AdminController(AuditLogRepository auditLogRepository, RoomActivityStatsRepository statsRepository) {
        this.auditLogRepository = auditLogRepository;
        this.statsRepository = statsRepository;
    }

    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public Object audit() {
        return auditLogRepository.findTop100ByOrderByTsDesc();
    }

    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @GetMapping("/stats/{roomId}")
    public RoomStatsResponse stats(@PathVariable Long roomId) {
        RoomActivityStats s = statsRepository.findById(roomId)
                .orElseGet(() -> {
                    RoomActivityStats empty = new RoomActivityStats();
                    empty.setRoomId(roomId);
                    return empty;
                });

        return new RoomStatsResponse(
                s.getRoomId(),
                s.getCommentsCount(),
                s.getProgressUpdatesCount(),
                s.getLastEventAt()
        );
    }
}
