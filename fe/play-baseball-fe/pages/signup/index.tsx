import React, { useState } from 'react';
import Container from '@mui/material/Container';
import Box from '@mui/material/Box';
import TextField from '@mui/material/TextField';
import Button from '@mui/material/Button';
import Typography from '@mui/material/Typography';
import FormControl from '@mui/material/FormControl';
import FormHelperText from '@mui/material/FormHelperText';
import InputLabel from '@mui/material/InputLabel';
import MenuItem from '@mui/material/MenuItem';
import Select from '@mui/material/Select';
import { SelectChangeEvent } from '@mui/material/Select';
import Alert from '@mui/material/Alert';
import Grid from '@mui/material/Grid';

const SignupPage: React.FC = () => {
  const [formValues, setFormValues] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    phoneFirst: '',
    phoneMiddle: '',
    phoneLast: '',
    gender: '',
  });

  const [passwordMatchError, setPasswordMatchError] = useState(false);
  const [submitError, setSubmitError] = useState('');

  const handleInputChange = (event: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = event.target;
    setFormValues({ ...formValues, [name]: value });

    // 비밀번호와 비밀번호 확인 값이 일치하는지 확인
    if (name === 'confirmPassword' || name === 'password') {
      if (name === 'confirmPassword' && value !== formValues.password) {
        setPasswordMatchError(true);
      } else if (name === 'password' && formValues.confirmPassword !== '' && value !== formValues.confirmPassword) {
        setPasswordMatchError(true);
      } else {
        setPasswordMatchError(false);
      }
    }
  };

  const handleSelectChange = (event: SelectChangeEvent) => {
    setFormValues({ ...formValues, gender: event.target.value as string });
  };

  const handleSubmit = () => {
    if (passwordMatchError || formValues.password !== formValues.confirmPassword) {
      setSubmitError('Passwords do not match.');
      return;
    }

    // TODO: 회원가입 처리 로직 추가 (예: 백엔드 API 호출)
    console.log('Form submitted', formValues);
    setSubmitError(''); // 에러 메시지 초기화
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
          회원가입 홍보 메세지
        </Typography>

        {submitError && <Alert severity="error">{submitError}</Alert>}

        <FormControl fullWidth margin="normal">
          <TextField
            label="이름"
            variant="outlined"
            fullWidth
            name="name"
            value={formValues.name}
            onChange={handleInputChange}
          />
        </FormControl>

        <FormControl fullWidth margin="normal">
          <TextField
            label="Email"
            variant="outlined"
            fullWidth
            name="email"
            value={formValues.email}
            onChange={handleInputChange}
          />
        </FormControl>

        <FormControl fullWidth margin="normal">
          <TextField
            label="비밀번호"
            type="password"
            variant="outlined"
            fullWidth
            name="password"
            value={formValues.password}
            onChange={handleInputChange}
          />
        </FormControl>

        <FormControl fullWidth margin="normal" error={passwordMatchError}>
          <TextField
            label="비밀번호 확인"
            type="password"
            variant="outlined"
            fullWidth
            name="confirmPassword"
            value={formValues.confirmPassword}
            onChange={handleInputChange}
          />
          {passwordMatchError && <FormHelperText error>비밀번호가 일치하지 않습니다.</FormHelperText>}
        </FormControl>

        <FormControl fullWidth margin="normal">
          <TextField
            label="Nickname"
            variant="outlined"
            fullWidth
            name="nickname"
            value={formValues.nickname}
            onChange={handleInputChange}
          />
        </FormControl>

        <FormControl fullWidth margin="normal">
          <FormHelperText>휴대폰 번호</FormHelperText>
          <Grid container spacing={2}>
            <Grid item xs={4}>
              <TextField
                label="010"
                variant="outlined"
                fullWidth
                name="phoneFirst"
                value={formValues.phoneFirst}
                onChange={handleInputChange}
                
              />
            </Grid>
            <Grid item xs={4}>
              <TextField
                label="0000"
                variant="outlined"
                fullWidth
                name="phoneMiddle"
                value={formValues.phoneMiddle}
                onChange={handleInputChange}
                
              />
            </Grid>
            <Grid item xs={4}>
              <TextField
                label="0000"
                variant="outlined"
                fullWidth
                name="phoneLast"
                value={formValues.phoneLast}
                onChange={handleInputChange}
                
              />
            </Grid>
          </Grid>
        </FormControl>

        
        <FormControl fullWidth margin="normal" sx={{ mt: 3 }}>
          <InputLabel id="gender-label">성별</InputLabel>
          <Select
            labelId="gender-label"
            name="gender"
            value={formValues.gender}
            onChange={handleSelectChange}
            label="성별"
            sx={{
            border: 'none',
            '&:before': { borderBottom: 'none' },
            '&:after': { borderBottom: 'none' }
            }}
        >
            <MenuItem value="MALE">남</MenuItem>
            <MenuItem value="FEMALE">여</MenuItem>
            <MenuItem value="NON_BINARY">알 수 없음</MenuItem>
          </Select>
        </FormControl>

        <Button variant="contained" color="primary" fullWidth onClick={handleSubmit}>
          회원가입
        </Button>
      </Box>
    </Container>
  );
};

export default SignupPage;
