import React from 'react';
import UserList from '../../components/UserList';
import Wrapper from '../../components/Wrapper'

const Admin: React.FC = () => {
    return (
        <Wrapper>
            <UserList />
        </Wrapper>
    );
};

export default Admin;