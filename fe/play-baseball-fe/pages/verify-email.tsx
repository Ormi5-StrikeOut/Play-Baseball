import { useRouter } from 'next/router';
import { useEffect, useState } from 'react';
import axios from 'axios';
import { MEMBER_VERIFY, MEMBER_VERIFY_RESEND } from '@/constants/endpoints';
import ResultPage from '../pages/result/result-page';
import ResendVerifyEmailPage from '../pages/resend-verify-email'; // [수정 사항 1] 이메일 재발송 페이지 컴포넌트 추가

export default function VerifyEmail() {
    const router = useRouter();
    const { token } = router.query;
    const [verificationState, setVerificationState] = useState<{
        isSuccess: boolean;
        title: string;
        message: string;
        buttonText: string;
        buttonAction: () => void;
    } | null>(null);
    const [showResendPage, setShowResendPage] = useState(false); // [수정 사항 1] 이메일 재발송 페이지 표시 여부 상태 추가

    useEffect(() => {
        if (token) {
            verifyEmail(token as string);
        }
    }, [token]);

    const verifyEmail = async (verificationToken: string) => {
        try {
            const response = await axios.get(`${MEMBER_VERIFY}?token=${verificationToken}`);
            setVerificationState({
                isSuccess: true,
                title: "이메일 인증 성공",
                message: "이메일 인증이 성공적으로 완료되었습니다!",
                buttonText: "로그인 페이지로",
                buttonAction: () => router.push('/login')
            });
        } catch (error) {
            console.error('Verification error:', error);
            let errorMessage = '이메일 인증에 실패했습니다.';
            if (axios.isAxiosError(error) && error.response) {
                errorMessage = error.response.data.message || errorMessage;
            }
            setVerificationState({
                isSuccess: false,
                title: "이메일 인증 실패",
                message: errorMessage,
                buttonText: "인증 메일 재발송",
                buttonAction: () => setShowResendPage(true) // [수정 사항 1] prompt 대신 이메일 재발송 페이지로 이동
            });
        }
    };

    const handleResendVerification = async (email: string) => { // [수정 사항 1] 이메일을 파라미터로 받도록 변경
        try {
            const response = await axios.post(MEMBER_VERIFY_RESEND, { email });
            if (response.status === 200) {
                setVerificationState({
                    isSuccess: true,
                    title: "재발송 성공",
                    message: "인증 메일이 재발송되었습니다. 이메일을 확인해주세요.",
                    buttonText: "확인",
                    buttonAction: () => router.push('/')
                });
            }
        } catch (error) {
            console.error('Resend verification error:', error);
            setVerificationState({
                isSuccess: false,
                title: "재발송 실패",
                message: "인증 메일 재발송에 실패했습니다. 다시 시도해주세요.",
                buttonText: "홈으로 돌아가기",
                buttonAction: () => router.push('/')
            });
        }
    };

    if (showResendPage) { // [수정 사항 1] 이메일 재발송 페이지 렌더링
        return <ResendVerifyEmailPage onSubmit={handleResendVerification} />;
    }

    if (!verificationState) {
        return <div>이메일 인증 중...</div>;
    }

    return (
        <ResultPage
            isSuccess={verificationState.isSuccess}
            title={verificationState.title}
            message={verificationState.message}
            buttonText={verificationState.buttonText}
            buttonAction={verificationState.buttonAction}
        />
    );
}