import api from "./axios";

export const fetchRoomStats = async (roomId) => {
  const res = await api.get(`/admin/stats/${roomId}`);
  return res.data;
};

export const fetchAuditLog = async () => {
  const res = await api.get("/admin/audit");
  return res.data;
};
