import React from 'react';
import { ListItem, ListItemAvatar, ListItemText, Avatar } from '@mui/material';
import { User } from '../constants/types';

interface UserListItemProps {
    user: User;
}

const UserListItem: React.FC<UserListItemProps> = ({ user }) => {
    return (
        <ListItem alignItems="flex-start">
            <ListItemAvatar>
                <Avatar src={user.avatarUrl} alt={`${user.name}'s avatar`} />
            </ListItemAvatar>
            <ListItemText
                primary={user.name}
                secondary={
                    <>
                        Age: {user.age}
                        <br />
                        {user.bio}
                    </>
                }
            />
        </ListItem>
    );
};

export default UserListItem;