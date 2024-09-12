import React, { useEffect, useState } from "react";
import { useRouter } from "next/router";
import { Box, Button, Toolbar, Typography, Container } from "@mui/material";
import HomeIcon from "@mui/icons-material/Home";
import Link from "next/link";
import { MEMBER_LOGOUT, PAGE_SEARCH } from "@/constants/endpoints";
import SearchBar from "./SearchBar";
import Wrapper from "./Wrapper";
import axiosInstance, { handleApiError } from "./axiosInstance";
import qs from "qs";

const Header: React.FC = () => {
  const [loggedIn, setLoggedIn] = useState<boolean>(false);
  const router = useRouter();

  useEffect(() => {
    const checkLoginStatus = () => {
      const token = localStorage.getItem("Authorization");
      setLoggedIn(!!token);
    };

    checkLoginStatus();
    window.addEventListener("storage", checkLoginStatus);

    return () => {
      window.removeEventListener("storage", checkLoginStatus);
    };
  }, []);

  const handleLogout = async () => {
    try {
      const response = await axiosInstance.post(
        MEMBER_LOGOUT,
        {},
        { withCredentials: true }
      );
      if (response.status === 200) {
        localStorage.removeItem("Authorization");
        delete axiosInstance.defaults.headers.common["Authorization"];
        setLoggedIn(false);
        await router.push("/");
      } else {
        throw new Error("로그아웃 처리 중 오류가 발생했습니다.");
      }
    } catch (error) {
      handleApiError(error);
      await router.push({
        pathname: "/result",
        query: {
          isSuccess: "false",
          message:
            error instanceof Error
              ? error.message
              : "알 수 없는 오류가 발생했습니다.",
          buttonText: "메인 페이지로 돌아가기",
          buttonAction: "/",
        },
      });
    }
  };

  // handleSearch 함수에서 검색어를 keyword로 추가하여 페이지 이동
  const handleSearch = (input: string) => {
    if (input) {
      const updatedQuery = { ...router.query, keyword: input };
      const url = `${PAGE_SEARCH}?${qs.stringify(updatedQuery)}`;

      window.location.href = url;
    }
  };

  return (
    <Box sx={{ backgroundColor: "#f5f5f5", width: "100%", py: 2 }}>
      {" "}
      {/* 헤더 배경과 전체 width 적용 */}
      <Container maxWidth="lg">
        {" "}
        {/* 내부 콘텐츠를 컨테이너로 감싸 최대 너비 제한 */}
        <Toolbar
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            padding: 0,
          }}
        >
          {/* Home 아이콘 */}
          <Typography
            variant="h6"
            component="div"
            sx={{
              fontFamily: "Pretendard",
              color: "#000",
            }}
          >
            <Link href="/" passHref>
              <HomeIcon sx={{ color: "#000" }} />
            </Link>
          </Typography>

          {/* Search Bar */}
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              flexGrow: 2,
              justifyContent: "center",
              mx: 2,
              maxWidth: "60%", // 최대 width 제한
            }}
          >
            <SearchBar onSearch={handleSearch} />
          </Box>

          {/* Navigation Buttons */}
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
      </Container>
    </Box>
  );
};

export default Header;
