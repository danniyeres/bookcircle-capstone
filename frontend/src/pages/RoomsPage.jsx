import { useCallback, useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { fetchRooms, joinRoom } from "../api/roomsApi";
import { getApiErrorMessage } from "../utils/getApiErrorMessage";

function RoomsPage() {
  const navigate = useNavigate();
  const [rooms, setRooms] = useState([]);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [joiningRoomId, setJoiningRoomId] = useState(null);
  const [joinError, setJoinError] = useState("");

  const lastCreatedRoom = useMemo(() => {
    const rawValue = localStorage.getItem("lastCreatedRoom");
    if (!rawValue) {
      return null;
    }

    try {
      return JSON.parse(rawValue);
    } catch {
      return null;
    }
  }, []);

  const loadRooms = useCallback(async () => {
    setError("");
    setIsLoading(true);

    try {
      const data = await fetchRooms();
      setRooms(Array.isArray(data) ? data : []);
    } catch (err) {
      setRooms([]);
      setError(getApiErrorMessage(err, "Failed to load rooms"));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    loadRooms();
  }, [loadRooms]);

  const handleJoinRoom = async (room) => {
    setJoinError("");
    setJoiningRoomId(room.id);

    try {
      await joinRoom(room.id);
      navigate(`/rooms/${room.id}`, { state: { room } });
    } catch (err) {
      setJoinError(getApiErrorMessage(err, "Failed to join room"));
    } finally {
      setJoiningRoomId(null);
    }
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Rooms</h1>
          <p className="page-description">
            Discover reading rooms in your area and continue discussion chapter by chapter.
          </p>
        </div>
        <Link className="button" to="/rooms/create">
          Create Room
        </Link>
      </div>

      <div className="grid grid-2" style={{ marginBottom: 20 }}>
        <section className="card">
          <h2 className="section-title">Last Created Room</h2>
          {lastCreatedRoom ? (
            <div className="comment-item">
              <strong>{lastCreatedRoom.name}</strong>
              <p className="card-text" style={{ marginTop: 6 }}>
                Room #{lastCreatedRoom.id}, Book: {lastCreatedRoom.bookTitle}
              </p>
              <Link
                className="button"
                style={{ marginTop: 12, display: "inline-flex" }}
                to={`/rooms/${lastCreatedRoom.id}`}
                state={{ room: lastCreatedRoom }}
              >
                Open Room
              </Link>
            </div>
          ) : (
            <p className="card-text">No room saved yet. Create one to see quick access here.</p>
          )}
        </section>

        <section className="card">
          <h2 className="section-title">Rooms Summary</h2>
          <div className="comment-item">
            <strong>Total rooms</strong>
            <p className="card-text" style={{ marginTop: 6 }}>
              {isLoading ? "Loading..." : rooms.length}
            </p>
          </div>

          <button
            className="button button-secondary"
            type="button"
            style={{ marginTop: 12 }}
            onClick={loadRooms}
            disabled={isLoading}
          >
            {isLoading ? "Refreshing..." : "Refresh List"}
          </button>
        </section>
      </div>

      {error ? (
        <section className="card" style={{ marginBottom: 20 }}>
          <p className="helper-text" style={{ color: "#ef4444" }}>
            {error}
          </p>
        </section>
      ) : null}
      {joinError ? (
        <section className="card" style={{ marginBottom: 20 }}>
          <p className="helper-text" style={{ color: "#ef4444" }}>
            {joinError}
          </p>
        </section>
      ) : null}

      <section className="card">
        <h2 className="section-title">All Rooms</h2>
        {rooms.length === 0 ? (
          <p className="card-text">
            {isLoading ? "Loading rooms..." : "No rooms found yet."}
          </p>
        ) : (
          <div className="grid">
            {rooms.map((room) => (
              <div key={room.id} className="comment-item">
                <strong>{room.name}</strong>
                <p className="card-text" style={{ marginTop: 6 }}>
                  Room #{room.id}, Book: {room.bookTitle || "Not set"}
                </p>
                <p className="helper-text" style={{ marginTop: 6 }}>
                  Area: {room.h3Index || "Not set"}
                </p>

                <div style={{ marginTop: 12, display: "flex", gap: 10, flexWrap: "wrap" }}>
                  <Link
                    className="button button-secondary"
                    style={{ display: "inline-flex" }}
                    to={`/rooms/${room.id}`}
                    state={{ room }}
                  >
                    Open
                  </Link>
                  <button
                    className="button"
                    type="button"
                    onClick={() => handleJoinRoom(room)}
                    disabled={joiningRoomId === room.id}
                  >
                    {joiningRoomId === room.id ? "Joining..." : "Join"}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  );
}

export default RoomsPage;
