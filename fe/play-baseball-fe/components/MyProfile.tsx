import React from 'react';
import {
    Grid,
    Paper,
    Typography,
    Avatar,
    Box,
    TextField,
    Button,
    Checkbox,
    FormControlLabel,
    FormControl,
    InputLabel,
    MenuItem,
    Alert
} from '@mui/material';
import {User} from '@/constants/types';
import Select, {SelectChangeEvent} from '@mui/material/Select';

interface MyProfileProps {
    user: User;
    setUser: React.Dispatch<React.SetStateAction<User>>;
    onSubmit: (user: User) => void;
    onDeleteAccount: (user: User) => void;
    onResendVerification: () => Promise<void>;
    resendStatus: string;
    isResending: boolean;
}

const MyProfile: React.FC<MyProfileProps> = ({
                                                 user,
                                                 setUser,
                                                 onSubmit,
                                                 onDeleteAccount,
                                                 onResendVerification,
                                                 resendStatus,
                                                 isResending
                                             }) => {
    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const {name, value} = e.target;
        setUser((prevUser: User) => ({
            ...prevUser,
            [name as string]: value,
        }));
    };

    const handleSelectChange = (e: SelectChangeEvent) => {
        const name = e.target.name;
        const value = e.target.value;
        setUser((prevUser: User) => ({
            ...prevUser,
            [name]: value,
        }));
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        onSubmit(user);
    };

    const handleDelete = () => {
        onDeleteAccount(user);
    };

    const handleResendVerification = async () => {
        if (user.emailVerified) {
            alert('이미 인증이 완료된 이메일입니다.');
            return;
        }
        await onResendVerification();
    };

    console.log('emailVerified:', user.emailVerified);

    return (
        <Box sx={{flexGrow: 1, padding: 3}}>
            <Grid container spacing={3} justifyContent="center">
                <Grid item xs={12} md={4}>
                    <Paper elevation={3} sx={{padding: 2}}>
                        <Box sx={{textAlign: 'center'}}>
                            <Avatar
                                src={"./assets/profile_placeholder.jpg"}
                                alt={`${user.nickname}'s avatar`}
                                sx={{width: 100, height: 100, margin: '0 auto'}}
                            />
                        </Box>
                    </Paper>
                </Grid>
                <Grid item xs={12} md={8}>
                    <Paper elevation={3} sx={{padding: 2}}>
                        <form onSubmit={handleSubmit}>
                            <Typography variant="h6" gutterBottom>
                                My Profile
                            </Typography>
                            <TextField
                                fullWidth
                                margin="normal"
                                label="Email"
                                name="email"
                                type="email"
                                value={user.email}
                                onChange={handleChange}
                                disabled
                            />
                            <TextField
                                fullWidth
                                margin="normal"
                                label="Nickname"
                                name="nickname"
                                value={user.nickname}
                                onChange={handleChange}
                            />
                            <TextField
                                fullWidth
                                margin="normal"
                                label="Phone no."
                                name="phoneNumber"
                                value={user.phoneNumber}
                                onChange={handleChange}
                            />
                            <FormControl fullWidth margin="normal">
                                <InputLabel id="gender">Gender</InputLabel>
                                <Select
                                    labelId="gender"
                                    name="gender"
                                    value={user.gender}
                                    onChange={handleSelectChange}
                                    label="Gender"
                                >
                                    <MenuItem value={"MALE"}>MALE</MenuItem>
                                    <MenuItem value={"FEMALE"}>FEMALE</MenuItem>
                                </Select>
                            </FormControl>
                            <TextField
                                fullWidth
                                margin="normal"
                                label="Role"
                                name="role"
                                value={user.role}
                                disabled
                                onChange={handleChange}
                            />
                            <FormControlLabel
                                control={
                                    <Checkbox
                                        checked={Boolean(user.emailVerified)}
                                        disabled
                                        sx={{
                                            color: user.emailVerified ? 'primary.main' : 'grey.500',
                                            '&.Mui-checked': {
                                                color: 'primary.main',
                                            },
                                        }}
                                    />
                                }
                                label="Email Verified"
                            />
                            {!user.emailVerified && (
                                <Box mt={2}>
                                    <Button
                                        variant="outlined"
                                        color="primary"
                                        onClick={handleResendVerification}
                                        disabled={isResending}
                                    >
                                        {isResending ? '이메일 발송 중...' : '이메일 인증 재발송'}
                                    </Button>
                                    {resendStatus && (
                                        <Alert severity="info" sx={{mt: 1}}>
                                            {resendStatus}
                                        </Alert>
                                    )}
                                </Box>
                            )}
                            <Box className="italic" mt={2}>
                                <span>Member since: {user.createdAt.substring(0, 10)}</span>
                            </Box>
                            <Button variant="contained" color="primary" type="submit" sx={{mt: 2}}>
                                저장
                            </Button>
                        </form>
                        <Button variant="contained" color="secondary" onClick={handleDelete} sx={{mt: 2}}>
                            회원탈퇴
                        </Button>
                    </Paper>
                </Grid>
            </Grid>
        </Box>
    );
};

export default MyProfile;