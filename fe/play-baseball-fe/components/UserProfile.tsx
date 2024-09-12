import React, { useState } from 'react';
import { Grid, Paper, Typography, Avatar, Box } from '@mui/material';
import { User } from '@/constants/types'

const initialUser: User = {
    id: -1,
    email: "user@example.com",
    nickname: "UserNickname",
    role: "UserRole",
    createdAt: "",
    updatedAt: "",
    lastLoginDate: "",
    deletedAt: "",
    emailVerified: false,
    phoneNumber: '',
    gender: ''
};

interface UserProfileProps {
    user: User;
}

const UserProfile: React.FC<UserProfileProps> = ({ user }) => {
    return (
        <Box sx={{ flexGrow: 1, padding: 3 }}>
            <Grid container spacing={3} justifyContent="center">
                <Grid item xs={12} md={4}>
                    <Paper elevation={3} sx={{ padding: 2 }}>
                        <Box sx={{ textAlign: 'center' }}>
                            <Avatar
                                src={"/assets/profile_placeholder.jpg"}
                                alt={`${user.nickname}'s avatar`}
                                sx={{ width: 100, height: 100, margin: '0 auto' }}
                            />
                        </Box>
                    </Paper>
                </Grid>
                <Grid item xs={12} md={8}>
                    <Paper elevation={3} sx={{ padding: 2 }}>
                        <Typography variant="h6" gutterBottom>
                            My Profile
                        </Typography>
                        <Typography variant="body1" gutterBottom>
                            <strong>Email:</strong> {user.email}
                        </Typography>
                        <Typography variant="body1" gutterBottom>
                            <strong>Nickname:</strong> {user.nickname}
                        </Typography>
                        <Typography variant="body1" gutterBottom>
                            <strong>Role:</strong> {user.role}
                        </Typography>
                        <Typography variant="body1" gutterBottom>
                            <strong>Email Verified:</strong> {user.emailVerified ? "Yes" : "No"}
                        </Typography>
                        <Typography variant="body2" className="italic">
                            Member since: {user.createdAt.substring(0, 10)}
                        </Typography>
                    </Paper>
                </Grid>
            </Grid>
        </Box>
    );
};

export default UserProfile;