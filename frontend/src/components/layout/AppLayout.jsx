import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";
import Sidebar from "./Sidebar";

function AppLayout() {
  return (
    <div className="app-shell">
      <Navbar />
      <Sidebar />

      <main className="page">
        <Outlet />
        <div className="footer">
          BookCircle • Read together, chapter by chapter
        </div>
      </main>
    </div>
  );
}

export default AppLayout;