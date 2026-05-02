import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getCurrentUser, updateCurrentUser } from "../api/authApi";
import { useAuthStore } from "../store/authStore";
import { getApiErrorMessage } from "../utils/getApiErrorMessage";

function ProfilePage() {
  const navigate = useNavigate();

  const email = useAuthStore((state) => state.email);
  const phone = useAuthStore((state) => state.phone);
  const role = useAuthStore((state) => state.role);
  const userId = useAuthStore((state) => state.userId);
  const logout = useAuthStore((state) => state.logout);
  const updateProfile = useAuthStore((state) => state.updateProfile);

  const [form, setForm] = useState({
    email: email || "",
    phone: phone || "",
  });

  const [success, setSuccess] = useState("");
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isFetchingProfile, setIsFetchingProfile] = useState(false);

  useEffect(() => {
        const fetchProfile = async () => {
            setIsFetchingProfile(true);
            setError("");

            try {
                const response = await getCurrentUser();
                const nextEmail = response?.email || "";
                const nextPhone = response?.phoneNumber || "";

                setForm({
                    email: nextEmail,
                    phone: nextPhone,
                });

                updateProfile({
                    email: nextEmail,
                    phone: nextPhone,
                });
            } catch (err) {
                const message = getApiErrorMessage(err, "Failed to load profile");
                setError(message);
            } finally {
                setIsFetchingProfile(false);
            }
        };

        fetchProfile();
    }, [updateProfile]);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));

    setSuccess("");
    setError("");
  };

    const handleSave = async () => {
        setSuccess("");
        setError("");
        setIsLoading(true);

        const payload = {
            email: form.email.trim(),
            phoneNumber: form.phone.trim(),
        };

        try {
            const response = await updateCurrentUser(payload);
            const nextEmail = response?.email || payload.email;
            const nextPhone = response?.phoneNumber || payload.phoneNumber;

            updateProfile({
                email: nextEmail,
                phone: nextPhone,
            });

            setForm({
                email: nextEmail,
                phone: nextPhone,
            });
            setSuccess("Profile saved successfully");
        } catch (err) {
            const message = getApiErrorMessage(err, "Failed to update profile");
            setError(message);
        } finally {
            setIsLoading(false);
        }
    };

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Profile</h1>
          <p className="page-description">
            View and update your account details.
          </p>
        </div>

        <span className="badge">My Account</span>
      </div>

      <div className="grid grid-2">
        <section className="card">
          <h2 className="section-title">Account Information</h2>

          <div className="grid">
            <label className="label">
              Email
              <input
                className="input"
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                placeholder="Enter your email"
              />
            </label>

            <label className="label">
              Phone Number
              <input
                className="input"
                type="tel"
                name="phone"
                value={form.phone}
                onChange={handleChange}
                placeholder="+7 777 777 77 77"
              />
            </label>

            <div className="comment-item">
              <strong>Role</strong>
              <p className="card-text" style={{ marginTop: 6 }}>
                {role}
              </p>
            </div>

            <div className="comment-item">
              <strong>User ID</strong>
              <p className="card-text" style={{ marginTop: 6 }}>
                {userId}
              </p>
            </div>
          </div>
        </section>

        <section className="card">
          <h2 className="section-title">Membership</h2>

          <div className="grid">
            <div className="comment-item">
              <strong>Access Level</strong>
              <p className="card-text" style={{ marginTop: 6 }}>
                {role}
              </p>
            </div>

            <div className="comment-item">
              <strong>Profile ID</strong>
              <p className="card-text" style={{ marginTop: 6 }}>
                {userId}
              </p>
            </div>

            <div className="comment-item">
              <strong>Phone</strong>
              <p className="card-text" style={{ marginTop: 6 }}>
                {form.phone || "Not added yet"}
              </p>
            </div>
          </div>
        </section>
      </div>

      <section className="card" style={{ marginTop: 20 }}>
        <h2 className="section-title">Actions</h2>

        {success ? (
          <p className="helper-text" style={{ color: "#16a34a" }}>
            {success}
          </p>
        ) : null}

          {error ? (
              <p className="helper-text" style={{ color: "#ef4444" }}>
                  {error}
              </p>
          ) : null}

          <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
              <button
                  className="button"
                  type="button"
                  onClick={handleSave}
                  disabled={isLoading || isFetchingProfile}
              >
                  {isLoading ? "Saving..." : "Save"}
              </button>

          <button
            className="button button-secondary"
            type="button"
            onClick={handleLogout}
          >
            Logout
          </button>
        </div>
      </section>
    </div>
  );
}

export default ProfilePage;