import React, { useState, useEffect, useCallback } from "react";
import {
  Box,
  Grid,
  Typography,
  Card,
  CardMedia,
  CardContent,
  Container,
} from "@mui/material";
import Link from "next/link";
import axios from "axios";
import { DEFAULT_BANNER, DEFAULT_IMAGE, EXCHANGE } from "@/constants/endpoints";
import debounce from "lodash/debounce";
import AccessTimeIcon from "@mui/icons-material/AccessTime";
import VisibilityIcon from "@mui/icons-material/Visibility";
import SaleStatusNavigation from "../components/SaleStatusNavigation"; // 네비게이션 컴포넌트 임포트
import RecentExchangesNavigation from "@/components/RecentExchangesNavigation";

interface Item {
  title: string;
  price: number;
  url: string;
  imageUrl: string;
  updatedAt: string;
  viewCount: number;
  status: "SALE" | "COMPLETE";
}

const formatTimeAgo = (dateString: string) => {
  const date = new Date(dateString);
  const now = new Date();
  const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

  if (diffInSeconds < 60) return "방금 전";
  const diffInMinutes = Math.floor(diffInSeconds / 60);
  if (diffInMinutes < 60) return `${diffInMinutes}분 전`;
  const diffInHours = Math.floor(diffInMinutes / 60);
  if (diffInHours < 24) return `${diffInHours}시간 전`;
  const diffInDays = Math.floor(diffInHours / 24);
  if (diffInDays < 30) return `${diffInDays}일 전`;
  const diffInMonths = Math.floor(diffInDays / 30);
  if (diffInMonths < 12) return `${diffInMonths}개월 전`;
  const diffInYears = Math.floor(diffInMonths / 12);
  return `${diffInYears}년 전`;
};

