import api from "./axios";

export const fetchBooks = async (params) => {
  const res = await api.get("/books", { params });
  return res.data;
};

export const createBook = async (data) => {
  const res = await api.post("/books", data);
  return res.data;
};
