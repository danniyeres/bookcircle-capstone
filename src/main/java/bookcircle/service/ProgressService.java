package bookcircle.service;

import bookcircle.dto.ProgressDtos;
import bookcircle.entity.ReadingProgress;
import bookcircle.events.ProgressUpdatedEvent;
import bookcircle.exception.ApiException;
import bookcircle.repo.ReadingProgressRepository;
import bookcircle.repo.RoomMemberRepository;
import bookcircle.repo.RoomRepository;
import bookcircle.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final ReadingProgressRepository progressRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher publisher;



    @Transactional
    public ProgressDtos.ProgressResponse updateProgress(Long actorUserId, ProgressDtos.UpdateProgressRequest req) {
        var room = roomRepository.findById(req.roomId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));

        if (roomMemberRepository.findByRoom_IdAndUser_Id(room.getId(), actorUserId).isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not a member of this room");
        }

        var user = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));

        ReadingProgress p = progressRepository.findByRoom_IdAndUser_Id(room.getId(), actorUserId)
                .orElseGet(() -> {
                    ReadingProgress np = new ReadingProgress();
                    np.setRoom(room);
                    np.setUser(user);
                    return np;
                });

        p.setChapterNumber(req.chapterNumber());
        p.setUpdatedAt(Instant.now());
        progressRepository.save(p);

        auditService.log(actorUserId, "PROGRESS_UPDATED", "ReadingProgress", p.getId(), "roomId=" + room.getId());
        publisher.publishEvent(new ProgressUpdatedEvent(room.getId(), actorUserId));

        return new ProgressDtos.ProgressResponse(room.getId(), actorUserId, p.getChapterNumber());
    }

    public int getCurrentChapter(Long roomId, Long userId) {
        return progressRepository.findByRoom_IdAndUser_Id(roomId, userId)
                .map(ReadingProgress::getChapterNumber)
                .orElse(0);
    }
}
