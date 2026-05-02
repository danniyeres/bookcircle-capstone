import api from "./axios";

export const updateProgress = async (data) => {
  const res = await api.post(`/progress`, data);
  return res.data;
};
