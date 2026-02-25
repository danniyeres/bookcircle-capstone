package bookcircle.service;

import bookcircle.entity.Book;
import bookcircle.repo.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<Book> findBooks(String query) {
        if (query == null || query.isBlank()) {
            return bookRepository.findAll();
        }
        return bookRepository.findByTitleContainingIgnoreCase(query);
    }

    public Book createBook(Book book) {
        return bookRepository.save(book);
    }
}