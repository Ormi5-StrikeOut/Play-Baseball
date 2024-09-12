import React, { useState, useEffect, useRef } from "react";
import { useRouter } from "next/router";
import MyProfile from "@/components/MyProfile";
import Wrapper from "@/components/Wrapper";
import api from "@/constants/axios";
import { User } from "@/constants/types";
import {
  MEMBER_MODIFY,
  MEMBER_MY,
  MEMBER_RESIGN,
  SERVER_URL,
} from "@/constants/endpoints";

const My: React.FC = () => {
  const initialUser: User = {
    id: -1,
    email: "",
    nickname: "",
    role: "",
    createdAt: "",
    updatedAt: "",
    lastLoginDate: "",
    deletedAt: "",
    emailVerified: false,
    phoneNumber: "",
    gender: "",
  };

  const [userState, setUser] = useState<User>(initialUser);
  const router = useRouter();

  useEffect(() => {
    try {
      api.get(MEMBER_MY).then((res) => {
        const userData = res.data.data;

        const user: User = {
          id: userData.id,
          email: userData.email,
          nickname: userData.nickname,
          role: userData.role,
          createdAt: userData.createdAt,
          updatedAt: userData.updatedAt,
          lastLoginDate: userData.lastLoginDate,
          deletedAt: userData.deletedAt,
          // createdAt: new Date(userData.createdAt),
          // updatedAt: new Date(userData.updatedAt),
          // lastLoginDate: new Date(userData.lastLoginDate),
          // deletedAt: new Date(userData.deletedAt),
          emailVerified: userData.emailVerified,
          phoneNumber: userData.phoneNumber,
          gender: userData.gender,
        };

        setUser(user);
        console.log("User loaded! " + res.data);
        console.log("User is " + userState.nickname);
      });
    } catch (err) {
      if (err instanceof Error) {
        console.error("Error fetching user data: " + err.message);
        setUser(initialUser);
      }
    }
  }, []);

  function handleSubmit(user: User) {
    try {
      api
        .put(MEMBER_MODIFY, {
          // id: user.id,
          // email: user.email,
          nickname: user.nickname,
          phoneNumber: user.phoneNumber,
          gender: user.gender,
          name: user.nickname,
          // role: user.role,
          // createdAt: user.createdAt,
          // updatedAt: user.updatedAt,
          // lastLoginDate: user.lastLoginDate,
          // deletedAt: user.deletedAt,
          // createdAt: new Date(userData.createdAt),
          // updatedAt: new Date(userData.updatedAt),
          // lastLoginDate: new Date(userData.lastLoginDate),
          // deletedAt: new Date(userData.deletedAt),
          // emailVerified: user.emailVerified,
        })
        .then(() => {
          window.location.href = `${SERVER_URL}/result?isSuccess=true&message=정보수정이 완료되었습니다.&buttonText=홈으로+돌아가기&buttonAction=/`;
        });
    } catch (err) {
      if (err instanceof Error) {
        console.error("Error modifying user data: " + err.message);
      }
    }
  }

  function handleDeleteAccount(user: User) {
    try {
      api.put(MEMBER_RESIGN, {}).then(() => {
        localStorage.removeItem("Authorization");
        delete api.defaults.headers.common["Authorization"];
        window.location.href = `${SERVER_URL}/result?isSuccess=true&message=회원탈퇴가 완료되었습니다.&buttonText=홈으로+돌아가기&buttonAction=/`;
      });
    } catch (err) {
      if (err instanceof Error) {
        console.error("Error performing account deletion: " + err.message);
      }
    }
  }

  return (
    <Wrapper>
      <MyProfile
        user={userState}
        setUser={setUser}
        onSubmit={handleSubmit}
        onDeleteAccount={handleDeleteAccount}
      />
    </Wrapper>
  );
};

export default My;
