import React from "react";
import Container from "@mui/material/Container";
import Box from "@mui/material/Box";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import Link from "@mui/material/Link";
import Alert from "@mui/material/Alert";
import Divider from "@mui/material/Divider";
import FormControl from "@mui/material/FormControl";
// import FormHelperText from '@mui/material/FormHelperText';

const LoginPage: React.FC = () => {
  const handleLogin = () => {
    // TODO: 로그인 처리 로직 추가
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
        <Typography variant="h4" gutterBottom>
          로그인
        </Typography>

        <Alert severity="error">
          이메일 또는 비밀번호를 다시 입력해주세요.
        </Alert>

        <FormControl fullWidth margin="normal">
          <TextField label="Email" variant="outlined" fullWidth />
        </FormControl>

        <FormControl fullWidth margin="normal">
          <TextField
            label="Password"
            type="password"
            variant="outlined"
            fullWidth
          />
        </FormControl>

        <Button
          variant="contained"
          color="primary"
          fullWidth
          onClick={handleLogin}
        >
          로그인
        </Button>

        <Box mt={2} display="flex" justifyContent="center" alignItems="center">
          <Link href="/signup" variant="body2" sx={{ mx: 2 }}>
            회원가입
          </Link>
          <Typography variant="body2" sx={{ mx: 1 }}>
            |
          </Typography>
          <Link href="/forgot-password" variant="body2" sx={{ mx: 2 }}>
            비밀번호 찾기
          </Link>
        </Box>
      </Box>
    </Container>
  );
};

export default LoginPage;
