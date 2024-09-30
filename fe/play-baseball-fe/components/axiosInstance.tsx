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
        if (newAccessToken) {
            localStorage.setItem('Authorization', newAccessToken);
            axiosInstance.defaults.headers.common['Authorization'] = newAccessToken;
        }
        return response;
    },
    (error: AxiosError) => {
        // 여기서는 401 에러 처리를 제거합니다.
        // 서버가 자동으로 새 액세스 토큰을 제공하므로 클라이언트에서 추가 처리가 필요 없습니다.
        return Promise.reject(error);
    }
);

export const handleApiError = (error: unknown): void => {
    if (axios.isAxiosError(error)) {
        console.error('API Error:', error.response?.data);
        // 여기서 토큰 관련 오류를 확인하고 필요한 경우 로그아웃 처리를 할 수 있습니다.
        if (error.response?.status === 401) {
            localStorage.removeItem('Authorization');
            window.location.href = '/auth/login';
        }
    } else {
        console.error('Unknown Error:', error);
    }
};

export default axiosInstance;