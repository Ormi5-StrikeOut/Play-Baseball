import React, { useState, useEffect } from "react";
import { AppBar, Toolbar, Typography, Button, Box } from "@mui/material";
import HomeIcon from "@mui/icons-material/Home";
import Link from "next/link";
import { useRouter } from "next/router";
import { MEMBER_LOGOUT, SERVER_URL } from "@/constants/endpoints";

// 쿠키에서 특정 이름의 쿠키 값을 가져오는 함수
const getCookie = (name: string): string | null => {
  const value = `; ${document.cookie}`;
  const parts = value.split(`; ${name}=`);
  if (parts.length === 2) {
    return parts.pop()?.split(";").shift() ?? null;
  }
  return null;
};

const Header = () => {
  const [loggedIn, setLoggedIn] = useState<boolean>(false);
  const router = useRouter();

  // 로그인 상태 확인
  useEffect(() => {
    const token = getCookie("Authorization");
    if (token) {
      setLoggedIn(true);
    }
  }, []);

  // 로그아웃 처리
  const handleLogout = () => {
    const token = getCookie("Authorization");
    fetch(MEMBER_LOGOUT, {
      method: "GET",
      headers: {
        Authorization: `${token}`,
        "Content-Type": "application/json",
      },
    })
      .then((response) => {
        if (response.ok) {
          setLoggedIn(false);
          window.location.href = SERVER_URL;
        } else {
          router.push({
            pathname: "/result",
            query: {
              isSuccess: "false",
              message: "통신 오류가 발생했습니다.",
              buttonText: "메인 페이지로 돌아가기",
              buttonAction: "/",
            },
          });
        }
      })
      .catch((error) => {
        router.push({
          pathname: "/result",
          query: {
            isSuccess: "false",
            message: "통신 오류가 발생했습니다.",
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
