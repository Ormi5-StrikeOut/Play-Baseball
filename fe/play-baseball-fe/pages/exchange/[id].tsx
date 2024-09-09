import React, { useState, useEffect } from "react";
import {
  Container,
  Box,
  Typography,
  Divider,
  Button,
  IconButton,
  Grid,
  Fade,
  Rating,
  Paper,
  Modal,
} from "@mui/material";
import axios from "axios";
import { ArrowBack, ArrowForward } from "@mui/icons-material";
import Image from "next/image";
import Wrapper from "../../components/Wrapper";
import { useRouter } from "next/router";
import { EXCHANGE } from "@/constants/endpoints";

// Define types for the ExchangeDetailResponseDto and Image objects
interface Image {
  url: string;
  title: string;
}

interface RecentExchange {
  title: string;
  price: number;
  url: string;
  imageUrl: string;
  updatedAt: string;
}

interface ExchangeDetailResponseDto {
  title: string;
  price: number;
  regularPrice: number;
  content: string;
  viewCount: number;
  status: "SALE" | "COMPLETE";
  updatedAt: string;
  images: Image[];
  writer: string;
  recentExchangesByMember: RecentExchange[];
  isWriter: boolean;
}

const ItemDetail: React.FC = () => {
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [hover, setHover] = useState<boolean>(false);
  const [openModal, setOpenModal] = useState<boolean>(false);
  const [exchangeData, setExchangeData] =
    useState<ExchangeDetailResponseDto | null>(null); // State with specific type
  const router = useRouter();
  const { id } = router.query;
  const token =
    typeof window !== "undefined"
      ? localStorage.getItem("Authorization")
      : null;

  useEffect(() => {
    const fetchExchangeData = async () => {
      if (!id) return;
      try {
        const response = await axios.get<ExchangeDetailResponseDto>(
          `${EXCHANGE}/${id}`,
          {
            headers: {
              Authorization: token,
            },
            withCredentials: true,
          }
        );
        setExchangeData(response.data);
      } catch (error) {
        router.push({
          pathname: "/result",
          query: {
            isSuccess: "false",
            message: `데이터를 가져오는 중 오류가 발생했습니다: ${
              (error as Error).message
            }`,
            buttonText: "메인으로 돌아가기",
            buttonAction: `/`,
          },
        });
      }
    };

    fetchExchangeData();
  }, [id, router]);

  const handlePrev = () => {
    setCurrentIndex(
      (prevIndex) =>
        (prevIndex - 1 + (exchangeData?.images.length || 1)) %
        (exchangeData?.images.length || 1)
    );
  };

  const handleNext = () => {
    setCurrentIndex(
      (prevIndex) => (prevIndex + 1) % (exchangeData?.images.length || 1)
    );
  };

  const handleMouseEnter = () => {
    setHover(true);
  };

  const handleMouseLeave = () => {
    setHover(false);
  };

  const handleDelete = async () => {
    const token =
      typeof window !== "undefined"
        ? localStorage.getItem("Authorization")
        : null;

    try {
      await axios.delete(`${EXCHANGE}/${id}`, {
        headers: {
          Authorization: token,
        },
        withCredentials: true,
      });
      router.push({
        pathname: "/result",
        query: {
          isSuccess: "true",
          message: `글이 정상적으로 삭제되었습니다. ${exchangeData?.title}`,
          buttonText: "메인으로 돌아가기",
          buttonAction: `/`,
        },
      });
    } catch (error) {
      router.push({
        pathname: "/result",
        query: {
          isSuccess: "false",
          message: `통신 오류가 발생했습니다: ${(error as Error).message}`,
          buttonText: "작성한 글로 돌아가기",
          buttonAction: `/exchange/${id}`,
        },
      });
    }
  };

  if (!exchangeData) {
    return (
      <Wrapper>
        <Container maxWidth="lg" style={{ marginTop: "20px" }}>
          <Typography variant="h6">Loading...</Typography>
        </Container>
      </Wrapper>
    );
  }

  return (
    <Wrapper>
      <Container maxWidth="lg" style={{ marginTop: "20px" }}>
        <Grid container spacing={2}>
          {/* Gallery */}
          <Grid item xs={12} md={6}>
            <Box
              position="relative"
              display="flex"
              flexDirection="column"
              alignItems="center"
              onMouseEnter={handleMouseEnter}
              onMouseLeave={handleMouseLeave}
              sx={{
                width: "100%",
                maxWidth: "100%",
              }}
            >
              <Box
                display="flex"
                alignItems="center"
                justifyContent="center"
                position="relative"
                width="100%"
              >
                <Fade in={hover}>
                  <IconButton
                    onClick={handlePrev}
                    aria-label="previous image"
                    sx={{
                      position: "absolute",
                      left: "10px",
                      zIndex: 1,
                      backgroundColor: "rgba(255, 255, 255, 0.7)",
                    }}
                  >
                    <ArrowBack />
                  </IconButton>
                </Fade>
                <Image
                  src={exchangeData.images[currentIndex].url}
                  alt={exchangeData.images[currentIndex].title}
                  layout="responsive"
                  width={700}
                  height={400}
                  objectFit="cover"
                />
                <Fade in={hover}>
                  <IconButton
                    onClick={handleNext}
                    aria-label="next image"
                    sx={{
                      position: "absolute",
                      right: "10px",
                      zIndex: 1,
                      backgroundColor: "rgba(255, 255, 255, 0.7)",
                    }}
                  >
                    <ArrowForward />
                  </IconButton>
                </Fade>
              </Box>

              {/* Indicators */}
              <Box display="flex" justifyContent="center" mt={1}>
                {exchangeData.images.map((_, index) => (
                  <Box
                    key={index}
                    onClick={() => setCurrentIndex(index)}
                    sx={{
                      width: "10px",
                      height: "10px",
                      margin: "0 5px",
                      borderRadius: "50%",
                      backgroundColor:
                        currentIndex === index ? "black" : "gray",
                      cursor: "pointer",
                    }}
                  />
                ))}
              </Box>
            </Box>
          </Grid>

          {/* Product Info */}
          <Grid item xs={12} md={6}>
            <Paper elevation={3} sx={{ padding: "20px" }}>
              <Typography variant="h5">{exchangeData.title}</Typography>
              <Typography
                variant="h5"
                color="primary"
                sx={{ marginTop: "10px" }}
              >
                {exchangeData.price.toLocaleString()}원
              </Typography>
              <Divider sx={{ margin: "20px 0" }} />
              <Typography variant="body1">
                상태: {exchangeData.status === "SALE" ? "판매중" : "판매완료"}
              </Typography>
              <Divider sx={{ margin: "20px 0" }} />
              <Typography variant="body1">
                이 상품의 정가는 {exchangeData.regularPrice.toLocaleString()}원
                입니다.
              </Typography>
              <Divider sx={{ margin: "20px 0" }} />
              <Button variant="contained" fullWidth>
                채팅하기
              </Button>
              <Button variant="contained" fullWidth sx={{ mt: 2 }}>
                결제하기
              </Button>
              {exchangeData.isWriter && (
                <>
                  <Button
                    variant="outlined"
                    fullWidth
                    sx={{ mt: 2 }}
                    onClick={() => router.push(`/exchange/write/${id}`)}
                  >
                    수정하기
                  </Button>
                  <Button
                    variant="outlined"
                    fullWidth
                    sx={{ mt: 2 }}
                    color="error"
                    onClick={() => setOpenModal(true)}
                  >
                    삭제하기
                  </Button>
                </>
              )}
            </Paper>
          </Grid>

          {/* Details */}
          <Grid item xs={12} md={6}>
            <Paper elevation={3} sx={{ padding: "20px" }}>
              <Typography variant="h4">상품 정보</Typography>
              <Typography color="textSecondary" sx={{ marginTop: "10px" }}>
                작성일: {new Date(exchangeData.updatedAt).toLocaleDateString()}
              </Typography>
              <Typography color="textSecondary">
                조회: {exchangeData.viewCount}
              </Typography>
              <Divider sx={{ margin: "20px 0" }} />
              <Typography variant="body1">{exchangeData.content}</Typography>
            </Paper>
          </Grid>

          {/* Seller Info */}
          <Grid item xs={12} md={6}>
            <Paper elevation={3} sx={{ padding: "20px" }}>
              <Typography variant="h6">{exchangeData.writer}</Typography>
              <Box display="flex" alignItems="center">
                <Box display="flex" alignItems="center">
                  <Rating value={4.6} precision={0.1} readOnly />
                  <Typography
                    variant="body2"
                    color="textSecondary"
                    sx={{ marginLeft: "5px" }}
                  >
                    (123)
                  </Typography>
                </Box>
              </Box>
              <Divider sx={{ margin: "20px 0" }} />
              <Grid container spacing={1} mt={2}>
                {exchangeData.recentExchangesByMember.length > 0 ? (
                  exchangeData.recentExchangesByMember.map((item, index) => (
                    <Grid
                      item
                      xs={4}
                      key={index}
                      onClick={() => router.push(item.url)} // 클릭 시 해당 URL로 이동
                      sx={{
                        cursor: "pointer",
                        "&:hover": {
                          boxShadow: 2, // 마우스 오버 시 박스 그림자 효과
                        },
                      }}
                    >
                      <Image
                        src={item.imageUrl}
                        alt={item.title}
                        layout="responsive"
                        width={100}
                        height={100}
                        objectFit="cover"
                        style={{ borderRadius: "4px" }}
                      />
                      <Typography
                        variant="caption"
                        display="block"
                        align="center"
                      >
                        {item.title}
                      </Typography>
                      <Typography
                        variant="caption"
                        display="block"
                        align="center"
                      >
                        {item.price.toLocaleString()}원
                      </Typography>
                    </Grid>
                  ))
                ) : (
                  <Typography variant="body2" color="textSecondary">
                    판매중인 다른 게시물이 없습니다.
                  </Typography>
                )}
              </Grid>
            </Paper>
          </Grid>

          {/* Delete Confirmation Modal */}
          <Modal open={openModal} onClose={() => setOpenModal(false)}>
            <Box
              sx={{
                position: "absolute",
                top: "50%",
                left: "50%",
                transform: "translate(-50%, -50%)",
                width: 400,
                bgcolor: "background.paper",
                boxShadow: 24,
                p: 4,
                textAlign: "center",
              }}
            >
              <Typography variant="h6" gutterBottom>
                정말 삭제하시겠습니까?
              </Typography>
              <Button variant="contained" color="error" onClick={handleDelete}>
                확인
              </Button>
              <Button
                variant="outlined"
                sx={{ ml: 2 }}
                onClick={() => setOpenModal(false)}
              >
                취소
              </Button>
            </Box>
          </Modal>
        </Grid>
      </Container>
    </Wrapper>
  );
};

export default ItemDetail;
