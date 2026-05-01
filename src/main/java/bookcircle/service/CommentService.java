package bookcircle.service;

import bookcircle.dto.CommentDtos;
import bookcircle.entity.Comment;
import bookcircle.events.CommentCreatedEvent;
import bookcircle.exception.ApiException;
import bookcircle.repo.CommentRepository;
import bookcircle.repo.RoomMemberRepository;
import bookcircle.repo.RoomRepository;
import bookcircle.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final Logger log = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;
    private final ProgressService progressService;
    private final AuditService auditService;
    private final ApplicationEventPublisher publisher;


    @Transactional
    public CommentDtos.CommentResponse create(Long actorUserId, CommentDtos.CreateCommentRequest req) {
        log.info("Create comment requested actorUserId={} roomId={} chapter={}",
                actorUserId, req.roomId(), req.chapterNumber());

        var room = roomRepository.findById(req.roomId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));

        if (roomMemberRepository.findByRoom_IdAndUser_Id(room.getId(), actorUserId).isEmpty()) {
            log.warn("Create comment rejected: user is not room member actorUserId={} roomId={}",
                    actorUserId, room.getId());
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not a member of this room");
        }

        var user = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));

        Comment c = new Comment();
        c.setRoom(room);
        c.setAuthor(user);
        c.setChapterNumber(req.chapterNumber());
        c.setContent(req.content());
        c = commentRepository.save(c);

        auditService.log(actorUserId, "COMMENT_CREATED", "Comment", c.getId(), "roomId=" + room.getId());
        publisher.publishEvent(new CommentCreatedEvent(room.getId(), actorUserId));
        log.info("Comment created commentId={} actorUserId={} roomId={}", c.getId(), actorUserId, room.getId());

        return new CommentDtos.CommentResponse(c.getId(), room.getId(), actorUserId, c.getChapterNumber(), c.getContent(), c.getCreatedAt());
    }

    /**
     * ABAC rule: comment is visible only if user's progress >= comment.chapterNumber
     */
    public List<CommentDtos.CommentResponse> getVisibleComments(Long actorUserId, Long roomId) {
        log.info("Visible comments requested actorUserId={} roomId={}", actorUserId, roomId);

        if (roomMemberRepository.findByRoom_IdAndUser_Id(roomId, actorUserId).isEmpty()) {
            log.warn("Visible comments rejected: user is not room member actorUserId={} roomId={}",
                    actorUserId, roomId);
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not a member of this room");
        }

        int current = progressService.getCurrentChapter(roomId, actorUserId);
        var visibleComments = commentRepository.findByRoom_IdOrderByCreatedAtAsc(roomId).stream()
                .filter(c -> c.getChapterNumber() <= current)
                .map(c -> new CommentDtos.CommentResponse(
                        c.getId(),
                        roomId,
                        c.getAuthor().getId(),
                        c.getChapterNumber(),
                        c.getContent(),
                        c.getCreatedAt()
                ))
                .toList();
        log.info("Visible comments fetched actorUserId={} roomId={} chapter={} count={}",
                actorUserId, roomId, current, visibleComments.size());
        return visibleComments;
    }

    @Transactional
    public void deleteComment(Long actorUserId, Long commentId) {
        log.info("Delete comment requested actorUserId={} commentId={}", actorUserId, commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Comment not found"));
        Long roomId = comment.getRoom().getId();

        commentRepository.delete(comment);
        auditService.log(actorUserId, "COMMENT_DELETED", "Comment", commentId, "roomId=" + roomId);
        log.info("Comment deleted actorUserId={} commentId={}", actorUserId, commentId);
    }
}
