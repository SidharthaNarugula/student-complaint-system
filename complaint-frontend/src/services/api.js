import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true
});

// Response interceptor to handle authentication errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Only redirect to login if we truly have no authentication
    // 401: Unauthorized (no valid session)
    // 403: Forbidden (has session but lacks authorization/role)

    if (error.response?.status === 401) {
      // Truly unauthenticated - clear user data and redirect
      const storedUser = localStorage.getItem('user');
      if (storedUser) {
        localStorage.removeItem('user');
        window.location.href = '/';
      }
    }

    // 403 (authorization/role failure) should NOT cause redirect
    // Let the component handle it and display an error message

    return Promise.reject(error);
  }
);

export default api;


