import React, { useState, useEffect } from "react";
import { useRouter } from "next/router";
import {
  AppBar,
  Toolbar,
  Typography,
  Button,
  Box,
  Container,
  TextField,
  InputAdornment,
  IconButton,
} from "@mui/material";
import HomeIcon from "@mui/icons-material/Home";
import SearchIcon from "@mui/icons-material/Search";
import Link from "next/link";
import axios from "axios";
import { MEMBER_LOGOUT, SERVER_URL } from "@/constants/endpoints";
import SearchBar from "./SearchBar";
import Wrapper from "./Wrapper";

const Header: React.FC = () => {
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
          withCredentials: true,
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

  const handleSearch = (input: string) => {
      // 검색 API 호출 로직을 여기에 추가하세요.
  }

  return (
      <Wrapper>
        <Toolbar
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: 0, // 기본 패딩 제거
          }}
        >
          <Typography
            variant="h6"
            component="div"
            sx={{
              flexGrow: 1,
              fontFamily: "Pretendard",
              color: "#000",
            }}
          >
            <Link href="/" passHref>
              <HomeIcon sx={{ color: "#000" }} />
            </Link>
          </Typography>

          {/* 검색창 */}
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              flexGrow: 2, // 검색창이 더 넓게 차지하도록
              justifyContent: "center",
              mx: 2, // 간격 조정
            }}
          >
            <SearchBar onSearch={handleSearch}></SearchBar>
          </Box>

          <Box sx={{ display: "flex", gap: 2 }}>
            {loggedIn ? (
              <Button
                onClick={handleLogout}
                sx={{ color: "#000", fontFamily: "Pretendard" }}
              >
                로그아웃
              </Button>
            ) : (
              <Link href="/login" passHref>
                <Button sx={{ color: "#000", fontFamily: "Pretendard" }}>
                  로그인
                </Button>
              </Link>
            )}
            <Link href="/exchange/write" passHref>
              <Button sx={{ color: "#000", fontFamily: "Pretendard" }}>
                판매하기
              </Button>
            </Link>
            <Link href="/chat" passHref>
              <Button sx={{ color: "#000", fontFamily: "Pretendard" }}>
                채팅하기
              </Button>
            </Link>
            <Link href="/my" passHref>
              <Button sx={{ color: "#000", fontFamily: "Pretendard" }}>
                마이페이지
              </Button>
            </Link>
          </Box>
        </Toolbar>
      </Wrapper>
  );
};

export default Header;
