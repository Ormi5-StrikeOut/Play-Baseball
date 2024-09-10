import axios, { AxiosError, AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios';

const baseURL = process.env.NEXT_PUBLIC_API_URL;

if (!baseURL) {
    throw new Error('NEXT_PUBLIC_API_URL is not defined in environment variables');
}

const axiosInstance: AxiosInstance = axios.create({
    baseURL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
    withCredentials: true,
});

axiosInstance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {

        if (config.url?.includes('/verify-email')) {
            return config;
        }

        const token = localStorage.getItem('Authorization');
        if (token) {
            config.headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
        }
        return config;
    },
    (error: AxiosError) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
    (response: AxiosResponse) => {
        const newAccessToken = response.headers['authorization'];
        if (newAccessToken && typeof newAccessToken === 'string') {
            localStorage.setItem('Authorization', newAccessToken);
            axiosInstance.defaults.headers.common['Authorization'] = newAccessToken;
        }
        return response;
    },
    async (error: AxiosError) => {
        if (error.response?.status === 401 && !error.config?.url?.includes('/verify-email')) {
            localStorage.removeItem('Authorization');
            window.location.href = '/auth/login';
        }
        return Promise.reject(error);
    }
);

export const handleApiError = (error: unknown): void => {
    if (axios.isAxiosError(error)) {
        console.error('API Error:', error.response?.data);
    } else {
        console.error('Unknown Error:', error);
    }
};

export default axiosInstance;