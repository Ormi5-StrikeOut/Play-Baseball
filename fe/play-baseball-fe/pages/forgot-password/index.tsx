import React, { useState } from "react";
import Container from "@mui/material/Container";
import Box from "@mui/material/Box";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import FormControl from "@mui/material/FormControl";
import Link from "@mui/material/Link";

const ForgotPasswordPage: React.FC = () => {
  const [email, setEmail] = useState("");
  const handleEmailChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setEmail(event.target.value);
  };

  const handleResetPassword = () => {
    // TODO: 비밀번호 재설정 로직 추가
  };

  return (
    <Container maxWidth="sm">
      <Box
        display="flex"
        flexDirection="column"
        alignItems="center"
        justifyContent="center"
        minHeight="100vh"
      >
        <Typography variant="h5" gutterBottom>
          비밀번호 찾기
        </Typography>
        <Typography variant="body1" align="center" gutterBottom>
          가입한 이메일을 입력해 주세요.
          <br />
          이메일을 통해 비밀번호 변경 링크가 전송됩니다.
        </Typography>

        <FormControl fullWidth margin="normal">
          <TextField
            label="Email"
            variant="outlined"
            fullWidth
            value={email}
            onChange={handleEmailChange}
          />
        </FormControl>

        <Button
          variant="contained"
          color="primary"
          fullWidth
          onClick={handleResetPassword}
        >
          변경 링크 전송하기
        </Button>

        <Box mt={2} display="flex" justifyContent="center" alignItems="center">
          <Link href="/login" variant="body2" sx={{ mx: 2 }}>
            로그인 페이지로 이동하기
          </Link>
        </Box>
      </Box>
    </Container>
  );
};

export default ForgotPasswordPage;
