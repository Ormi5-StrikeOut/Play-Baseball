// pages/resend-verify-email-page.tsx
import React, { useState } from "react";
import { useRouter } from "next/router";
import Container from "@mui/material/Container";
import Box from "@mui/material/Box";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import Alert from "@mui/material/Alert";
import FormControl from "@mui/material/FormControl";
import axios from "axios";
import { MEMBER_VERIFY_RESEND } from "@/constants/endpoints";

interface ResendVerifyEmailPageProps {
    onSubmit: (email: string) => Promise<void>;
}

const ResendVerifyEmailPage: React.FC<ResendVerifyEmailPageProps> = ({ onSubmit }) => {
    const [email, setEmail] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [alreadyVerifiedMessage, setAlreadyVerifiedMessage] = useState(''); // [수정 사항 1] 이미 인증된 이메일 메시지 상태 추가

    const router = useRouter();

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        setErrorMessage('');
        setSuccessMessage('');
        setAlreadyVerifiedMessage(''); // [수정 사항 2] 이미 인증된 이메일 메시지 초기화

        try {
            await axios.post(MEMBER_VERIFY_RESEND, null, { params: { email } });
            onSubmit(email);
            setSuccessMessage('인증 메일이 재발송되었습니다.');
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                if (error.response.status === 400 && error.response.data.message === '이미 인증된 이메일입니다.') {
                    setAlreadyVerifiedMessage('이미 인증된 이메일입니다.'); // [수정 사항 3] 이미 인증된 이메일 메시지 설정
                } else {
                    setErrorMessage('인증 메일 재발송에 실패했습니다. 다시 시도해주세요.');
                }
            } else {
                setErrorMessage('인증 메일 재발송에 실패했습니다. 다시 시도해주세요.');
            }
        }

        setIsSubmitting(false);
    };

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setEmail(e.target.value);
    };

    return (
        <Box
            display="flex"
            flexDirection="column"
            alignItems="center"
            minHeight="100vh"
            pt={12}
        >
            <Container maxWidth="sm">
                <Box mb={4}>
                    <Typography variant="h4" align="center" gutterBottom>
                        인증 메일 재발송
                    </Typography>
                </Box>

                {successMessage && (
                    <Box mb={2}>
                        <Alert severity="success" style={{ color: 'green' }}>
                            {successMessage}
                        </Alert>
                    </Box>
                )}

                {alreadyVerifiedMessage && ( // [수정 사항 4] 이미 인증된 이메일 메시지 표시
                    <Box mb={2}>
                        <Alert severity="info">
                            {alreadyVerifiedMessage}
                        </Alert>
                    </Box>
                )}

                {errorMessage && (
                    <Box mb={2}>
                        <Alert severity="error">
                            {errorMessage}
                        </Alert>
                    </Box>
                )}

                <form onSubmit={handleSubmit}>
                    <FormControl fullWidth margin="normal">
                        <TextField
                            label="Email"
                            variant="outlined"
                            fullWidth
                            name="email"
                            value={email}
                            onChange={handleChange}
                        />
                    </FormControl>

                    <Box mt={2}>
                        <Button
                            variant="contained"
                            color="primary"
                            fullWidth
                            type="submit"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? '재발송 중...' : '인증 메일 재발송'}
                        </Button>
                    </Box>
                </form>
            </Container>
        </Box>
    );
};

export default ResendVerifyEmailPage;