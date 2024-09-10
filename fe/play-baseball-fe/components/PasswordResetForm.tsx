import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useRouter } from 'next/router';
import { Box, Button, TextField, Typography, Alert, Container, FormControl } from '@mui/material';
import { MEMBER_RESET_PASSWORD } from '@/constants/endpoints';

interface PasswordResetFormProps {
    token: string;
}

const PasswordResetForm: React.FC<PasswordResetFormProps> = ({ token }) => {
    const [newPassword, setNewPassword] = useState('');
    const [confirmNewPassword, setConfirmNewPassword] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');
    const [isTokenValid, setIsTokenValid] = useState(true);

    const router = useRouter();

    useEffect(() => {
        const validateToken = async () => {
            try {
                await axios.get(`${MEMBER_RESET_PASSWORD}?token=${token}`, { withCredentials: true });
                setIsTokenValid(true);
            } catch (error) {
                setIsTokenValid(false);
                setErrorMessage('토큰이 만료되었거나 유효하지 않습니다.');
            }
        };

        validateToken();
    }, [token]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (newPassword !== confirmNewPassword) {
            setErrorMessage('비밀번호가 일치하지 않습니다.');
            return;
        }
        setIsSubmitting(true);

        try {
            await axios.patch(`${MEMBER_RESET_PASSWORD}?token=${token}`, {
                newPassword,
                confirmNewPassword,
            }, { withCredentials: true });
            setSuccessMessage('비밀번호가 성공적으로 재설정되었습니다.');
            setTimeout(() => router.push('/login'), 3000);
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                setErrorMessage(error.response.data.message || '비밀번호 재설정에 실패했습니다.');
            } else {
                setErrorMessage('비밀번호 재설정에 실패했습니다. 다시 시도해주세요.');
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    if (!isTokenValid) {
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
                            토큰 만료
                        </Typography>
                    </Box>
                    <Alert severity="error" sx={{ width: '100%', mb: 2 }}>
                        {errorMessage}
                    </Alert>
                    <Button variant="contained" color="primary" fullWidth onClick={() => router.push('/login')}>
                        로그인 페이지로 이동
                    </Button>
                </Container>
            </Box>
        );
    }

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
                        비밀번호 재설정
                    </Typography>
                </Box>

                {successMessage && (
                    <Box mb={2}>
                        <Alert severity="success">
                            {successMessage}
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
                            label="새 비밀번호"
                            type="password"
                            fullWidth
                            value={newPassword}
                            onChange={(e) => setNewPassword(e.target.value)}
                            required
                        />
                    </FormControl>
                    <FormControl fullWidth margin="normal">
                        <TextField
                            label="새 비밀번호 확인"
                            type="password"
                            fullWidth
                            value={confirmNewPassword}
                            onChange={(e) => setConfirmNewPassword(e.target.value)}
                            required
                        />
                    </FormControl>
                    <Box mt={2}>
                        <Button
                            type="submit"
                            fullWidth
                            variant="contained"
                            color="primary"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? '재설정 중...' : '비밀번호 재설정'}
                        </Button>
                    </Box>
                </form>
            </Container>
        </Box>
    );
};

export default PasswordResetForm;