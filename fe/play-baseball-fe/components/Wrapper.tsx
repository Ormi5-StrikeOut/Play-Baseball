import React, { useEffect } from 'react';
import { useRouter } from 'next/router';
import axios from 'axios';
import { MEMBER_LOGIN, SERVER_URL } from "@/constants/endpoints";

const WrapperStyles = "w-[100%] md:w-[70%] max-w-[1300px] m-auto";

const Wrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const router = useRouter();

    useEffect(() => {
        const requestInterceptor = axios.interceptors.request.use(
            (config) => {
                const token = localStorage.getItem('Authorization');
                if (token) {
                    config.headers['Authorization'] = `Bearer ${token}`;
                }
                return config;
            },
            (error) => Promise.reject(error)
        );

        const responseInterceptor = axios.interceptors.response.use(
            (response) => response,
            async (error) => {
                const originalRequest = error.config;
                if (error.response.status === 401 && !originalRequest._retry) {
                    originalRequest._retry = true;
                    try {
                        // 리프레시 토큰으로 새 액세스 토큰 요청
                        const response = await axios.post(`${SERVER_URL}/auth/refresh`, {}, { withCredentials: true });
                        const newToken = response.data.accessToken;
                        localStorage.setItem('Authorization', newToken);
                        axios.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
                        return axios(originalRequest);
                    } catch (refreshError) {
                        // 리프레시 토큰도 만료된 경우
                        localStorage.removeItem('Authorization');
                        router.push(`${MEMBER_LOGIN}?expired=true`);
                        return Promise.reject(refreshError);
                    }
                }
                if (error.response.status === 404) {
                    // 404 에러 처리
                    router.push('/404'); // 404 페이지로 리다이렉트
                }
                return Promise.reject(error);
            }
        );

        return () => {
            axios.interceptors.request.eject(requestInterceptor);
            axios.interceptors.response.eject(responseInterceptor);
        };
    }, [router]);

    return (
        <div className={WrapperStyles}>{children}</div>
    );
}

export default Wrapper;
