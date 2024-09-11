import React, { useState } from "react";
import Container from "@mui/material/Container";
import Box from "@mui/material/Box";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import Alert from "@mui/material/Alert";
import FormControl from "@mui/material/FormControl";
import axios from "axios";
import { MEMBER_REQUEST_PASSWORD_RESET } from "@/constants/endpoints";

const RequestPasswordReset: React.FC = () => {
    const [email, setEmail] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [errorMessage, setErrorMessage] = useState('');
    const [successMessage, setSuccessMessage] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setIsSubmitting(true);
        setErrorMessage('');
        setSuccessMessage('');

        try {
            await axios.post(MEMBER_REQUEST_PASSWORD_RESET, { email }, { withCredentials: true });
            setSuccessMessage('패스워드 재설정 메일이 발송되었습니다.');
        } catch (error) {
            if (axios.isAxiosError(error) && error.response) {
                setErrorMessage(error.response.data.message || '비밀번호 재설정 요청에 실패했습니다.');
            } else {
                setErrorMessage('비밀번호 재설정 요청에 실패했습니다. 다시 시도해주세요.');
            }
        }

        setIsSubmitting(false);
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
                        비밀번호 재설정 요청
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
                            label="Email"
                            variant="outlined"
                            fullWidth
                            name="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
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
                            {isSubmitting ? '요청 중...' : '비밀번호 재설정 요청'}
                        </Button>
                    </Box>
                </form>
            </Container>
        </Box>
    );
};

export default RequestPasswordReset;