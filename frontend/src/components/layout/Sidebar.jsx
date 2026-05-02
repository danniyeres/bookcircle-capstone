import { NavLink } from "react-router-dom";
import { useAuthStore } from "../../store/authStore";

function Sidebar() {
  const role = useAuthStore((state) => state.role);
  const isAdmin = role === "ADMIN";

  const linkClass = ({ isActive }) =>
    isActive ? "top-nav-link active" : "top-nav-link";

  return (
    <nav className="top-nav">
      <NavLink to="/" end className={linkClass}>
        Dashboard
      </NavLink>

      <NavLink to="/books" className={linkClass}>
        Books
      </NavLink>

      <NavLink to="/rooms" end className={linkClass}>
        Rooms
      </NavLink>

      <NavLink to="/rooms/create" className={linkClass}>
        Create Room
      </NavLink>

      <NavLink to="/profile" className={linkClass}>
        Profile
      </NavLink>

      {isAdmin ? (
        <NavLink to="/admin" className={linkClass}>
          Admin
        </NavLink>
      ) : null}
    </nav>
  );
}

export default Sidebar;