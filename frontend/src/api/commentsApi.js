import api from "./axios";

export const fetchComments = async (roomId) => {
  const res = await api.get(`/comments`, {
    params: { roomId },
  });
  return res.data;
};

export const createComment = async (data) => {
  const res = await api.post(`/comments`, data);
  return res.data;
};
