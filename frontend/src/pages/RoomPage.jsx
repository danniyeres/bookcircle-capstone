import { useCallback, useEffect, useMemo, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import { createComment, fetchComments } from "../api/commentsApi";
import { updateProgress } from "../api/progressApi";
import { joinRoom } from "../api/roomsApi";
import { getApiErrorMessage } from "../utils/getApiErrorMessage";

function RoomPage() {
  const { roomId } = useParams();
  const location = useLocation();
  const numericRoomId = Number(roomId);

  const roomFromRoute = location.state?.room;
  const roomFromStorage = useMemo(() => {
    const rawValue = localStorage.getItem("lastCreatedRoom");
    if (!rawValue) {
      return null;
    }
    try {
      const parsed = JSON.parse(rawValue);
      return parsed?.id === numericRoomId ? parsed : null;
    } catch {
      return null;
    }
  }, [numericRoomId]);
  const room = roomFromRoute || roomFromStorage;

  const [joinMessage, setJoinMessage] = useState("");
  const [joinError, setJoinError] = useState("");
  const [isJoining, setIsJoining] = useState(false);

  const [progressChapter, setProgressChapter] = useState("");
  const [progressData, setProgressData] = useState(null);
  const [progressError, setProgressError] = useState("");
  const [isSavingProgress, setIsSavingProgress] = useState(false);

  const [commentForm, setCommentForm] = useState({
    chapterNumber: "",
    content: "",
  });
  const [commentError, setCommentError] = useState("");
  const [isSendingComment, setIsSendingComment] = useState(false);

  const [comments, setComments] = useState([]);
  const [commentsError, setCommentsError] = useState("");
  const [isLoadingComments, setIsLoadingComments] = useState(false);

  const loadComments = useCallback(async () => {
    if (!Number.isFinite(numericRoomId)) {
      setComments([]);
      setCommentsError("Invalid room id");
      return;
    }

    setCommentsError("");
    setIsLoadingComments(true);
    try {
      const data = await fetchComments(numericRoomId);
      setComments(Array.isArray(data) ? data : []);
    } catch (err) {
      setCommentsError(getApiErrorMessage(err, "Failed to load comments"));
      setComments([]);
    } finally {
      setIsLoadingComments(false);
    }
  }, [numericRoomId]);

  const handleJoinRoom = async () => {
    if (!Number.isFinite(numericRoomId)) {
      setJoinError("Invalid room id");
      return;
    }

    setJoinMessage("");
    setJoinError("");
    setIsJoining(true);

    try {
      await joinRoom(numericRoomId);
      setJoinMessage("Joined successfully. You can now update progress and post comments.");
      await loadComments();
    } catch (err) {
      setJoinError(getApiErrorMessage(err, "Failed to join room"));
    } finally {
      setIsJoining(false);
    }
  };

  const handleSaveProgress = async (event) => {
    event.preventDefault();
    if (!Number.isFinite(numericRoomId)) {
      setProgressError("Invalid room id");
      return;
    }

    setProgressError("");
    setIsSavingProgress(true);

    try {
      const data = await updateProgress({
        roomId: numericRoomId,
        chapterNumber: Number(progressChapter),
      });
      setProgressData(data);
      await loadComments();
    } catch (err) {
      setProgressError(getApiErrorMessage(err, "Failed to save progress"));
    } finally {
      setIsSavingProgress(false);
    }
  };

  const handleCommentSubmit = async (event) => {
    event.preventDefault();
    if (!Number.isFinite(numericRoomId)) {
      setCommentError("Invalid room id");
      return;
    }

    setCommentError("");
    setIsSendingComment(true);

    try {
      await createComment({
        roomId: numericRoomId,
        chapterNumber: Number(commentForm.chapterNumber),
        content: commentForm.content.trim(),
      });
      setCommentForm({
        chapterNumber: "",
        content: "",
      });
      await loadComments();
    } catch (err) {
      setCommentError(getApiErrorMessage(err, "Failed to publish comment"));
    } finally {
      setIsSendingComment(false);
    }
  };

  const progressPercent = progressData?.chapterNumber
    ? Math.min(progressData.chapterNumber * 5, 100)
    : 0;

  useEffect(() => {
    loadComments();
  }, [loadComments]);

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Room #{roomId}</h1>
          <p className="page-description">
            Join the room, track your chapter progress, and share spoiler-safe comments.
          </p>
        </div>

        <div style={{ display: "flex", gap: 12, flexDirection: "column", alignItems: "flex-end" }}>
          <span className="badge">Spoiler Safe</span>
          <button className="button" type="button" onClick={handleJoinRoom} disabled={isJoining}>
            {isJoining ? "Joining..." : "Join Room"}
          </button>
        </div>
      </div>

      {joinMessage ? <p className="helper-text" style={{ color: "#16a34a" }}>{joinMessage}</p> : null}
      {joinError ? <p className="helper-text" style={{ color: "#ef4444" }}>{joinError}</p> : null}

      <div className="grid grid-2" style={{ marginTop: 20 }}>
        <section className="card">
          <h2 className="section-title">Room Information</h2>

          <div className="grid">
            <div className="comment-item">
              <strong>Room Name</strong>
              <p className="card-text" style={{ marginTop: 6 }}>
                {room?.name || "Room details are not available yet"}
              </p>
            </div>

            <div className="comment-item">
              <strong>Linked Book</strong>
              <p className="card-text" style={{ marginTop: 6 }}>
                {room?.bookTitle || "Not available from this route"}
              </p>
            </div>

            <div className="comment-item">
              <strong>Area Code</strong>
              <p className="card-text" style={{ marginTop: 6 }}>
                {room?.h3Index || "Not available"}
              </p>
            </div>
          </div>
        </section>

        <section className="card">
          <h2 className="section-title">Your Progress</h2>

          <div className="comment-item" style={{ marginBottom: 16 }}>
            <strong>Current Progress</strong>
            <p className="card-text" style={{ marginTop: 6 }}>
              {progressData
                ? `Chapter ${progressData.chapterNumber}`
                : "No saved progress yet"}
            </p>

            <div className="progress-shell">
              <div className="progress-bar" style={{ width: `${progressPercent}%` }} />
            </div>
          </div>

          <form className="form" onSubmit={handleSaveProgress}>
            <label className="label">
              Current Chapter Number
              <input
                className="input"
                type="number"
                min="1"
                placeholder="Enter chapter number"
                value={progressChapter}
                onChange={(event) => setProgressChapter(event.target.value)}
                required
              />
            </label>

            {progressError ? <p className="helper-text" style={{ color: "#ef4444" }}>{progressError}</p> : null}

            <button className="button" type="submit" disabled={isSavingProgress}>
              {isSavingProgress ? "Saving..." : "Save Progress"}
            </button>
          </form>
        </section>
      </div>

      <div className="grid grid-2" style={{ marginTop: 20 }}>
        <section className="card">
          <h2 className="section-title">Add Comment</h2>

          <form className="form" onSubmit={handleCommentSubmit}>
            <label className="label">
              Chapter Number
              <input
                className="input"
                type="number"
                min="1"
                placeholder="Enter chapter number"
                value={commentForm.chapterNumber}
                onChange={(event) =>
                  setCommentForm((prev) => ({ ...prev, chapterNumber: event.target.value }))
                }
                required
              />
            </label>

            <label className="label">
              Comment
              <textarea
                className="textarea"
                placeholder="Write your room comment here"
                value={commentForm.content}
                onChange={(event) =>
                  setCommentForm((prev) => ({ ...prev, content: event.target.value }))
                }
                required
              />
            </label>

            {commentError ? <p className="helper-text" style={{ color: "#ef4444" }}>{commentError}</p> : null}

            <button className="button" type="submit" disabled={isSendingComment}>
              {isSendingComment ? "Publishing..." : "Publish Comment"}
            </button>
          </form>
        </section>

        <section className="card">
          <div className="page-header" style={{ marginBottom: 14 }}>
            <h2 className="section-title" style={{ marginBottom: 0 }}>
              Visible Comments
            </h2>
            <button className="button button-secondary" type="button" onClick={loadComments}>
              Refresh
            </button>
          </div>

          {isLoadingComments ? <p className="card-text">Loading comments...</p> : null}
          {commentsError ? <p className="helper-text" style={{ color: "#ef4444" }}>{commentsError}</p> : null}

          {!isLoadingComments && !commentsError && comments.length === 0 ? (
            <p className="card-text">
              No visible comments yet. Join the room, update progress, then refresh.
            </p>
          ) : null}

          <div className="grid">
            {comments.map((item) => (
              <div key={item.id} className="comment-item">
                <strong>Chapter {item.chapterNumber}</strong>
                <p className="card-text" style={{ marginTop: 6 }}>
                  {item.content}
                </p>
                <p className="helper-text" style={{ marginTop: 8 }}>
                  Author #{item.authorId} • {new Date(item.createdAt).toLocaleString()}
                </p>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  );
}

export default RoomPage;
