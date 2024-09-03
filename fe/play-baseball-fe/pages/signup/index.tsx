import React, { useState } from "react";
import { useRouter } from "next/router";
import axios from "axios"; // Import axios
import { MEMBER_SIGNUP } from "@/constants/endpoints";
import {
  Container,
  Box,
  Typography,
  Button,
  Grid,
  TextField,
  FormControl,
  FormHelperText,
  InputLabel,
  MenuItem,
  Select,
  SelectChangeEvent,
  Alert,
  Link,
} from "@mui/material";

const SignupPage: React.FC = () => {
  const [formValues, setFormValues] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
    nickname: "",
    phoneFirst: "",
    phoneMiddle: "",
    phoneLast: "",
    gender: "",
  });

  const [errors, setErrors] = useState({
    name: false,
    email: false,
    password: false,
    confirmPassword: false,
    nickname: false,
    phoneFirst: false,
    phoneMiddle: false,
    phoneLast: false,
    gender: false,
    general: "",
  });

  const [passwordErrorMessage, setPasswordErrorMessage] = useState("");
  const router = useRouter();

  const handleInputChange = (
    event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    const { name, value } = event.target;

    // 전화번호 입력 제한 (phoneFirst는 3자리, phoneMiddle과 phoneLast는 4자리로 제한)
    if (
      (name === "phoneFirst" && value.length > 3) ||
      ((name === "phoneMiddle" || name === "phoneLast") && value.length > 4)
    ) {
      return;
    }

    setFormValues({ ...formValues, [name]: value });

    // 비밀번호와 비밀번호 확인 값이 일치하는지 확인
    if (name === "confirmPassword" || name === "password") {
      if (name === "confirmPassword" && value !== formValues.password) {
        setErrors({ ...errors, confirmPassword: true });
      } else if (
        name === "password" &&
        formValues.confirmPassword !== "" &&
        value !== formValues.confirmPassword
      ) {
        setErrors({ ...errors, confirmPassword: true });
      } else {
        setErrors({ ...errors, confirmPassword: false });
      }
    }
  };

  const handleSelectChange = (event: SelectChangeEvent) => {
    setFormValues({ ...formValues, gender: event.target.value as string });
    setErrors({ ...errors, gender: false });
  };

  const handleSubmit = async () => {
    // 초기 에러 상태 초기화
    let currentErrors = { ...errors, general: "" };
    let hasError = false;

    // 필수 필드 검사
    Object.keys(formValues).forEach((key) => {
      if (!formValues[key as keyof typeof formValues]) {
        currentErrors = { ...currentErrors, [key]: true };
        hasError = true;
      } else {
        currentErrors = { ...currentErrors, [key]: false };
      }
    });

    if (hasError) {
      setErrors(currentErrors);
      setErrors({ ...currentErrors, general: "모든 필드를 입력해주세요." });
      return;
    }

    // 이메일 형식 유효성 검사
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(formValues.email)) {
      setErrors({ ...errors, email: true });
      setErrors({ ...errors, general: "올바른 이메일 형식을 입력하세요." });
      return;
    }

    // 비밀번호 유효성 검사 (예: 최소 8자, 대문자, 소문자, 숫자, 특수문자 포함)
    const passwordRegex =
      /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!passwordRegex.test(formValues.password)) {
      setErrors({ ...errors, password: true });
      setPasswordErrorMessage(
        "비밀번호는 최소 8자 이상, 대문자, 소문자, 숫자, 특수문자를 포함해야 합니다."
      );
      setErrors({ ...errors, general: "비밀번호가 규칙에 맞지 않습니다." });
      return;
    }

    if (formValues.password !== formValues.confirmPassword) {
      setErrors({ ...errors, confirmPassword: true });
      setErrors({ ...errors, general: "비밀번호가 일치하지 않습니다." });
      return;
    }

    setErrors(currentErrors); // Reset errors before submit
    try {
      const response = await axios.post(MEMBER_SIGNUP, {
        name: formValues.name,
        email: formValues.email,
        password: formValues.password,
        nickname: formValues.nickname,
        phoneNumber: `${formValues.phoneFirst}-${formValues.phoneMiddle}-${formValues.phoneLast}`,
        gender: formValues.gender,
      });

      if (response.status === 200) {
        router.push({
          pathname: "/result",
          query: {
            isSuccess: "true",
            message: "회원가입이 성공적으로 완료되었습니다!",
            buttonText: "로그인 페이지로 이동하기",
            buttonAction: "/login",
          },
        });
      } else {
        const errorData = response.data;
        router.push({
          pathname: "/result",
          query: {
            isSuccess: "false",
            message: errorData.message || "회원가입 중 오류가 발생했습니다.",
            buttonText: "다시 시도하기",
            buttonAction: "/signup",
          },
        });
      }
    } catch (error) {
      if (axios.isAxiosError(error) && error.response) {
        const errorData = error.response.data;
        router.push({
          pathname: "/result",
          query: {
            isSuccess: "false",
            message: errorData.message || "회원가입 중 오류가 발생했습니다.",
            buttonText: "다시 시도하기",
            buttonAction: "/signup",
          },
        });
      } else {
        router.push({
          pathname: "/result",
          query: {
            isSuccess: "false",
            message: "통신 오류가 발생했습니다.",
            buttonText: "다시 시도하기",
            buttonAction: "/signup",
          },
        });
      }
    }

    console.log("Form submitted", formValues);
    setPasswordErrorMessage(""); // 에러 메시지 초기화
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
          회원가입
        </Typography>
        <Typography variant="body1" align="center" gutterBottom>
          가입을 환영합니다.
        </Typography>

        {errors.general && <Alert severity="error">{errors.general}</Alert>}

        <FormControl fullWidth margin="normal" error={errors.name}>
          <TextField
            label="이름"
            variant="outlined"
            fullWidth
            name="name"
            value={formValues.name}
            onChange={handleInputChange}
            error={errors.name}
          />
        </FormControl>

        <FormControl fullWidth margin="normal" error={errors.email}>
          <TextField
            label="Email"
            variant="outlined"
            fullWidth
            name="email"
            value={formValues.email}
            onChange={handleInputChange}
            error={errors.email}
          />
        </FormControl>

        <FormControl fullWidth margin="normal" error={errors.password}>
          <TextField
            label="비밀번호"
            type="password"
            variant="outlined"
            fullWidth
            name="password"
            value={formValues.password}
            onChange={handleInputChange}
            error={errors.password}
            helperText={passwordErrorMessage}
          />
        </FormControl>

        <FormControl fullWidth margin="normal" error={errors.confirmPassword}>
          <TextField
            label="비밀번호 확인"
            type="password"
            variant="outlined"
            fullWidth
            name="confirmPassword"
            value={formValues.confirmPassword}
            onChange={handleInputChange}
            error={errors.confirmPassword}
            helperText={
              errors.confirmPassword ? "비밀번호가 일치하지 않습니다." : ""
            }
          />
        </FormControl>

        <FormControl fullWidth margin="normal" error={errors.nickname}>
          <TextField
            label="Nickname"
            variant="outlined"
            fullWidth
            name="nickname"
            value={formValues.nickname}
            onChange={handleInputChange}
            error={errors.nickname}
          />
        </FormControl>

        <FormControl fullWidth margin="normal">
          <FormHelperText>휴대폰 번호</FormHelperText>
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <TextField
                label={formValues.phoneFirst ? "" : "010"}
                variant="outlined"
                fullWidth
                name="phoneFirst"
                value={formValues.phoneFirst}
                onChange={handleInputChange}
                error={errors.phoneFirst}
                InputLabelProps={{
                  shrink: formValues.phoneFirst ? true : false,
                }}
              />
            </Grid>
            <Grid item xs={4}>
              <TextField
                label={formValues.phoneMiddle ? "" : "0000"}
                variant="outlined"
                fullWidth
                name="phoneMiddle"
                value={formValues.phoneMiddle}
                onChange={handleInputChange}
                error={errors.phoneMiddle}
                InputLabelProps={{
                  shrink: formValues.phoneMiddle ? true : false,
                }}
              />
            </Grid>
            <Grid item xs={4}>
              <TextField
                label={formValues.phoneLast ? "" : "0000"}
                variant="outlined"
                fullWidth
                name="phoneLast"
                value={formValues.phoneLast}
                onChange={handleInputChange}
                error={errors.phoneLast}
                InputLabelProps={{
                  shrink: formValues.phoneLast ? true : false,
                }}
              />
            </Grid>
          </Grid>
        </FormControl>

        <FormControl fullWidth margin="normal" error={errors.gender}>
          <InputLabel id="gender-label">성별</InputLabel>
          <Select
            labelId="gender-label"
            name="gender"
            value={formValues.gender}
            onChange={handleSelectChange}
            label="성별"
            error={errors.gender && formValues.gender !== ""}
            sx={{
              border: "none",
              "&:before": { borderBottom: "none" },
              "&:after": { borderBottom: "none" },
            }}
          >
            <MenuItem value="MALE">남</MenuItem>
            <MenuItem value="FEMALE">여</MenuItem>
            <MenuItem value="NON_BINARY">알 수 없음</MenuItem>
          </Select>
        </FormControl>

        <Button
          variant="contained"
          color="primary"
          fullWidth
          onClick={handleSubmit}
        >
          회원가입
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

export default SignupPage;
