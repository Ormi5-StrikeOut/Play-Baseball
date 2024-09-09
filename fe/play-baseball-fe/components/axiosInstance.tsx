import axios, {AxiosError, AxiosInstance, AxiosResponse, InternalAxiosRequestConfig} from 'axios';

const baseURL = process.env.NEXT_PUBLIC_API_URL || 'https://localhost:8000';

interface ErrorResponseData {
    tokenRefreshed?: boolean;
    // 필요한 다른 속성들을 추가로 정의
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
            // 'Bearer' 키워드 중복 방지
            config.headers['Authorization'] = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
        } else if (!config.url?.includes('/auth/login')) {
            console.warn('No token found in localStorage');
            window.location.href = '/auth/login';
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
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        if (error.response?.status === 401 && (error.response.data as ErrorResponseData)?.tokenRefreshed === true) {
            if (originalRequest._retry) {
                return Promise.reject(error);
            }

            originalRequest._retry = true;
            const newToken = error.response.headers['authorization'];
            if (newToken) {
                localStorage.setItem('Authorization', newToken);
                axiosInstance.defaults.headers.common['Authorization'] = newToken;
            }

            // 원래 요청 객체 복제
            const newRequest = {
                ...originalRequest,
                headers: {
                    ...originalRequest.headers,
                    Authorization: newToken,
                },
            };

            // 새로운 요청 객체로 요청 재시도
            return axiosInstance(newRequest);
        }

        // 401 에러 처리 (토큰 만료)
        if (error.response?.status === 401) {
            // HttpOnly 쿠키 확인
            const cookies = document.cookie.split('; ');
            const refreshToken = cookies.find((cookie) => cookie.startsWith('refreshToken='));

            if (!refreshToken) {
                // HttpOnly 쿠키가 없는 경우, 로그인 페이지로 이동
                localStorage.removeItem('Authorization');
                window.location.href = '/auth/login';
                return Promise.reject(error);
            }
        }

        return Promise.reject(error);
    }
);

export const handleApiError = (error: unknown) => {
    if (axios.isAxiosError(error)) {
        console.error('API Error:', error.response?.data);
    } else {
        console.error('Unknown Error:', error);
    }
};

export default axiosInstance;