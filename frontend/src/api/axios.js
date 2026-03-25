import axios from "axios";

// Force localhost for development - comment this out for production
const API_URL = "http://localhost:8081";

// Uncomment this for production (uses .env file)
// const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8081";

const axiosInstance = axios.create({
  baseURL: API_URL,
  withCredentials: true,
});

export default axiosInstance;
