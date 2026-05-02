export const getApiErrorMessage = (error, fallback = "Request failed") => {
  if (error?.response?.data?.error) {
    return error.response.data.error;
  }

  if (typeof error?.response?.data === "string" && error.response.data.trim()) {
    return error.response.data;
  }

  if (error?.message) {
    return error.message;
  }

  return fallback;
};
