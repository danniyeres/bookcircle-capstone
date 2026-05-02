package bookcircle.controller;


import bookcircle.dto.AdminDtos;
import bookcircle.dto.RoomStatsResponse;
import bookcircle.entity.RoomActivityStats;
import bookcircle.repo.AuditLogRepository;
import bookcircle.repo.RoomActivityStatsRepository;
import bookcircle.service.AuthService;
import bookcircle.util.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AuditLogRepository auditLogRepository;
    private final RoomActivityStatsRepository statsRepository;
    private final AuthService authService;

    public AdminController(
            AuditLogRepository auditLogRepository,
            RoomActivityStatsRepository statsRepository,
            AuthService authService
    ) {
        this.auditLogRepository = auditLogRepository;
        this.statsRepository = statsRepository;
        this.authService = authService;
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

    @PatchMapping("/users/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminDtos.UserRoleResponse updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody AdminDtos.UpdateUserRoleRequest req
    ) {
        return authService.updateUserRole(AuthUtil.principal().userId(), userId, req.role());
    }
}
