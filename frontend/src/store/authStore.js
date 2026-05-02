import { create } from "zustand";

export const useAuthStore = create((set) => ({
  token: localStorage.getItem("token") || null,
  userId: localStorage.getItem("userId") || null,
  role: localStorage.getItem("role") || null,
  email: localStorage.getItem("email") || null,
  phone: localStorage.getItem("phone") || null,

  login: ({ token, userId, role, email }) => {
    localStorage.setItem("token", token);
    localStorage.setItem("userId", String(userId));
    localStorage.setItem("role", role);

    if (email) {
      localStorage.setItem("email", email);
    }

    set({
      token,
      userId: String(userId),
      role,
      email: email || null,
      phone: localStorage.getItem("phone") || null,
    });
  },

  updateProfile: ({ email, phone }) => {
    localStorage.setItem("email", email);
    localStorage.setItem("phone", phone);

    set({
      email,
      phone,
    });
  },

  logout: () => {
    localStorage.removeItem("token");
    localStorage.removeItem("userId");
    localStorage.removeItem("role");
    localStorage.removeItem("email");
    localStorage.removeItem("phone");

    set({
      token: null,
      userId: null,
      role: null,
      email: null,
      phone: null,
    });
  },
}));