const MainPage: React.FC = () => {
  const [items, setItems] = useState<Item[]>([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [status, setStatus] = useState<string | null>(null); // 초기에는 null로 설정

  // 클라이언트에서만 쿼리 파라미터 처리
  useEffect(() => {
    if (typeof window !== "undefined") {
      const query = new URLSearchParams(window.location.search);
      const statusFromQuery = query.get("status") || "NONE"; // 기본값 설정
      setStatus(statusFromQuery); // 쿼리에서 status 값을 설정
    }
  }, []);

  // 데이터 불러오기
  const fetchItems = async (pageNumber: number) => {
    setLoading(true);
    try {
      const response = await axios.get(
        `${EXCHANGE}?page=${pageNumber}&status=${status}`
      );
      const newItems = response.data.data.content.map((item: any) => ({
        title: item.title,
        price: item.price,
        url: item.url,
        imageUrl: item.imageUrl,
        updatedAt: item.updatedAt,
        viewCount: item.viewCount,
        status: item.status,
      }));
      setItems((prevItems) => [...prevItems, ...newItems]);
      setHasMore(!response.data.data.last);
    } catch (error) {
      console.error("Error fetching items:", error);
    }
    setLoading(false);
  };

  // 쿼리에서 받은 status 값이 설정된 후에만 fetchItems 실행
  useEffect(() => {
    if (status !== null) {
      // status가 null이 아닐 때만 실행
      fetchItems(page);
    }
  }, [status, page]);

  // 스크롤 이벤트 핸들러
  const handleScroll = useCallback(
    debounce(() => {
      if (
        window.innerHeight + document.documentElement.scrollTop >=
        document.documentElement.offsetHeight - 50
      ) {
        if (!loading && hasMore) {
          setPage((prevPage) => prevPage + 1); // 다음 페이지 요청
        }
      }
    }, 300), // 300ms 동안 이벤트가 연속적으로 발생하지 않도록 제한.
    [loading, hasMore]
  );

  // 스크롤 이벤트 감지
  useEffect(() => {
    window.addEventListener("scroll", handleScroll);
    return () => {
      window.removeEventListener("scroll", handleScroll);
    };
  }, [handleScroll]);

  return (
    <Box
      sx={{
        width: "100%",
        display: "flex",
        justifyContent: "center",
        paddingTop: 2,
      }}
    >
      {/* 전체 레이아웃: 20% - 60% - 20% */}
      {/* 왼쪽 네비게이션 영역 */}
      {/* <Box
        sx={{
          width: "20%",
          position: "fixed", // 화면에 고정
          left: 0, // 왼쪽에 고정
          top: "50%", // 세로 기준 가운데
          transform: "translateY(-50%)", // 세로 가운데 정렬
          display: "flex",
          justifyContent: "center",
          alignItems: "center", // 부모 기준 가로 및 세로 중앙 정렬
        }}
      >
        <Box>
          <SaleStatusNavigation />
        </Box>
      </Box> */}

      {/* 가운데 컨텐츠 영역 */}
      <Box sx={{ width: "100%" }}>
        <Container maxWidth="lg" sx={{ py: 3 }}>
          {/* 배너 이미지 */}
          <Box
            sx={{
              width: "100%",
              height: "250px", // 원하는 배너 높이
              mb: 5,
            }}
          >
            <CardMedia
              component="img"
              image={DEFAULT_BANNER} // 배너 이미지 경로
              alt="배너 이미지"
              sx={{
                width: "100%",
                height: "100%",
                objectFit: "cover", // 이미지가 컨테이너를 꽉 채우도록 설정
              }}
            />
          </Box>

          <Grid container spacing={3}>
            {items.map((item) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={item.url}>
                <Link href={`${item.url}`} passHref>
                  <Card
                    sx={{
                      height: "100%",
                      position: "relative", // 상태 표시를 위한 상대 위치 지정
                      "&:hover": {
                        boxShadow: 6,
                        transform: "translateY(-5px)",
                        transition: "transform 0.3s ease-in-out",
                      },
                    }}
                  >
                    <CardMedia
                      component="img"
                      height="200"
                      image={item.imageUrl || DEFAULT_IMAGE}
                      alt={item.title}
                    />

                    {/* 상태 아이콘 */}
                    <Box
                      sx={{
                        position: "absolute",
                        top: 8,
                        left: 8,
                        display: "flex",
                        alignItems: "center",
                        backgroundColor: "rgba(0, 0, 0, 0.5)",
                        borderRadius: "50px",
                        padding: "4px 8px",
                      }}
                    >
                      <Box
                        sx={{
                          width: 10,
                          height: 10,
                          borderRadius: "50%",
                          backgroundColor:
                            item.status === "SALE" ? "#32CD32" : "#FF0000",
                          marginRight: "8px",
                        }}
                      />
                      <Typography
                        variant="body2"
                        sx={{ color: "#fff", fontWeight: "bold" }}
                      >
                        {item.status === "SALE" ? "판매중" : "판매완료"}
                      </Typography>
                    </Box>

                    <CardContent>
                      <Typography variant="h6" gutterBottom>
                        {/* 제목이 30글자 이상이면 ... 처리 */}
                        {item.title.length > 30
                          ? `${item.title.substring(0, 30)}...`
                          : item.title}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {item.price.toLocaleString()} 원
                      </Typography>
                      {/* 조회수 및 작성시간 추가 */}
                      <Box
                        sx={{
                          display: "flex",
                          justifyContent: "space-between",
                          alignItems: "center",
                          mt: 2,
                        }}
                      >
                        <Box sx={{ display: "flex", alignItems: "center" }}>
                          <VisibilityIcon sx={{ fontSize: 16, mr: 0.5 }} />
                          <Typography variant="body2">
                            {item.viewCount}
                          </Typography>
                        </Box>
                        <Box sx={{ display: "flex", alignItems: "center" }}>
                          <AccessTimeIcon sx={{ fontSize: 16, mr: 0.5 }} />
                          <Typography variant="body2">
                            {formatTimeAgo(item.updatedAt)}
                          </Typography>
                        </Box>
                      </Box>
                    </CardContent>
                  </Card>
                </Link>
              </Grid>
            ))}
          </Grid>

          {loading && (
            <Box sx={{ display: "flex", justifyContent: "center", mt: 3 }}>
              <Typography>Loading...</Typography>
            </Box>
          )}
        </Container>
      </Box>

      {/* 오른쪽 네비게이션 영역 */}
      {/* <Box
        sx={{
          width: "20%",
          position: "fixed", // 화면에 고정
          right: 0, // 오른쪽에 고정
          top: "50%", // 세로 기준 가운데
          transform: "translateY(-50%)", // 세로 가운데 정렬
          display: "flex",
          justifyContent: "center",
          alignItems: "center", // 부모 기준 가로 및 세로 중앙 정렬
        }}
      >
        <Box>
          <RecentExchangesNavigation />
        </Box>
      </Box> */}
    </Box>
  );
};

export default MainPage;
