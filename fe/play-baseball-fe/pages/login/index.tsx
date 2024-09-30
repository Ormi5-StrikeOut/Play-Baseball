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
import axios from "axios";
import { MEMBER_LOGIN, SERVER_URL } from "@/constants/endpoints";
import Wrapper from "../../components/Wrapper";

const LoginPage: React.FC = () => {
  const [formValues, setFormValues] = useState({
    email: "",
    password: "",
  });

  const [error, setError] = useState("");

  const router = useRouter();

  const handleLogin = async () => {
    setError("");
    try {
      const response = await axios.post(
          MEMBER_LOGIN,
          {
            email: formValues.email,
            password: formValues.password,
          },
          {
            withCredentials: true,
          }
      );

      if (response.status === 200) {
        const token = response.headers["authorization"];
        if (token) {
          localStorage.setItem("Authorization", token);
          if (SERVER_URL === undefined) {
            await router.push("/");
          } else {
            window.location.href = SERVER_URL;
          }
        } else {
          setError("로그인 처리 중 오류가 발생했습니다. 다시 시도해 주세요.");
        }
      }
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        if (error.response.status === 401) {
          setError("이메일 주소나 비밀번호가 올바르지 않습니다. 다시 확인해 주세요.");
        } else {
          setError("로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
        }
      } else {
        setError("알 수 없는 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
      }
      console.error("로그인 요청 실패", error);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormValues({
      ...formValues,
      [e.target.name]: e.target.value,
    });
  };

  return (
      <Wrapper>
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

            {error && (
                <Alert severity="error" sx={{ width: '100%', mb: 2 }}>
                  {error}
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
                sx={{ mt: 2 }}
            >
              로그인
            </Button>

            <Box
                mt={2}
                display="flex"
                justifyContent="center"
                alignItems="center"
            >
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
      </Wrapper>
  );
};

export default LoginPage;