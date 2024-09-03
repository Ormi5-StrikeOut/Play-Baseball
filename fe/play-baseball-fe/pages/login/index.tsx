import React, { useState } from "react";
import { useRouter } from "next/router";
import Container from "@mui/material/Container";
import Box from "@mui/material/Box";
import TextField from "@mui/material/TextField";
import Button from "@mui/material/Button";
import Typography from "@mui/material/Typography";
import Link from "@mui/material/Link";
import Alert from "@mui/material/Alert";
import FormControl from "@mui/material/FormControl";
import { MEMBER_LOGIN } from "@/constants/endpoints";

const LoginPage: React.FC = () => {
  const [formValues, setFormValues] = useState({
    email: "",
    password: "",
  });

  const [showError, setShowError] = useState(false);

  const router = useRouter();

  const handleLogin = async () => {
    setShowError(false);
    try {
      const response = await fetch(MEMBER_LOGIN, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          email: formValues.email,
          password: formValues.password,
        }),
      });

      if (response.ok) {
        const token = response.headers.get("Authorization");
        if (token) {
          localStorage.setItem("Authorization", token);
          router.push({
            pathname: "/",
          });
        } else {
          throw new Error("token 생성 오류");
        }
      } else {
        setShowError(true);
      }
    } catch {
      router.push({
        pathname: "/result",
        query: {
          isSuccess: "false",
          message: "통신 오류가 발생했습니다.",
          buttonText: "다시 시도하기",
          buttonAction: "/login",
        },
      });
    }

    console.log("Form submitted", formValues);
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormValues({
      ...formValues,
      [e.target.name]: e.target.value,
    });
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

        {showError && (
          <Alert severity="error">
            이메일 또는 비밀번호를 다시 입력해주세요.
          </Alert>
        )}
        <FormControl fullWidth margin="normal">
          <TextField
            label="Email"
            variant="outlined"
            fullWidth
            name="email"
            value={formValues.email}
            onChange={handleChange}
          />
        </FormControl>

        <FormControl fullWidth margin="normal">
          <TextField
            label="Password"
            type="password"
            variant="outlined"
            fullWidth
            name="password"
            value={formValues.password}
            onChange={handleChange}
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
