import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { fetchBooks } from "../api/booksApi";
import { fetchRooms } from "../api/roomsApi";
import { getApiErrorMessage } from "../utils/getApiErrorMessage";

function DashboardPage() {
    const [booksCount, setBooksCount] = useState(0);
    const [booksError, setBooksError] = useState("");

    const [roomsCount, setRoomsCount] = useState(0);
    const [roomsCountError, setRoomsCountError] = useState("");

    const [roomName, setRoomName] = useState("");
    const [roomSearchError, setRoomSearchError] = useState("");
    const [isSearchingRooms, setIsSearchingRooms] = useState(false);
    const [rooms, setRooms] = useState([]);

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

    useEffect(() => {
        let isCancelled = false;

        const loadDashboardData = async () => {
            setBooksError("");
            setRoomsCountError("");

            try {
                const booksData = await fetchBooks();

                if (!isCancelled) {
                    setBooksCount(Array.isArray(booksData) ? booksData.length : 0);
                }
            } catch (err) {
                if (!isCancelled) {
                    setBooksError(getApiErrorMessage(err, "Failed to load books count"));
                }
            }

            try {
                const roomsData = await fetchRooms();

                if (!isCancelled) {
                    setRoomsCount(Array.isArray(roomsData) ? roomsData.length : 0);
                }
            } catch (err) {
                if (!isCancelled) {
                    setRoomsCountError(getApiErrorMessage(err, "Failed to load rooms count"));
                }
            }
        };

        loadDashboardData();

        return () => {
            isCancelled = true;
        };
    }, []);

    const handleFindRooms = async (event) => {
        event.preventDefault();

        setRoomSearchError("");
        setIsSearchingRooms(true);

        try {
            const data = await fetchRooms();
            const allRooms = Array.isArray(data) ? data : [];

            const filteredRooms = allRooms.filter((room) =>
                room.name?.toLowerCase().includes(roomName.trim().toLowerCase())
            );

            setRooms(filteredRooms);
        } catch (err) {
            setRooms([]);
            setRoomSearchError(getApiErrorMessage(err, "Failed to find rooms"));
        } finally {
            setIsSearchingRooms(false);
        }
    };

    return (
        <div>
            <section className="hero-card">
                <h1 className="hero-title">
                    Read together, track progress, and discuss books without spoilers.
                </h1>

                <p className="hero-text">
                    Track your books, discover reading rooms, and continue chapter by chapter.
                </p>

                <div className="hero-actions">
                    <Link className="button" to="/rooms/create">
                        Create New Room
                    </Link>
                    <Link className="button button-secondary" to="/books">
                        Explore Books
                    </Link>
                </div>
            </section>

            <div className="grid grid-2" style={{ marginTop: 20 }}>
                <div className="stat-card">
                    <div className="stat-label">Available Books</div>
                    <div className="stat-value">{booksCount}</div>
                </div>

                <div className="stat-card">
                    <div className="stat-label">Available Rooms</div>
                    <div className="stat-value">{roomsCount}</div>
                </div>
            </div>

            {booksError ? (
                <section className="card" style={{ marginTop: 20 }}>
                    <h2 className="section-title">Couldn&apos;t Load Books</h2>
                    <p className="card-text">{booksError}</p>
                </section>
            ) : null}

            {roomsCountError ? (
                <section className="card" style={{ marginTop: 20 }}>
                    <h2 className="section-title">Couldn&apos;t Load Rooms</h2>
                    <p className="card-text">{roomsCountError}</p>
                </section>
            ) : null}

            <div className="grid grid-2" style={{ marginTop: 20 }}>
                <section className="card">
                    <h2 className="section-title">Find Rooms by Name</h2>
                    <p className="card-text" style={{ marginBottom: 14 }}>
                        Enter a room name to search existing reading rooms.
                    </p>

                    <form className="form" onSubmit={handleFindRooms}>
                        <label className="label">
                            Room Name
                            <input
                                className="input"
                                type="text"
                                placeholder="Example: Clean Code"
                                value={roomName}
                                onChange={(event) => setRoomName(event.target.value)}
                                required
                            />
                        </label>

                        {roomSearchError ? (
                            <p className="helper-text" style={{ color: "#ef4444" }}>
                                {roomSearchError}
                            </p>
                        ) : null}

                        <button className="button" type="submit" disabled={isSearchingRooms}>
                            {isSearchingRooms ? "Searching..." : "Find Rooms"}
                        </button>
                    </form>
                </section>

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
                        <p className="card-text">
                            No room saved yet. Create one to see quick access here.
                        </p>
                    )}
                </section>
            </div>

            <section className="card" style={{ marginTop: 20 }}>
                <h2 className="section-title">Search Results</h2>

                {rooms.length === 0 ? (
                    <p className="card-text">No rooms found yet. Search by room name above.</p>
                ) : (
                    <div className="grid">
                        {rooms.map((room) => (
                            <div key={room.id} className="comment-item">
                                <strong>{room.name}</strong>
                                <p className="card-text" style={{ marginTop: 6 }}>
                                    Room #{room.id}, Book: {room.bookTitle || "Not set"}
                                </p>
                                <Link
                                    className="button"
                                    style={{ marginTop: 12, display: "inline-flex" }}
                                    to={`/rooms/${room.id}`}
                                    state={{ room }}
                                >
                                    Open Room
                                </Link>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
}

export default DashboardPage;