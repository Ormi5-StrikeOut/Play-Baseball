import React, { useEffect, useState } from 'react';
import{ Grid, Paper, Typography, Avatar, Box, TextField, Button, Checkbox, FormControlLabel } from '@mui/material';
import { User } from '@/constants/types';

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
}

const MyProfile: React.FC<MyProfileProps> = ({ user }) => { 
    const [userState, setUser] = useState(user);
    const [joinedString, setJoinedString] = useState("");

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setUser((prevUser) => ({
            ...prevUser,
            [name]: value,
        }));
    };

    const handleSubmit = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        // Handle form submission logic here (e.g., send data to server)
        console.log('User Profile Updated:', userState);
    };

    useEffect(() => {
        const formatted = userState.createdAt.toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
        setJoinedString(formatted);
    }, [userState.createdAt]);

    return (
        <Box sx={{ flexGrow: 1, padding: 3 }}>
            <Grid container spacing={3} justifyContent="center">
                <Grid item xs={12} md={4}>
                    <Paper elevation={3} sx={{ padding: 2 }}>
                        <Box sx={{ textAlign: 'center' }}>
                            <Avatar
                                src={"./assets/profile_placeholder.jpg"}
                                alt={`${userState.nickname}'s avatar`}
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
                                value={userState.email}
                                onChange={handleChange}
                            />
                            <TextField
                                fullWidth
                                margin="normal"
                                label="Nickname"
                                name="nickname"
                                value={userState.nickname}
                                onChange={handleChange}
                            />
                            <TextField
                                fullWidth
                                margin="normal"
                                label="Role"
                                name="role"
                                value={userState.role}
                                onChange={handleChange}
                            />
                            <FormControlLabel control={<Checkbox
                                value={userState.emailVerified}
                            />} label="Email Verified" />
                            
                            <Box className="italic">
                                <span>Member since: {joinedString}</span>
                            </Box>
                            <Button variant="contained" color="primary" type="submit" sx={{ mt: 2 }}>
                                Save
                            </Button>
                        </form>
                    </Paper>
                </Grid>
            </Grid>
        </Box>
    );
};

export default MyProfile;