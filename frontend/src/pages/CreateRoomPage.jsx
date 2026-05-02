import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { fetchBooks } from "../api/booksApi";
import { createRoom } from "../api/roomsApi";
import { getApiErrorMessage } from "../utils/getApiErrorMessage";
import { MapContainer, TileLayer, Marker, useMapEvents, useMap } from "react-leaflet";


function LocationPickerMap({ lat, lon, onSelectLocation }) {
    const position = lat && lon ? [Number(lat), Number(lon)] : [43.238949, 76.889709];

    function MapClickHandler() {
        useMapEvents({
            click(event) {
                onSelectLocation({
                    lat: event.latlng.lat.toFixed(6),
                    lon: event.latlng.lng.toFixed(6),
                });
            },
        });

        return null;
    }

    return (
        <div className="map-card">
            <MapContainer center={position} zoom={12} className="room-map">
                <TileLayer
                    attribution='&copy; OpenStreetMap contributors'
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                />

                <MapClickHandler />

                {lat && lon ? <Marker position={position} /> : null}
            </MapContainer>
        </div>
    );
}

function CreateRoomPage() {
    const navigate = useNavigate();
    const location = useLocation();

    const selectedBookId = useMemo(() => {
        const value = location.state?.selectedBookId;
        return value ? String(value) : "";
    }, [location.state]);

    const [books, setBooks] = useState([]);
    const [isBooksLoading, setIsBooksLoading] = useState(true);
    const [booksError, setBooksError] = useState("");

    const [form, setForm] = useState({
        name: "",
        bookId: selectedBookId,
        lat: "",
        lon: "",
        resolution: "9",
    });
    const [submitError, setSubmitError] = useState("");
    const [isSubmitting, setIsSubmitting] = useState(false);


    const handleMapSelect = ({ lat, lon }) => {
        setForm((prev) => ({
            ...prev,
            lat,
            lon,
        }));
    };

    function RecenterMap({ lat, lon }) {
        const map = useMap();

        useEffect(() => {
            if (lat && lon) {
                map.setView([Number(lat), Number(lon)], 14);
            }
        }, [lat, lon, map]);

        return null;
    }

    useEffect(() => {
        let isCancelled = false;

        const loadBooks = async () => {
            setIsBooksLoading(true);
            setBooksError("");
            try {
                const data = await fetchBooks();
                if (!isCancelled) {
                    const safeData = Array.isArray(data) ? data : [];
                    setBooks(safeData);
                    if (!form.bookId && safeData.length > 0) {
                        setForm((prev) => ({ ...prev, bookId: String(safeData[0].id) }));
                    }
                }
            } catch (err) {
                if (!isCancelled) {
                    setBooksError(getApiErrorMessage(err, "Failed to load books"));
                }
            } finally {
                if (!isCancelled) {
                    setIsBooksLoading(false);
                }
            }
        };

        loadBooks();

        return () => {
            isCancelled = true;
        };
    }, []);

    useEffect(() => {
        if (!selectedBookId) {
            return;
        }
        setForm((prev) => ({
            ...prev,
            bookId: selectedBookId,
        }));
    }, [selectedBookId]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleUseMyLocation = () => {
        setSubmitError("");

        if (!navigator.geolocation) {
            setSubmitError("Geolocation is not supported by your browser");
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {
                const lat = position.coords.latitude.toFixed(6);
                const lon = position.coords.longitude.toFixed(6);

                setForm((prev) => ({
                    ...prev,
                    lat,
                    lon,
                }));
            },
            (error) => {
                if (error.code === error.PERMISSION_DENIED) {
                    setSubmitError("Location permission denied. Please allow location access in browser settings.");
                } else {
                    setSubmitError("Cannot access current location");
                }
            },
            {
                enableHighAccuracy: true,
                timeout: 10000,
                maximumAge: 0,
            }
        );
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setSubmitError("");
        setIsSubmitting(true);

        try {
            const payload = {
                name: form.name.trim(),
                bookId: Number(form.bookId),
                lat: form.lat.trim() ? Number(form.lat) : null,
                lon: form.lon.trim() ? Number(form.lon) : null,
                resolution: form.resolution.trim() ? Number(form.resolution) : null,
            };

            const room = await createRoom(payload);
            localStorage.setItem("lastCreatedRoom", JSON.stringify(room));

            navigate(`/rooms/${room.id}`, {
                replace: true,
                state: { room },
            });
        } catch (err) {
            setSubmitError(getApiErrorMessage(err, "Failed to create room"));
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div>
            <div className="page-header">
                <div>
                    <h1 className="page-title">Create Reading Room</h1>
                    <p className="page-description">
                        Create a room, choose a book, and start reading together.
                    </p>
                </div>
            </div>

            <div className="grid grid-2">
                <section className="card">
                    <h2 className="section-title">Room Details</h2>

                    <form className="form" onSubmit={handleSubmit}>
                        <label className="label">
                            Room Name
                            <input
                                className="input"
                                type="text"
                                name="name"
                                placeholder="Enter room name"
                                value={form.name}
                                onChange={handleChange}
                                required
                            />
                        </label>

                        <label className="label">
                            Select Book
                            <select
                                className="select"
                                name="bookId"
                                value={form.bookId}
                                onChange={handleChange}
                                disabled={isBooksLoading || books.length === 0}
                                required
                            >
                                {books.length === 0 ? <option value="">No books available</option> : null}
                                {books.map((book) => (
                                    <option key={book.id} value={book.id}>
                                        {book.title} (#{book.id})
                                    </option>
                                ))}
                            </select>
                        </label>

                        <div className="form-row">
                            <label className="label">
                                Latitude
                                <input
                                    className="input"
                                    type="number"
                                    name="lat"
                                    placeholder="43.238949"
                                    value={form.lat}
                                    onChange={handleChange}
                                    step="any"
                                />
                            </label>

                            <label className="label">
                                Longitude
                                <input
                                    className="input"
                                    type="number"
                                    name="lon"
                                    placeholder="76.889709"
                                    value={form.lon}
                                    onChange={handleChange}
                                    step="any"
                                />
                            </label>
                        </div>

                        <label className="label">
                            Resolution
                            <input
                                className="input"
                                type="number"
                                name="resolution"
                                placeholder="9"
                                value={form.resolution}
                                onChange={handleChange}
                                min="1"
                            />
                        </label>

                        {booksError ? <p className="helper-text" style={{ color: "#ef4444" }}>{booksError}</p> : null}
                        {submitError ? (
                            <p className="helper-text" style={{ color: "#ef4444" }}>
                                {submitError}
                            </p>
                        ) : null}

                        <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
                            <button className="button" type="submit" disabled={isSubmitting || isBooksLoading}>
                                {isSubmitting ? "Creating..." : "Create Room"}
                            </button>
                            <button
                                className="button button-secondary"
                                type="button"
                                onClick={handleUseMyLocation}
                            >
                                Use My Location
                            </button>
                        </div>
                    </form>
                </section>

                <section className="card">
                    <h2 className="section-title">Choose Location</h2>
                    <p className="card-text" style={{ marginBottom: 14 }}>
                        Click on the map to automatically fill latitude and longitude.
                    </p>

                    <LocationPickerMap
                        lat={form.lat}
                        lon={form.lon}
                        onSelectLocation={handleMapSelect}
                    />
                </section>

            </div>
        </div>
    );
}

export default CreateRoomPage;