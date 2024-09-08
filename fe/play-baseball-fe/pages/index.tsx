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
import SearchBar from "../components/SearchBar";
import axios from "axios";
import { EXCHANGE } from "@/constants/endpoints";
import debounce from "lodash/debounce";

interface Item {
  id: number;
  title: string;
  price: number;
  imageUrl: string;
}

const MainPage: React.FC = () => {
  const [items, setItems] = useState<Item[]>([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  const handleSearch = (searchTerm: string) => {
    console.log("Searching for:", searchTerm);
  };

  // 데이터 불러오기
  const fetchItems = async (pageNumber: number) => {
    setLoading(true);
    try {
      const response = await axios.get(`${EXCHANGE}?page=${pageNumber}`);
      const newItems = response.data.data.content.map((item: any) => ({
        id: item.id,
        title: item.title,
        price: item.price,
        imageUrl: item.images[0]?.url,
      }));
      setItems((prevItems) => [...prevItems, ...newItems]);
      setHasMore(!response.data.data.last);
    } catch (error) {
      console.error("Error fetching items:", error);
    }
    setLoading(false);
  };

  // 페이지 처음 로드 시 첫 데이터 요청
  useEffect(() => {
    fetchItems(page);
  }, [page]);

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
    <Container maxWidth="lg" sx={{ py: 3 }}>
      <div className="width-[70%]">
        <SearchBar onSearch={handleSearch} />
      </div>

      <Box
        sx={{
          width: "100%",
          height: "250px",
          backgroundColor: "#32CD32",
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          mb: 5,
        }}
      >
        <Typography variant="h4" sx={{ color: "#fff", fontWeight: "bold" }}>
          2024년 첫 안전결제라면 네이버페이 최대 15,000원 혜택
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {items.map((item) => (
          <Grid item xs={12} sm={6} md={4} lg={3} key={item.id}>
            <Link href={`/exchange/${item.id}`} passHref>
              <Card
                sx={{
                  height: "100%",
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
                  image={item.imageUrl}
                  alt={item.title}
                />
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    {item.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {item.price.toLocaleString()} 원
                  </Typography>
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
  );
};

export default MainPage;
