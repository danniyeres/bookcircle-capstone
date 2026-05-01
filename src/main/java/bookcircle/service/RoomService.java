package bookcircle.service;

import bookcircle.domain.RoomMemberRole;
import bookcircle.dto.RoomDtos;
import bookcircle.entity.Book;
import bookcircle.entity.ReadingProgress;
import bookcircle.entity.Room;
import bookcircle.entity.RoomMember;
import bookcircle.entity.User;
import bookcircle.exception.ApiException;
import bookcircle.repo.BookRepository;
import bookcircle.repo.CommentRepository;
import bookcircle.repo.ReadingProgressRepository;
import bookcircle.repo.RoomActivityStatsRepository;
import bookcircle.repo.RoomMemberRepository;
import bookcircle.repo.RoomRepository;
import bookcircle.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final UserRepository userRepository;
    private final H3Service h3Service;
    private final AuditService auditService;
    private final BookRepository bookRepository;
    private final CommentRepository commentRepository;
    private final ReadingProgressRepository progressRepository;
    private final RoomActivityStatsRepository statsRepository;


    @Transactional
    public RoomDtos.RoomResponse createRoom(Long actorUserId, RoomDtos.CreateRoomRequest req) {
        log.info("Create room requested actorUserId={} name='{}' bookId={}", actorUserId, req.name(), req.bookId());

        User owner = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));


        String h3Index = req.h3Index();
        if (h3Index == null || h3Index.isBlank()) {
            if (req.lat() == null || req.lon() == null) {
                log.warn("Create room rejected: missing h3Index and coordinates actorUserId={}", actorUserId);
                throw new ApiException(HttpStatus.BAD_REQUEST, "Provide either h3Index or (lat, lon)" );
            }
            int res = req.resolution() == null ? 9 : req.resolution();
            h3Index = h3Service.toH3(req.lat(), req.lon(), res);
        }


        Room room = new Room();
        room.setName(req.name());
        room.setOwner(owner);
        room.setH3Index(h3Index);

        Book book = bookRepository.findById(req.bookId())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Book not found"));
        room.setBook(book);

        room = roomRepository.save(room);

        RoomMember m = new RoomMember();
        m.setRoom(room);
        m.setUser(owner);
        m.setRole(RoomMemberRole.OWNER);
        roomMemberRepository.save(m);

        auditService.log(actorUserId, "ROOM_CREATED", "Room", room.getId(), "h3=" + h3Index);
        log.info("Room created roomId={} ownerId={} h3Index={}", room.getId(), owner.getId(), h3Index);

        return new RoomDtos.RoomResponse(
                room.getId(),
                room.getName(),
                book.getId(),
                book.getTitle(),
                room.getH3Index(),
                owner.getId()
        );
    }

    @Transactional
    public void joinRoom(Long actorUserId, Long roomId) {
        log.info("Join room requested actorUserId={} roomId={}", actorUserId, roomId);

        var user = userRepository.findById(actorUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        var room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));

        if (roomMemberRepository.findByRoom_IdAndUser_Id(roomId, actorUserId).isPresent()) {
            log.info("Join room skipped: already member actorUserId={} roomId={}", actorUserId, roomId);
            return;
        }

        RoomMember m = new RoomMember();
        m.setRoom(room);
        m.setUser(user);
        m.setRole(RoomMemberRole.MEMBER);
        roomMemberRepository.save(m);

        auditService.log(actorUserId, "ROOM_JOINED", "Room", roomId, null);
        log.info("Room joined actorUserId={} roomId={}", actorUserId, roomId);
    }

    @Transactional(readOnly = true)
    public List<RoomDtos.RoomResponse> findByH3(String h3Index) {
        var rooms = roomRepository.findByH3Index(h3Index).stream()

                .map(r -> new RoomDtos.RoomResponse(
                        r.getId(),
                        r.getName(),
                        r.getBook().getId(),
                        r.getBook().getTitle(),
                        r.getH3Index(),
                        r.getOwner().getId()))
                .toList();
        log.info("Rooms fetched by h3Index={} count={}", h3Index, rooms.size());
        return rooms;
    }

    @Transactional(readOnly = true)
    public List<RoomDtos.RoomResponse> getRooms () {
        return roomRepository.findAll().stream()
                .map(r -> new RoomDtos.RoomResponse(
                        r.getId(),
                        r.getName(),
                        r.getBook().getId(),
                        r.getBook().getTitle(),
                        r.getH3Index(),
                        r.getOwner().getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomDtos.RoomMembersProgressListResponse getMembersProgress(Long actorUserId, Long roomId) {
        log.info("Members progress requested actorUserId={} roomId={}", actorUserId, roomId);

        if (roomMemberRepository.findByRoom_IdAndUser_Id(roomId, actorUserId).isEmpty()) {
            log.warn("Members progress rejected: user is not room member actorUserId={} roomId={}",
                    actorUserId, roomId);
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not a member of this room");
        }

        if (!roomRepository.existsById(roomId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Room not found");
        }

        Map<Long, ReadingProgress> progressByUserId = progressRepository.findByRoom_Id(roomId).stream()
                .collect(Collectors.toMap(p -> p.getUser().getId(), Function.identity()));

        List<RoomDtos.RoomMemberProgressResponse> members = roomMemberRepository.findByRoom_Id(roomId).stream()
                .map(m -> {
                    ReadingProgress progress = progressByUserId.get(m.getUser().getId());
                    int chapterNumber = progress == null ? 0 : progress.getChapterNumber();
                    return new RoomDtos.RoomMemberProgressResponse(
                            m.getUser().getId(),
                            m.getUser().getEmail(),
                            m.getUser().getNickname(),
                            m.getRole().name(),
                            chapterNumber,
                            progress == null ? null : progress.getUpdatedAt()
                    );
                })
                .toList();

        return new RoomDtos.RoomMembersProgressListResponse(roomId, members);
    }

    @Transactional
    public void deleteRoom(Long actorUserId, Long roomId) {
        log.info("Delete room requested actorUserId={} roomId={}", actorUserId, roomId);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Room not found"));

        commentRepository.deleteByRoom_Id(roomId);
        progressRepository.deleteByRoom_Id(roomId);
        roomMemberRepository.deleteByRoom_Id(roomId);
        statsRepository.deleteById(roomId);
        roomRepository.delete(room);

        auditService.log(actorUserId, "ROOM_DELETED", "Room", roomId, null);
        log.info("Room deleted actorUserId={} roomId={}", actorUserId, roomId);
    }
}
