import React from 'react';
import { ListItem, ListItemAvatar, ListItemText, Avatar } from '@mui/material';
import { User } from '@/constants/types';

interface UserListItemProps {
    user: User;
}

const UserListItem: React.FC<UserListItemProps> = ({ user }) => {
    return (
        <ListItem alignItems="flex-start">
            <ListItemAvatar>
                <Avatar src={"@/assets/profile_placeholder.jpg"} alt={`${user.nickname}'s avatar`} />
            </ListItemAvatar>
            <ListItemText
                primary={user.nickname}
                secondary={
                    <>
                        Email: {user.email}
                        <br />
                        Last login: {user.lastLoginDate}
                    </>
                }
            />
        </ListItem>
    );
};

export default UserListItem;