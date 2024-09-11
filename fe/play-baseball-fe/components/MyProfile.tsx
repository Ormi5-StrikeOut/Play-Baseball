import React, { useEffect, useState } from 'react';
import{ Grid, Paper, Typography, Avatar, Box, TextField, Button, Checkbox, FormControlLabel, FormControl, InputLabel, MenuItem } from '@mui/material';
import { User } from '@/constants/types';
import Select, { SelectChangeEvent } from '@mui/material/Select';

// const initialUser = {
//     id: -1,
//     email: "",
//     nickname: "",
//     role: "",
//     createdAt: NaN,
//     updatedAt: NaN,
//     lastLoginDate: NaN,
//     deletedAt: NaN,
//     emailVerified: false
// };

interface MyProfileProps {
    user: User;
    setUser: React.Dispatch<React.SetStateAction<User>>;
    onSubmit: (user: User) => void;
    onDeleteAccount: (user: User) => void;
}

const MyProfile: React.FC<MyProfileProps> = ({ user, setUser, onSubmit, onDeleteAccount }) => { 
    // const [joinedString, setJoinedString] = useState("");

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
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
        console.log('User Profile Updated:', user);
    };

    const handleDelete = () => {
        onDeleteAccount(user);
        console.log('User Profile deleted:', user);
    };


    // useEffect(() => {
    //     // const formatted = user.createdAt.toLocaleDateString('en-US', {
    //     //     year: 'numeric',
    //     //     month: 'long',
    //     //     day: 'numeric'
    //     // });
    //     // setJoinedString(formatted);
    // }, [user.createdAt]);

    return (
        <Box sx={{ flexGrow: 1, padding: 3 }}>
            <Grid container spacing={3} justifyContent="center">
                <Grid item xs={12} md={4}>
                    <Paper elevation={3} sx={{ padding: 2 }}>
                        <Box sx={{ textAlign: 'center' }}>
                            <Avatar
                                src={"./assets/profile_placeholder.jpg"}
                                alt={`${user.nickname}'s avatar`}
                                sx={{ width: 100, height: 100, margin: '0 auto' }}
                            />
                        </Box>
                    </Paper>
                </Grid>
                <Grid item xs={12} md={8}>
                    <Paper elevation={3} sx={{ padding: 2 }}>
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
                                name="phonenumber"
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
                            <FormControlLabel control={<Checkbox
                                value={user.emailVerified} disabled
                            />} label="Email Verified" />
                            
                            <Box className="italic">
                                <span>Member since: {user.createdAt.substring(0, 10)}</span>
                            </Box>
                            <Button variant="contained" color="primary" type="submit" sx={{ mt: 2 }}>
                                저장
                            </Button>
                        </form>
                        <Button variant="contained" color="secondary" onClick={handleDelete} sx={{ mt: 2 }}>
                                회원탈퇴
                            </Button>
                    </Paper>
                </Grid>
            </Grid>
        </Box>
    );
};

export default MyProfile;
