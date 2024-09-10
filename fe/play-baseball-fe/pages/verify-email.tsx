import { useRouter } from 'next/router';
import { useEffect, useState } from 'react';
import axios from 'axios';
import { MEMBER_VERIFY } from '@/constants/endpoints';

export default function VerifyEmail() {
    const router = useRouter();
    const { token } = router.query;
    const [status, setStatus] = useState('Verifying...');

    useEffect(() => {
        if (token) {
            verifyEmail(token as string);
        }
    }, [token]);

    const verifyEmail = async (verificationToken: string) => {
        try {
            console.log('Verifying token:', verificationToken);
            // MEMBER_VERIFY 엔드포인트 사용
            const response = await axios.get(`${MEMBER_VERIFY}?token=${verificationToken}`);
            console.log('Verification response:', response);
            setStatus('Email verified successfully!');
        } catch (error) {
            console.error('Verification error:', error);
            if (axios.isAxiosError(error)) {
                if (error.response) {
                    setStatus(`Verification failed: ${error.response.data.message || 'Unknown error'}`);
                } else if (error.request) {
                    setStatus('Unable to reach the server. Please check your internet connection.');
                } else {
                    setStatus('An error occurred while sending the request.');
                }
            } else {
                setStatus('An unexpected error occurred.');
            }
        }
    };

    return <div>{status}</div>;
}