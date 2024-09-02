import React from "react";
import { AppBar, Toolbar, Typography, Button, Box } from "@mui/material";
import HomeIcon from "@mui/icons-material/Home";
import Link from "next/link";

const Header = () => {
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
          <Link href="/login" passHref>
            <Button sx={{ color: "#F5F4FF", fontFamily: "Pretendard" }}>
              로그인
            </Button>
          </Link>
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
