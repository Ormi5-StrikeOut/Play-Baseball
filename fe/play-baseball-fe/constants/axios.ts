import axios from 'axios';
import { SERVER_URL } from './endpoints'

// Create an axios instance
const api = axios.create({
    baseURL: "", // Set to nothing and use endpoints.tsx constants
//   baseURL: SERVER_URL, // Replace with your API base URL
});

// Add a request interceptor
api.interceptors.request.use(
  config => {
    // Get the token from localStorage
    const token = localStorage.getItem('Authorization');
    if (token) {
      // If token exists, add it to the headers
      config.headers['Authorization'] = token;
    }
    return config;
  },
  error => {
    // Handle the error
    return Promise.reject(error);
  }
);

export default api;