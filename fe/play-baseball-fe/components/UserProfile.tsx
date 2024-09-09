import React, { useState } from 'react';
import{ Grid, Paper, Typography, Avatar, Box, TextField, Button } from '@mui/material';

const initialUser = {
    name: 'John Doe',
    age: 30,
    bio: 'Full Stack Developer with a passion for building scalable web applications and working with the latest technologies.',
    avatarUrl: 'https://via.placeholder.com/150',
    email: 'john.doe@example.com',
    location: 'San Francisco, CA',
    interests: ['Coding', 'Hiking', 'Photography'],
};

const UserProfile: React.FC = () => {
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
                                src={user.avatarUrl}
                                alt={`${user.name}'s avatar`}
                                sx={{ width: 100, height: 100, margin: '0 auto' }}
                            />
                        </Box>
                    </Paper>
                </Grid>
                <Grid item xs={12} md={8}>
                    <Paper elevation={3} sx={{ padding: 2 }}>
                        <form onSubmit={handleSubmit}>
                            <Typography variant="h6" gutterBottom>
                                Edit Profile
                            </Typography>
                            <TextField
                                fullWidth
                                margin="normal"
                                label="Name"
                                name="name"
                                value={user.name}
                                onChange={handleChange}
                            />
                            <TextField
                                fullWidth
                                margin="normal"
                                label="Age"
                                name="age"
                                type="number"
                                value={user.age}
                                onChange={handleChange}
                            />
                            <TextField
                                fullWidth
                                margin="normal"
                                label="Bio"
                                name="bio"
                                multiline
                                rows={4}
                                value={user.bio}
                                onChange={handleChange}
                            />
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
                                label="Location"
                                name="location"
                                value={user.location}
                                onChange={handleChange}
                            />
                            <TextField
                                fullWidth
                                margin="normal"
                                label="Interests"
                                name="interests"
                                value={user.interests}
                                onChange={handleChange}
                            />
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

export default UserProfile;