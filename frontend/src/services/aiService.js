import axiosInstance from "../api/axios";

const fallbackError = "Unable to generate content, please try again.";

const getApiData = (response) => response?.data?.data ?? response?.data ?? {};

const getErrorMessage = (error, defaultMessage = fallbackError) =>
  error?.response?.data?.message ||
  error?.response?.data?.error ||
  error?.message ||
  defaultMessage;

export const aiService = {
  getErrorMessage,

  async generateResumeSummary(payload) {
    const response = await axiosInstance.post(
      "/api/ai/resume/summary",
      payload,
    );
    return getApiData(response)?.content ?? "";
  },

  async enhanceResumeExperience(payload) {
    const response = await axiosInstance.post(
      "/api/ai/resume/experience",
      payload,
    );
    return getApiData(response)?.content ?? "";
  },

  async enhanceProjectDescription(payload) {
    const response = await axiosInstance.post(
      "/api/ai/resume/project",
      payload,
    );
    return getApiData(response)?.content ?? "";
  },

  async suggestResumeSkills(payload) {
    const response = await axiosInstance.post("/api/ai/resume/skills", payload);
    return getApiData(response);
  },

  async generateCoverLetter(payload) {
    const response = await axiosInstance.post("/api/ai/cover-letter", payload);
    return getApiData(response);
  },

  async chat(payload) {
    const response = await axiosInstance.post("/api/ai/chat", payload);
    return getApiData(response);
  },
};
