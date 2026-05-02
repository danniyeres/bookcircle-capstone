import api from "./axios";

export const fetchRooms = async () => {
  const res = await api.get("/rooms");
  return res.data;
};

export const createRoom = async (data) => {
  const res = await api.post("/rooms", data);
  return res.data;
};

export const joinRoom = async (roomId) => {
  const res = await api.post(`/rooms/${roomId}/join`);
  return res.data;
};

export const fetchRoomsByH3 = async (h3Index) => {
  const res = await api.get("/rooms/by-h3", {
    params: { h3Index },
  });
  return res.data;
};
