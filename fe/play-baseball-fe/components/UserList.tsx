import React from 'react';
import { List, ListItem, ListItemAvatar, ListItemText, Avatar, Paper, Box } from '@mui/material';
import UserItem from './UserItem';
import { User } from '../constants/types';

const users: User[] = [
    {
        id: 1,
        name: 'John Doe',
        age: 30,
        bio: 'Full Stack Developer',
        avatarUrl: 'https://via.placeholder.com/150',
    },
    {
        id: 2,
        name: 'Jane Smith',
        age: 25,
        bio: 'UI/UX Designer',
        avatarUrl: 'https://via.placeholder.com/150',
    },
    {
        id: 3,
        name: 'Alice Johnson',
        age: 28,
        bio: 'Data Scientist',
        avatarUrl: 'https://via.placeholder.com/150',
    },
];

const UserList: React.FC = () => {
    return (
        <Box sx={{ flexGrow: 1, padding: 3, maxWidth: 1300, margin: '0 auto' }}>
            <Paper elevation={3}>
                <List>
                    {users.map((user) => (
                        <UserItem key={user.id} user={user} />
                    ))}
                </List>
            </Paper>
        </Box>
    );
};

export default UserList;