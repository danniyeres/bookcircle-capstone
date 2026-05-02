import { useState } from "react";
import { fetchAuditLog, fetchRoomStats } from "../api/adminApi";
import { createBook } from "../api/booksApi";
import { useAuthStore } from "../store/authStore";
import { getApiErrorMessage } from "../utils/getApiErrorMessage";

function AdminPage() {
  const role = useAuthStore((state) => state.role);
  const canViewStats = role === "ADMIN" || role === "MODERATOR";
  const isAdmin = role === "ADMIN";

  const [bookForm, setBookForm] = useState({
    title: "",
    author: "",
    isbn: "",
    totalChapters: "",
  });
  const [createBookError, setCreateBookError] = useState("");
  const [createBookSuccess, setCreateBookSuccess] = useState("");
  const [isCreatingBook, setIsCreatingBook] = useState(false);

  const [roomIdForStats, setRoomIdForStats] = useState("");
  const [statsError, setStatsError] = useState("");
  const [statsData, setStatsData] = useState(null);
  const [isLoadingStats, setIsLoadingStats] = useState(false);

  const [auditError, setAuditError] = useState("");
  const [auditItems, setAuditItems] = useState([]);
  const [isLoadingAudit, setIsLoadingAudit] = useState(false);

  const handleBookFormChange = (event) => {
    const { name, value } = event.target;
    setBookForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleCreateBook = async (event) => {
    event.preventDefault();
    setCreateBookError("");
    setCreateBookSuccess("");
    setIsCreatingBook(true);

    try {
      const created = await createBook({
        title: bookForm.title.trim(),
        author: bookForm.author.trim() || null,
        isbn: bookForm.isbn.trim() || null,
        totalChapters: bookForm.totalChapters.trim()
          ? Number(bookForm.totalChapters)
          : null,
      });

      setCreateBookSuccess(`Book created: #${created.id} ${created.title}`);
      setBookForm({
        title: "",
        author: "",
        isbn: "",
        totalChapters: "",
      });
    } catch (err) {
      setCreateBookError(getApiErrorMessage(err, "Failed to create book"));
    } finally {
      setIsCreatingBook(false);
    }
  };

  const handleLoadStats = async (event) => {
    event.preventDefault();
    setStatsError("");
    setIsLoadingStats(true);

    try {
      const data = await fetchRoomStats(Number(roomIdForStats));
      setStatsData(data);
    } catch (err) {
      setStatsData(null);
      setStatsError(getApiErrorMessage(err, "Failed to load room stats"));
    } finally {
      setIsLoadingStats(false);
    }
  };

  const handleLoadAudit = async () => {
    setAuditError("");
    setIsLoadingAudit(true);

    try {
      const data = await fetchAuditLog();
      setAuditItems(Array.isArray(data) ? data : []);
    } catch (err) {
      setAuditItems([]);
      setAuditError(getApiErrorMessage(err, "Failed to load audit log"));
    } finally {
      setIsLoadingAudit(false);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Admin Panel</h1>
          <p className="page-description">
            Manage books, room stats, and audit history.
          </p>
        </div>
      </div>

      <div className="grid grid-2">
        <section className="card">
          <h2 className="section-title">Create Book (ADMIN)</h2>
          {!isAdmin ? (
            <p className="helper-text" style={{ color: "#ef4444", marginBottom: 16 }}>
              This action is available to admins only.
            </p>
          ) : null}

          <form className="form" onSubmit={handleCreateBook}>
            <label className="label">
              Title
              <input
                className="input"
                type="text"
                name="title"
                placeholder="Enter book title"
                value={bookForm.title}
                onChange={handleBookFormChange}
                disabled={!isAdmin}
                required
              />
            </label>

            <label className="label">
              Author
              <input
                className="input"
                type="text"
                name="author"
                placeholder="Enter author name"
                value={bookForm.author}
                onChange={handleBookFormChange}
                disabled={!isAdmin}
              />
            </label>

            <label className="label">
              ISBN
              <input
                className="input"
                type="text"
                name="isbn"
                placeholder="Enter ISBN"
                value={bookForm.isbn}
                onChange={handleBookFormChange}
                disabled={!isAdmin}
              />
            </label>

            <label className="label">
              Total Chapters
              <input
                className="input"
                type="number"
                min="1"
                name="totalChapters"
                placeholder="Enter total chapters"
                value={bookForm.totalChapters}
                onChange={handleBookFormChange}
                disabled={!isAdmin}
              />
            </label>

            {createBookError ? (
              <p className="helper-text" style={{ color: "#ef4444" }}>
                {createBookError}
              </p>
            ) : null}
            {createBookSuccess ? (
              <p className="helper-text" style={{ color: "#16a34a" }}>
                {createBookSuccess}
              </p>
            ) : null}

            <button className="button" type="submit" disabled={!isAdmin || isCreatingBook}>
              {isCreatingBook ? "Saving..." : "Save Book"}
            </button>
          </form>
        </section>

        <section className="card">
          <h2 className="section-title">Room Stats</h2>
          {!canViewStats ? (
            <p className="helper-text" style={{ color: "#ef4444", marginBottom: 16 }}>
              This section is available to admins and moderators.
            </p>
          ) : null}

          <form className="form" onSubmit={handleLoadStats}>
            <label className="label">
              Room ID
              <input
                className="input"
                type="number"
                min="1"
                placeholder="Enter room id"
                value={roomIdForStats}
                onChange={(event) => setRoomIdForStats(event.target.value)}
                disabled={!canViewStats}
                required
              />
            </label>

            {statsError ? <p className="helper-text" style={{ color: "#ef4444" }}>{statsError}</p> : null}

            <button className="button" type="submit" disabled={!canViewStats || isLoadingStats}>
              {isLoadingStats ? "Loading..." : "Load Stats"}
            </button>
          </form>

          {statsData ? (
            <div className="grid" style={{ marginTop: 18 }}>
              <div className="stat-card">
                <div className="stat-label">Room ID</div>
                <div className="stat-value">{statsData.roomId}</div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Comments</div>
                <div className="stat-value">{statsData.commentsCount}</div>
              </div>
              <div className="stat-card">
                <div className="stat-label">Progress Updates</div>
                <div className="stat-value">{statsData.progressUpdatesCount}</div>
              </div>
            </div>
          ) : null}
        </section>
      </div>

      <section className="card" style={{ marginTop: 20 }}>
        <div className="page-header" style={{ marginBottom: 14 }}>
          <h2 className="section-title" style={{ marginBottom: 0 }}>
            Audit Log (ADMIN)
          </h2>
          <button
            className="button button-secondary"
            type="button"
            onClick={handleLoadAudit}
            disabled={!isAdmin || isLoadingAudit}
          >
            {isLoadingAudit ? "Loading..." : "Refresh Audit"}
          </button>
        </div>

        {!isAdmin ? (
          <p className="helper-text" style={{ color: "#ef4444" }}>
            Audit log is available to admins only.
          </p>
        ) : null}
        {auditError ? <p className="helper-text" style={{ color: "#ef4444" }}>{auditError}</p> : null}

        {isAdmin ? (
          <table className="table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Action</th>
                <th>Actor User ID</th>
                <th>Entity</th>
                <th>Details</th>
              </tr>
            </thead>
            <tbody>
              {auditItems.length === 0 ? (
                <tr>
                  <td colSpan="5">No audit entries loaded</td>
                </tr>
              ) : (
                auditItems.map((item) => (
                  <tr key={item.id}>
                    <td>{item.ts ? new Date(item.ts).toLocaleString() : "-"}</td>
                    <td>{item.action}</td>
                    <td>{item.actorUserId}</td>
                    <td>
                      {item.entityType} #{item.entityId ?? "-"}
                    </td>
                    <td>{item.details || "-"}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        ) : null}
      </section>
    </div>
  );
}

export default AdminPage;
