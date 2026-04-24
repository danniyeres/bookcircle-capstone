package bookcircle.controller;

import bookcircle.entity.Book;
import bookcircle.service.BookService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping ("/books")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<Book> getBooks(@RequestParam(required = false) String query) {
        return bookService.findBooks(query);
    }

    @GetMapping("/{id}")
    public Book getBook(@RequestParam(required = false) @PathVariable Long id) {
        return bookService.findBooks(null).stream()
                .filter(b -> b.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Book createBook(@RequestBody Book book) {
        return bookService.createBook(book);
    }
}
