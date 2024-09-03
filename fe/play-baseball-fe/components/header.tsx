import React, { useState, useEffect } from "react";
import { useRouter } from "next/router";
import { AppBar, Toolbar, Typography, Button, Box } from "@mui/material";
import HomeIcon from "@mui/icons-material/Home";
import Link from "next/link";
import axios from "axios";
import { MEMBER_LOGOUT, SERVER_URL } from "@/constants/endpoints";

const Header = () => {
  const [loggedIn, setLoggedIn] = useState<boolean>(false);
  const router = useRouter();
  useEffect(() => {
    const token =
      typeof window !== "undefined"
        ? localStorage.getItem("Authorization")
        : null;
    if (token) {
      setLoggedIn(true);
    }
  }, []);

  // 로그아웃 처리
  const handleLogout = () => {
    const token =
      typeof window !== "undefined"
        ? localStorage.getItem("Authorization")
        : null;
    axios
      .post(
        MEMBER_LOGOUT,
        {},
        {
          headers: {
            Authorization: token,
            "Content-Type": "application/json",
          },
        }
      )
      .then((response) => {
        if (response.status === 200) {
          localStorage.removeItem("Authorization");
          setLoggedIn(false);
          window.location.href = SERVER_URL;
        } else {
          throw new Error("통신 오류가 발생했습니다: logout");
        }
      })
      .catch((error) => {
        router.push({
          pathname: "/result",
          query: {
            isSuccess: "false",
            message: error,
            buttonText: "메인 페이지로 돌아가기",
            buttonAction: "/",
          },
        });
      });
  };

  return (
    <AppBar
      position="static"
      sx={{ bgcolor: "#766CED", height: "clamp(80px, 8vh, 130px)" }}
    >
      <Toolbar sx={{ minHeight: "unset", height: "100%" }}>
        <Typography
          variant="h6"
          component="div"
          sx={{ flexGrow: 1, fontFamily: "Pretendard", color: "#F5F4FF" }}
        >
          <Link href="/" passHref>
            <HomeIcon sx={{ color: "#F5F4FF" }} />
          </Link>
        </Typography>
        <Box sx={{ display: "flex", gap: 2 }}>
          {loggedIn ? (
            <Button
              onClick={handleLogout}
              sx={{ color: "#F5F4FF", fontFamily: "Pretendard" }}
            >
              로그아웃
            </Button>
          ) : (
            <Link href="/login" passHref>
              <Button sx={{ color: "#F5F4FF", fontFamily: "Pretendard" }}>
                로그인
              </Button>
            </Link>
          )}
          <Link href="/exchange/write" passHref>
            <Button sx={{ color: "#F5F4FF", fontFamily: "Pretendard" }}>
              판매하기
            </Button>
          </Link>
          <Link href="/chat" passHref>
            <Button sx={{ color: "#F5F4FF", fontFamily: "Pretendard" }}>
              채팅하기
            </Button>
          </Link>
          <Link href="/profile" passHref>
            <Button sx={{ color: "#F5F4FF", fontFamily: "Pretendard" }}>
              마이페이지
            </Button>
          </Link>
        </Box>
      </Toolbar>
    </AppBar>
  );
};

export default Header;
