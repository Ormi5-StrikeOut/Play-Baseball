import React, { useState, useEffect} from 'react';
import axios from 'axios';
import MyProfile from '../../components/MyProfile';
import Wrapper from '../../components/Wrapper'
import { User } from '@/constants/types'

const My: React.FC = () => {
    const initialUser: User = {
        id: -1,
        email: "",
        nickname: "",
        role: "",
        createdAt: new Date(0),
        updatedAt: new Date(0),
        lastLoginDate: new Date(0),
        deletedAt: new Date(0),
        emailVerified: false
    };

    const [userState, setUser] = useState<User>(initialUser);

    useEffect(() => {
        try {
            axios.get<User>('https://api.ioshane.com/api/members/my').then(res => {
              setUser(res.data);
            })
          } catch (err) {
            if(err instanceof Error) {
              console.error("Error fetching user data: " + err.message);
              setUser(initialUser)
            }
          }
  
          console.log("User loaded! " + userState);
    }, []);

    return (
        <Wrapper>
            <MyProfile user={userState}/>
        </Wrapper>
    );
};

export default My;

function componentDidMount() {
    throw new Error('Function not implemented.');
}
  