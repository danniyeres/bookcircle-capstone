package bookcircle.util;

import bookcircle.entity.Book;
import bookcircle.repo.BookRepository;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class BookCatalogInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BookCatalogInitializer.class);
    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes";

    private final BookRepository bookRepository;
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;

    @Value("${app.books.seedOnEmpty:true}")
    private boolean seedOnEmpty;

    @Value("${app.books.seedFile:books.json}")
    private String seedFile;

    @Value("${app.books.googleApiKey:}")
    private String googleApiKey;

    @Value("${app.books.maxResultsPerQuery:1}")
    private int maxResultsPerQuery;

    @Value("${app.books.requestDelayMs:150}")
    private long requestDelayMs;

    @Value("${app.books.maxAttempts:3}")
    private int maxAttempts;

    @Value("${app.books.retryBackoffMs:600}")
    private long retryBackoffMs;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void run(String... args) {
        if (!seedOnEmpty) {
            log.info("Books auto-seed disabled by config");
            return;
        }

        long booksCount = bookRepository.count();
        if (booksCount > 0) {
            log.info("Books table already populated, skipping auto-seed count={}", booksCount);
            return;
        }

        List<SeedBook> seedBooks = loadSeedBooks();
        if (seedBooks.isEmpty()) {
            log.warn("No books loaded from seed file='{}'", seedFile);
            return;
        }

        List<Book> booksToSave = new ArrayList<>();
        Set<String> uniqueKeys = new HashSet<>();

        int fromApiCount = 0;
        int fallbackCount = 0;
        int skippedWithoutChapters = 0;

        for (SeedBook seedBook : seedBooks) {
            Optional<Book> googleBook = fetchFromGoogle(seedBook);
            Book book = googleBook.orElseGet(() -> buildFallbackBook(seedBook));
            if (book.getTitle() == null || book.getTitle().isBlank()) {
                continue;
            }
            if (book.getTotalChapters() == null) {
                skippedWithoutChapters++;
                log.info("Book skipped because totalChapters is missing title='{}'", book.getTitle());
                continue;
            }

            String uniqueKey = normalize(book.getTitle()) + "|" + normalize(book.getAuthor());
            if (!uniqueKeys.add(uniqueKey)) {
                continue;
            }

            if (googleBook.isPresent()) {
                fromApiCount++;
            } else {
                fallbackCount++;
            }

            booksToSave.add(book);
        }

        if (booksToSave.isEmpty()) {
            log.warn("Books auto-seed produced no records to save skippedWithoutChapters={}", skippedWithoutChapters);
            return;
        }

        bookRepository.saveAll(booksToSave);
        log.info("Books auto-seed completed saved={} fromApi={} fallback={} skippedWithoutChapters={} sourceFile='{}'",
                booksToSave.size(), fromApiCount, fallbackCount, skippedWithoutChapters, seedFile);
    }

    private List<SeedBook> loadSeedBooks() {
        try {
            Resource resource = resourceLoader.getResource("classpath:" + seedFile);
            if (!resource.exists()) {
                log.warn("Seed file not found file='{}'", seedFile);
                return List.of();
            }

            try (InputStream input = resource.getInputStream()) {
                List<SeedBook> books = objectMapper.readValue(input, new TypeReference<>() {
                });

                return books.stream()
                        .filter(seedBook -> seedBook != null && seedBook.title() != null && !seedBook.title().isBlank())
                        .toList();
            }
        } catch (Exception e) {
            log.error("Failed to load books seed file='{}'", seedFile, e);
            return List.of();
        }
    }

    private Optional<Book> fetchFromGoogle(SeedBook seedBook) {
        List<String> queries = buildGoogleQueries(seedBook);
        for (String query : queries) {
            Optional<Book> maybeBook = fetchByQueryWithRetry(seedBook.title(), seedBook.author(), query);
            if (maybeBook.isPresent()) {
                return maybeBook;
            }
        }
        return Optional.empty();
    }

    private Optional<Book> fetchByQueryWithRetry(String title, String author, String query) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (requestDelayMs > 0) {
                sleepSilently(requestDelayMs);
            }
            try {
                UriComponentsBuilder uriBuilder = UriComponentsBuilder
                        .fromHttpUrl(GOOGLE_BOOKS_API_URL)
                        .queryParam("q", query)
                        .queryParam("maxResults", maxResultsPerQuery)
                        .queryParam("printType", "books");

                if (googleApiKey != null && !googleApiKey.isBlank()) {
                    uriBuilder.queryParam("key", googleApiKey);
                }

                ResponseEntity<GoogleBooksResponse> response = restTemplate.getForEntity(
                        uriBuilder.build().encode().toUri(),
                        GoogleBooksResponse.class
                );

                GoogleBooksResponse body = response.getBody();
                if (!response.getStatusCode().is2xxSuccessful() || body == null || body.items() == null || body.items().isEmpty()) {
                    return Optional.empty();
                }

                GoogleBookItem firstItem = body.items().get(0);
                if (firstItem == null || firstItem.volumeInfo() == null || firstItem.volumeInfo().title() == null || firstItem.volumeInfo().title().isBlank()) {
                    return Optional.empty();
                }

                VolumeInfo volumeInfo = firstItem.volumeInfo();
                Book book = new Book();
                book.setTitle(volumeInfo.title().trim());
                book.setAuthor(resolveAuthor(volumeInfo.authors(), author));
                book.setIsbn(extractIsbn(volumeInfo.industryIdentifiers()));
                book.setCoverUrl(extractCoverUrl(volumeInfo.imageLinks()));
                book.setDescription(cleanText(volumeInfo.description()));
                book.setTotalChapters(estimateChapters(volumeInfo.pageCount()));
                return Optional.of(book);
            } catch (RestClientResponseException ex) {
                int status = ex.getStatusCode().value();
                log.warn("Google Books request failed title='{}' status={} attempt={}/{}",
                        title, status, attempt, maxAttempts);
                if (!isRetriableStatus(status) || attempt == maxAttempts) {
                    return Optional.empty();
                }
                sleepSilently(retryBackoffMs * attempt);
            } catch (ResourceAccessException ex) {
                log.warn("Google Books network issue title='{}' attempt={}/{} reason='{}'",
                        title, attempt, maxAttempts, ex.getMessage());
                if (attempt == maxAttempts) {
                    return Optional.empty();
                }
                sleepSilently(retryBackoffMs * attempt);
            } catch (RestClientException ex) {
                log.warn("Google Books request failed title='{}' attempt={}/{} reason='{}'",
                        title, attempt, maxAttempts, ex.getMessage());
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    private List<String> buildGoogleQueries(SeedBook seedBook) {
        String titlePart = "intitle:\"" + seedBook.title().trim() + "\"";
        if (seedBook.author() == null || seedBook.author().isBlank()) {
            return List.of(titlePart, seedBook.title().trim());
        }
        String titleAndAuthor = titlePart + " inauthor:\"" + seedBook.author().trim() + "\"";
        return List.of(titleAndAuthor, titlePart, seedBook.title().trim());
    }

    private boolean isRetriableStatus(int status) {
        return status == 408 || status == 429 || status >= 500;
    }

    private void sleepSilently(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    private Book buildFallbackBook(SeedBook seedBook) {
        Book book = new Book();
        book.setTitle(seedBook.title().trim());
        if (seedBook.author() != null && !seedBook.author().isBlank()) {
            book.setAuthor(seedBook.author().trim());
        }
        return book;
    }

    private Integer estimateChapters(Integer pageCount) {
        if (pageCount == null || pageCount <= 0) {
            return null;
        }
        int chapters = pageCount / 20;
        return Math.max(chapters, 1);
    }

    private String extractIsbn(List<IndustryIdentifier> identifiers) {
        if (identifiers == null || identifiers.isEmpty()) {
            return null;
        }

        Optional<String> isbn13 = identifiers.stream()
                .filter(id -> id != null && "ISBN_13".equalsIgnoreCase(id.type()) && id.identifier() != null && !id.identifier().isBlank())
                .map(IndustryIdentifier::identifier)
                .findFirst();

        return isbn13.orElseGet(() -> identifiers.stream()
                .filter(id -> id != null && "ISBN_10".equalsIgnoreCase(id.type()) && id.identifier() != null && !id.identifier().isBlank())
                .map(IndustryIdentifier::identifier)
                .findFirst()
                .orElse(null));

    }

    private String extractCoverUrl(ImageLinks imageLinks) {
        if (imageLinks == null) {
            return null;
        }

        String[] candidates = new String[]{
                imageLinks.extraLarge(),
                imageLinks.large(),
                imageLinks.medium(),
                imageLinks.small(),
                imageLinks.thumbnail(),
                imageLinks.smallThumbnail()
        };

        for (String url : candidates) {
            String normalized = normalizeCoverUrl(url);
            if (normalized != null) {
                return normalized;
            }
        }
        return null;
    }

    private String normalizeCoverUrl(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.trim();
        if (trimmed.startsWith("http://")) {
            return "https://" + trimmed.substring("http://".length());
        }
        return trimmed;
    }

    private String cleanText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String resolveAuthor(List<String> authors, String fallbackAuthor) {
        if (authors != null && !authors.isEmpty()) {
            return String.join(", ", authors);
        }

        if (fallbackAuthor != null && !fallbackAuthor.isBlank()) {
            return fallbackAuthor.trim();
        }

        return null;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    private record SeedBook(String title, String author) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleBooksResponse(List<GoogleBookItem> items) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GoogleBookItem(VolumeInfo volumeInfo) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record VolumeInfo(
            String title,
            List<String> authors,
            Integer pageCount,
            List<IndustryIdentifier> industryIdentifiers,
            String description,
            ImageLinks imageLinks
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record IndustryIdentifier(String type, String identifier) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ImageLinks(
            String smallThumbnail,
            String thumbnail,
            String small,
            String medium,
            String large,
            String extraLarge
    ) {
    }
}
