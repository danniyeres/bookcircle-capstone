package bookcircle.service;

import bookcircle.entity.Book;
import bookcircle.exception.ApiException;
import bookcircle.repo.BookRepository;
import bookcircle.repo.CommentRepository;
import bookcircle.repo.ReadingProgressRepository;
import bookcircle.repo.RoomActivityStatsRepository;
import bookcircle.repo.RoomMemberRepository;
import bookcircle.repo.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final CommentRepository commentRepository;
    private final ReadingProgressRepository progressRepository;
    private final RoomActivityStatsRepository statsRepository;
    private final AuditService auditService;

    public List<Book> findBooks(String query) {
        List<Book> books;
        if (query == null || query.isBlank()) {
            books = bookRepository.findAll();
            log.info("Books fetched without query count={}", books.size());
            return books;
        }
        books = bookRepository.findByTitleContainingIgnoreCase(query);
        log.info("Books fetched query='{}' count={}", query, books.size());
        return books;
    }

    public Book createBook(Book book) {
        log.info("Create book title='{}' author='{}'", book.getTitle(), book.getAuthor());
        Book saved = bookRepository.save(book);
        log.info("Book created bookId={}", saved.getId());
        return saved;
    }

    @Transactional
    public void deleteBook(Long actorUserId, Long bookId) {
        log.info("Delete book requested actorUserId={} bookId={}", actorUserId, bookId);

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Book not found"));

        var rooms = roomRepository.findByBook_Id(bookId);
        for (var room : rooms) {
            Long roomId = room.getId();
            commentRepository.deleteByRoom_Id(roomId);
            progressRepository.deleteByRoom_Id(roomId);
            roomMemberRepository.deleteByRoom_Id(roomId);
            statsRepository.deleteById(roomId);
            roomRepository.delete(room);
        }

        bookRepository.delete(book);
        auditService.log(actorUserId, "BOOK_DELETED", "Book", bookId, "removedRooms=" + rooms.size());
        log.info("Book deleted actorUserId={} bookId={} removedRooms={}", actorUserId, bookId, rooms.size());
    }
}
