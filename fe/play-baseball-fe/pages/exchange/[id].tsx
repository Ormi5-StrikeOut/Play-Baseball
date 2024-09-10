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

interface Image {
  url: string;
  id: number;
}

interface RecentExchange {
  title: string;
  price: number;
  url: string;
  imageUrl: string;
  updatedAt: string;
}

interface ApiResponse<T> {
  message: string;
  data: T;
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
  isWriter: "TRUE" | "FALSE";
}

const ItemDetail: React.FC = () => {
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [hover, setHover] = useState<boolean>(false);
  const [openModal, setOpenModal] = useState<boolean>(false);
  const [exchangeData, setExchangeData] =
    useState<ExchangeDetailResponseDto | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const router = useRouter();
  const { id } = router.query;
  const token =
    typeof window !== "undefined"
      ? localStorage.getItem("Authorization")
      : null;

  useEffect(() => {
    const fetchExchangeData = async () => {
      if (!id) return; // id가 없는 경우 일찍 반환
      try {
        const response = await axios.get<
          ApiResponse<ExchangeDetailResponseDto>
        >(`${EXCHANGE}/${id}`, {
          headers: {
            Authorization: token,
          },
          withCredentials: true,
        });
        console.log(response);
        setExchangeData(response.data.data);
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

    if (id) fetchExchangeData(); // id가 존재할 때만 API 호출
  }, [id, router, token]);

  useEffect(() => {
    if (exchangeData) {
      setLoading(false);
    }
  }, [exchangeData]); // exchangeData가 변경될 때마다 실행

  const handlePrev = () => {
    if (exchangeData?.images?.length) {
      setCurrentIndex(
        (prevIndex) =>
          (prevIndex - 1 + exchangeData.images.length) %
          exchangeData.images.length
      );
    }
  };

  const handleNext = () => {
    if (exchangeData?.images?.length) {
      setCurrentIndex(
        (prevIndex) => (prevIndex + 1) % exchangeData.images.length
      );
    }
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

  if (loading || !exchangeData) {
    return (
      <Wrapper>
        <Container maxWidth="lg" style={{ marginTop: "20px" }}>
          <Typography variant="h6">Loading...</Typography>
        </Container>
      </Wrapper>
    );
  } else {
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
                  sx={{
                    width: "100%",
                    aspectRatio: "1 / 1", // 원하는 비율을 설정할 수 있음
                    overflow: "hidden", // 이미지가 잘릴 수 있음
                  }}
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

                  {exchangeData?.images?.length > 0 ? (
                    <Image
                      src={exchangeData.images[currentIndex]?.url}
                      alt={exchangeData.images[currentIndex]?.id.toString()}
                      layout="fill" // 컨테이너에 맞춰 이미지를 채움
                      objectFit="cover" // 이미지 비율 유지하며 컨테이너를 채움
                    />
                  ) : (
                    <Image
                      src={"/default-img.jpg"}
                      alt={"Default image"}
                      layout="fill" // 컨테이너에 맞춰 이미지를 채움
                      objectFit="cover" // 이미지 비율 유지하며 컨테이너를 채움
                    />
                  )}

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
                  {exchangeData?.images?.length > 0 ? (
                    exchangeData.images.map((_, index) => (
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
                    ))
                  ) : (
                    <Typography variant="body2" color="textSecondary">
                      이미지가 없습니다.
                    </Typography>
                  )}
                </Box>
              </Box>
            </Grid>

            {/* Product Info */}
            <Grid item xs={12} md={6}>
              <Paper elevation={3} sx={{ padding: "20px" }}>
                <Typography variant="h5">
                  {exchangeData?.title || "상품 정보"}
                </Typography>
                <Typography
                  variant="h5"
                  color="primary"
                  sx={{ marginTop: "10px" }}
                >
                  {exchangeData?.price?.toLocaleString() || "0"}원
                </Typography>
                <Divider sx={{ margin: "20px 0" }} />
                <Typography variant="body1">
                  상태:{" "}
                  {exchangeData?.status === "SALE" ? "판매중" : "판매완료"}
                </Typography>
                <Divider sx={{ margin: "20px 0" }} />
                <Typography variant="body1">
                  이 상품의 정가는{" "}
                  {exchangeData?.regularPrice?.toLocaleString() || "0"}원
                  입니다.
                </Typography>
                <Divider sx={{ margin: "20px 0" }} />
                <Button variant="contained" fullWidth>
                  채팅하기
                </Button>
                <Button variant="contained" fullWidth sx={{ mt: 2 }}>
                  결제하기
                </Button>
                {exchangeData?.isWriter === "TRUE" && (
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
                  작성일:{" "}
                  {exchangeData?.updatedAt
                    ? new Date(exchangeData.updatedAt).toLocaleDateString()
                    : "N/A"}
                </Typography>
                <Typography color="textSecondary">
                  조회: {exchangeData?.viewCount || 0}
                </Typography>
                <Divider sx={{ margin: "20px 0" }} />
                <Typography variant="body1">
                  {exchangeData?.content || "상품 설명이 없습니다."}
                </Typography>
              </Paper>
            </Grid>

            {/* Seller Info */}
            <Grid item xs={12} md={6}>
              <Paper elevation={3} sx={{ padding: "20px" }}>
                <Typography variant="h6">
                  {exchangeData?.writer || "판매자 정보"}
                </Typography>
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
                  {exchangeData?.recentExchangesByMember?.length > 0 ? (
                    exchangeData.recentExchangesByMember.map((item, index) => (
                      <Grid
                        item
                        xs={4}
                        key={index}
                        onClick={() => router.push(item.url)}
                        sx={{
                          cursor: "pointer",
                          "&:hover": {
                            boxShadow: 2,
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
                <Button
                  variant="contained"
                  color="error"
                  onClick={handleDelete}
                >
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
  }
};

export default ItemDetail;
