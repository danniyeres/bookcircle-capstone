import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { getCurrentUser, registerUser } from "../api/authApi";
import { useAuthStore } from "../store/authStore";
import { getApiErrorMessage } from "../utils/getApiErrorMessage";

function RegisterPage() {
    const navigate = useNavigate();
    const login = useAuthStore((state) => state.login);
    const token = useAuthStore((state) => state.token);

    const [form, setForm] = useState({
        email: "",
        password: "",
    });
    const [error, setError] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        if (token) {
            navigate("/", { replace: true });
        }
    }, [navigate, token]);

    const handleChange = (event) => {
        const { name, value } = event.target;
        setForm((prev) => ({
            ...prev,
            [name]: value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();
        setError("");
        setIsLoading(true);

        try {
            const response = await registerUser(form);

            if (!response?.accessToken || !response?.userId || !response?.role) {
                throw new Error("Unexpected server response");
            }

            let profile;

            try {
                profile = await getCurrentUser();
            } catch {
                profile = null;
            }

            login({
                token: response.accessToken,
                userId: response.userId,
                role: response.role,
                email: profile?.email || form.email,
                phone: profile?.phoneNumber || null,
            });


            navigate("/", { replace: true });
        } catch (err) {
            const message = getApiErrorMessage(err, "Register failed");
            setError(message);
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-card">
                <div className="brand" style={{ marginBottom: 20 }}>
                    <img src="/logo.png" alt="BookCircle logo" className="logo" />
                    <div>
                        <div className="brand-title">BookCircle</div>
                        <div className="brand-subtitle">Create your reading identity</div>
                    </div>
                </div>

                <h1 className="auth-title">Create account</h1>
                <p className="auth-text">
                    Join the reading community, create rooms, and participate in structured
                    book discussions.
                </p>

                <form className="form" onSubmit={handleSubmit}>
                    <label className="label">
                        Email
                        <input
                            className="input"
                            type="email"
                            name="email"
                            placeholder="Enter your email"
                            value={form.email}
                            onChange={handleChange}
                            required
                        />
                    </label>

                    <label className="label">
                        Password
                        <input
                            className="input"
                            type="password"
                            name="password"
                            placeholder="Create a password"
                            value={form.password}
                            onChange={handleChange}
                            required
                        />
                    </label>

                    <button className="button" type="submit" disabled={isLoading}>
                        {isLoading ? "Registering..." : "Register"}
                    </button>

                    {error ? (
                        <p className="helper-text" style={{ color: "#ef4444", marginTop: 8 }}>
                            {error}
                        </p>
                    ) : null}
                </form>

                <p className="helper-text" style={{ marginTop: 18 }}>
                    Already have an account? <Link to="/login">Sign in</Link>
                </p>
            </div>
        </div>
    );
}

export default RegisterPage;