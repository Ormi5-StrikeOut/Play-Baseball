import React, { useState, useEffect } from "react";
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
    MEMBER_VERIFY_RESEND,
} from "@/constants/endpoints";
import axios from "axios";

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
    const [resendStatus, setResendStatus] = useState<string>("");
    const [isResending, setIsResending] = useState<boolean>(false);
    const router = useRouter();

    useEffect(() => {
        const fetchUserData = async () => {
            try {
                const res = await api.get(MEMBER_MY);
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
                    emailVerified: userData.emailVerified === 1,
                    phoneNumber: userData.phoneNumber,
                    gender: userData.gender,
                };

                setUser(user);
                console.log("User email: ", user.email);
                console.log("User loaded! " + JSON.stringify(res.data));
                console.log("User is " + userData.nickname);
            } catch (err) {
                if (err instanceof Error) {
                    console.error("Error fetching user data: " + err.message);
                    setUser(initialUser);
                }
            }
        };

        fetchUserData();
    }, []);

    const handleSubmit = async (user: User) => {
        try {
            await api.put(MEMBER_MODIFY, {
                nickname: user.nickname,
                phoneNumber: user.phoneNumber,
                gender: user.gender,
                name: user.nickname,
            });

            window.location.href = `${SERVER_URL}/result?isSuccess=true&message=정보수정이 완료되었습니다.&buttonText=홈으로+돌아가기&buttonAction=/`;
        } catch (err) {
            if (err instanceof Error) {
                console.error("Error modifying user data: " + err.message);
            }
        }
    };

    const handleDeleteAccount = async (user: User) => {
        try {
            await api.put(MEMBER_RESIGN, {});
            localStorage.removeItem("Authorization");
            delete api.defaults.headers.common["Authorization"];
            window.location.href = `${SERVER_URL}/result?isSuccess=true&message=회원탈퇴가 완료되었습니다.&buttonText=홈으로+돌아가기&buttonAction=/`;
        } catch (err) {
            if (err instanceof Error) {
                console.error("Error performing account deletion: " + err.message);
            }
        }
    };

    const handleResendVerification = async () => {
        if (userState.emailVerified) {
            setResendStatus("이미 인증이 완료된 이메일입니다.");
            return;
        }

        setIsResending(true);
        try {
            console.log("Sending verification email to:", userState.email);
            const response = await api.post(`${MEMBER_VERIFY_RESEND}?email=${encodeURIComponent(userState.email)}`);
            console.log("Server response:", response.data);
            setResendStatus("인증 이메일이 재발송되었습니다.");
        } catch (error) {
            console.error("Error resending verification email:", error);
            if (axios.isAxiosError(error) && error.response) {
                console.error("Server error response:", error.response.data);
                if (error.response.status === 400 && error.response.data.message === "이미 인증된 이메일입니다.") {
                    setResendStatus("이미 인증이 완료된 이메일입니다.");
                    setUser(prevUser => ({ ...prevUser, emailVerified: true }));
                } else {
                    setResendStatus("이메일 재발송에 실패했습니다. 나중에 다시 시도해주세요.");
                }
            } else {
                setResendStatus("이메일 재발송에 실패했습니다. 나중에 다시 시도해주세요.");
            }
        } finally {
            setIsResending(false);
        }
    };

    return (
        <Wrapper>
            <MyProfile
                user={userState}
                setUser={setUser}
                onSubmit={handleSubmit}
                onDeleteAccount={handleDeleteAccount}
                onResendVerification={handleResendVerification}
                resendStatus={resendStatus}
                isResending={isResending}
            />
        </Wrapper>
    );
};

export default My;