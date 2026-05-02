import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchBooks } from "../api/booksApi";
import { getApiErrorMessage } from "../utils/getApiErrorMessage";

function BookCover({ title, coverUrl }) {
  const [hasError, setHasError] = useState(false);

  if (!coverUrl || hasError) {
    return <div className="book-cover-fallback">No cover</div>;
  }

  return (
    <img
      className="book-cover"
      src={coverUrl}
      alt={`${title} cover`}
      loading="lazy"
      onError={() => setHasError(true)}
    />
  );
}

function BooksPage() {
  const navigate = useNavigate();
  const [searchTerm, setSearchTerm] = useState("");
  const [books, setBooks] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let isCancelled = false;
    const timerId = setTimeout(async () => {
      setIsLoading(true);
      setError("");

      try {
        const data = await fetchBooks(
          searchTerm.trim() ? { query: searchTerm.trim() } : undefined
        );
        if (!isCancelled) {
          setBooks(Array.isArray(data) ? data : []);
        }
      } catch (err) {
        if (!isCancelled) {
          setError(getApiErrorMessage(err, "Failed to load books"));
          setBooks([]);
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false);
        }
      }
    }, 250);

    return () => {
      isCancelled = true;
      clearTimeout(timerId);
    };
  }, [searchTerm]);

  const handleUseInRoom = (book) => {
    navigate("/rooms/create", {
      state: { selectedBookId: book.id },
    });
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Books Library</h1>
          <p className="page-description">
            Find a book and use it to create your next reading room.
          </p>
        </div>

        <span className="badge">{books.length} books</span>
      </div>

      <section className="card" style={{ marginBottom: 20 }}>
        <div className="form-row">
          <label className="label">
            Search by title
            <input
              className="input"
              type="text"
              placeholder="Search books..."
              value={searchTerm}
              onChange={(event) => setSearchTerm(event.target.value)}
            />
          </label>
        </div>
      </section>

      {error ? (
        <section className="card" style={{ marginBottom: 20 }}>
          <h2 className="section-title">Request failed</h2>
          <p className="card-text">{error}</p>
        </section>
      ) : null}

      {isLoading ? (
        <section className="card">
          <p className="card-text">Loading books...</p>
        </section>
      ) : null}

      {!isLoading && books.length > 0 ? (
        <div className="book-grid">
          {books.map((book) => (
            <article key={book.id} className="book-card">
              <div className="book-pill">Book #{book.id}</div>
              <div className="book-cover-wrap">
                <BookCover title={book.title} coverUrl={book.coverUrl} />
              </div>
              <h3 className="book-title">{book.title}</h3>
              <div className="book-meta">Author: {book.author || "Unknown"}</div>
              <div className="book-meta">ISBN: {book.isbn || "Not set"}</div>
              <div className="book-meta">
                Total Chapters: {book.totalChapters ?? "Not set"}
              </div>

              <button
                className="button"
                style={{ marginTop: 18 }}
                type="button"
                onClick={() => handleUseInRoom(book)}
              >
                Use in Room
              </button>
            </article>
          ))}
        </div>
      ) : null}

      {!isLoading && !error && books.length === 0 ? (
        <section className="card">
          <h2 className="section-title">No books found</h2>
          <p className="card-text">
            If list is empty, create the first book from the Admin page.
          </p>
        </section>
      ) : null}
    </div>
  );
}

export default BooksPage;
