package bookcircle.service;

import bookcircle.entity.Book;
import bookcircle.repo.BookRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private static final Logger log = LoggerFactory.getLogger(BookService.class);

    private final BookRepository bookRepository;

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
}
