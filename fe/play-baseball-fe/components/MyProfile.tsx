import React, { useState } from 'react';
import{ Grid, Paper, Typography, Avatar, Box, TextField, Button, Checkbox, FormControlLabel } from '@mui/material';

const initialUser = {
    id: -1,
    email: "",
    nickname: "",
    role: "",
    createdAt: NaN,
    updatedAt: NaN,
    lastLoginDate: NaN,
    deletedAt: NaN,
    emailVerified: false
};

const MyProfile: React.FC = () => {
    const [user, setUser] = useState(initialUser);

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
        console.log('User Profile Updated:', user);
    };

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
                                label="Role"
                                name="role"
                                value={user.role}
                                onChange={handleChange}
                            />
                            <FormControlLabel control={<Checkbox
                                value={user.emailVerified}
                            />} label="Email Verified" />
                            
                            <Box className="italic">
                                Member since: {user.createdAt}
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