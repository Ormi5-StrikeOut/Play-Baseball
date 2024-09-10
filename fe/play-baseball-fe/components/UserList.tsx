import React from 'react';
import { List, ListItem, ListItemAvatar, ListItemText, Avatar, Paper, Box } from '@mui/material';
import UserItem from './UserItem';
import { User } from '@/constants/types';

const users: User[] = [
    {
        id: 1,
        nickname: 'John Doe',
        email: '',
        role: '',
        createdAt: '',
        updatedAt: '',
        lastLoginDate: '',
        deletedAt: '',
        emailVerified: false
    },
    {
        id: 2,
        nickname: 'Jane Smith',
        email: '',
        role: '',
        createdAt: '',
        updatedAt: '',
        lastLoginDate: '',
        deletedAt: '',
        emailVerified: false
    },
    {
        id: 3,
        nickname: 'Alice Johnson',
        email: '',
        role: '',
        createdAt: '',
        updatedAt: '',
        lastLoginDate: '',
        deletedAt: '',
        emailVerified: false
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