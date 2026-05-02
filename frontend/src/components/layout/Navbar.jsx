import { Link, useNavigate } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";

function Navbar() {
  const navigate = useNavigate();
  const token = useAuthStore((state) => state.token);
  const logout = useAuthStore((state) => state.logout);

  const handleLogout = () => {
    logout();
    navigate("/login", { replace: true });
  };

  return (
    <header className="topbar">
      <div className="brand">
        <img src="/logo.png" alt="BookCircle logo" className="logo" />
        <div>
          <div className="brand-title">BookCircle</div>
          <div className="brand-subtitle">Social reading platform</div>
        </div>
      </div>

      <div className="topbar-actions">
        {token ? (
          <button className="button button-secondary" type="button" onClick={handleLogout}>
            Logout
          </button>
        ) : (
          <>
            <Link className="button button-secondary" to="/login">
              Login
            </Link>
            <Link className="button" to="/register">
              Register
            </Link>
          </>
        )}
      </div>
    </header>
  );
}

export default Navbar;
