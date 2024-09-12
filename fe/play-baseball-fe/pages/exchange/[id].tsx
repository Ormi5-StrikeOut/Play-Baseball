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
import {
  ArrowBack,
  ArrowForward,
  Favorite,
  FavoriteBorder,
  Visibility,
} from "@mui/icons-material";
import Image from "next/image";
import Wrapper from "../../components/Wrapper";
import { useRouter } from "next/router";
import {
  DEFAULT_IMAGE,
  EXCHANGE,
  EXCHANGE_LIKE,
  SERVER_URL,
} from "@/constants/endpoints";

interface ImageType {
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
  images: ImageType[];
  writer: string;
  recentExchangesByMember: RecentExchange[];
  isWriter: "TRUE" | "FALSE";
  reviewCount: number;
  average: number;
  likeCount: number;
  isLike: boolean;
}

const ItemDetail: React.FC = () => {
  const [currentIndex, setCurrentIndex] = useState<number>(0);
  const [hover, setHover] = useState<boolean>(false);
  const [openModal, setOpenModal] = useState<boolean>(false);
  const [exchangeData, setExchangeData] =
    useState<ExchangeDetailResponseDto | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [isLike, setIsLike] = useState<boolean>(false);
  const [likeCount, setLikeCount] = useState<number>(0);
  const [loggedIn, setLoggedIn] = useState<boolean>(false);
  const router = useRouter();
  const { id } = router.query;
  const token =
    typeof window !== "undefined"
      ? localStorage.getItem("Authorization")
      : null;

  // 데이터 가져오는 함수
  useEffect(() => {
    setLoggedIn(!!token);
    const fetchExchangeData = async () => {
      if (!id) return; // id가 없는 경우 바로 반환
      try {
        const response = await axios.get<
          ApiResponse<ExchangeDetailResponseDto>
        >(`${EXCHANGE}/${id}`, {
          headers: {
            Authorization: token,
          },
          withCredentials: true,
        });

        setExchangeData(response.data.data);
        setIsLike(response.data.data.isLike);
        setLikeCount(response.data.data.likeCount);
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

  // 로딩 상태 변경
  useEffect(() => {
    if (exchangeData) {
      setLoading(false);
    }
  }, [exchangeData]);

  // 이전 이미지로 변경
  const handlePrev = () => {
    if (exchangeData?.images?.length) {
      setCurrentIndex(
        (prevIndex) =>
          (prevIndex - 1 + exchangeData.images.length) %
          exchangeData.images.length
      );
    }
  };

  // 다음 이미지로 변경
  const handleNext = () => {
    if (exchangeData?.images?.length) {
      setCurrentIndex(
        (prevIndex) => (prevIndex + 1) % exchangeData.images.length
      );
    }
  };

  // 찜 토글
  const toggleLike = async () => {
    try {
      const response = await axios.post(
        `${EXCHANGE_LIKE}`,
        {
          exchangeId: id,
        },
        {
          headers: {
            Authorization: token,
          },
          withCredentials: true,
        }
      );
      setIsLike(!isLike);
      //setLikeCount(isLike ? likeCount - 1 : likeCount + 1);
    } catch (error) {
      console.error("찜 토글 중 오류 발생:", error);
    }
  };

  // 삭제 처리 함수
  const handleDelete = async () => {
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

  const createMessageRoom = async (targetNickname: string) => {
    setLoading(true);
    axios.defaults.baseURL = process.env.NEXT_PUBLIC_API_URL;

    axios.interceptors.request.use(
      (config) => {
        config.headers.Authorization = localStorage.getItem("Authorization");
        return config;
      },
      (err) => {
        console.log(err);
        return Promise.reject(err);
      }
    );

    try {
      const response = await axios.post(`/messages/room/${targetNickname}`);
      console.log("메시지 방 생성 성공:", response.data);

      router.push("/chat");
    } catch (err) {
      console.error("메시지 방 생성 실패:", err);
    } finally {
      setLoading(false);
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
            {/* 이미지 갤러리 */}
            <Grid item xs={12} md={6}>
              <Box
                position="relative"
                display="flex"
                flexDirection="column"
                alignItems="center"
                onMouseEnter={() => setHover(true)}
                onMouseLeave={() => setHover(false)}
                sx={{ width: "100%", maxWidth: "100%" }}
              >
                <Box
                  display="flex"
                  alignItems="center"
                  justifyContent="center"
                  position="relative"
                  sx={{
                    width: "100%",
                    aspectRatio: "1 / 1",
                    overflow: "hidden",
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
                      layout="fill"
                      objectFit="cover"
                    />
                  ) : (
                    <Image
                      src={DEFAULT_IMAGE}
                      alt="Default image"
                      layout="fill"
                      objectFit="cover"
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

                {/* 이미지 인디케이터 */}
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

            {/* 상품 정보 */}
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
                  {exchangeData?.regularPrice === 0
                    ? "현재 앨런이 상품 가격을 탐색 중입니다."
                    : `앨런이 찾은 이 상품의 새 제품 가격은 ${exchangeData?.regularPrice?.toLocaleString()}원입니다.`}
                </Typography>
                <Divider sx={{ margin: "20px 0" }} />
                <Button
                  variant="contained"
                  fullWidth
                  onClick={() => {
                    if (loggedIn) {
                      createMessageRoom(exchangeData?.writer);
                    } else {
                      window.location.href = `${SERVER_URL}/login`;
                    }
                  }}
                >
                  채팅하기
                </Button>
                <Button
                  variant="contained"
                  fullWidth
                  sx={{ mt: 2 }}
                  onClick={() => alert("결제 기능은 아직 구현되지 않았습니다.")}
                >
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

            {/* 상품 상세 정보 */}
            <Grid item xs={12} md={6}>
              <Paper elevation={3} sx={{ padding: "20px" }}>
                <Typography variant="h4">상품 정보</Typography>
                <Box
                  display="flex"
                  alignItems="center"
                  sx={{ marginTop: "10px" }}
                >
                  {/* 조회수 표시 */}
                  <Box display="flex" alignItems="center" sx={{ mr: 2 }}>
                    <Visibility sx={{ fontSize: 16, mr: 0.5 }} />
                    <Typography color="textSecondary">
                      {exchangeData?.viewCount || 0}
                    </Typography>
                  </Box>

                  {/* 찜 버튼과 찜 개수 표시 */}
                  <Box display="flex" alignItems="center">
                    <IconButton onClick={toggleLike} sx={{ p: 0 }}>
                      {isLike ? (
                        <Favorite sx={{ color: "red" }} />
                      ) : (
                        <FavoriteBorder sx={{ color: "gray" }} />
                      )}
                    </IconButton>
                    <Typography color="textSecondary" sx={{ ml: 0.5 }}>
                      {likeCount}
                    </Typography>
                  </Box>
                </Box>
                <Divider sx={{ margin: "20px 0" }} />
                <Typography variant="body1">
                  {exchangeData?.content || "상품 설명이 없습니다."}
                </Typography>
              </Paper>
            </Grid>

            {/* 판매자 정보 */}
            <Grid item xs={12} md={6}>
              <Paper elevation={3} sx={{ padding: "20px" }}>
                <Typography variant="h6">
                  {exchangeData?.writer || "판매자 정보"}
                </Typography>
                <Box display="flex" alignItems="center">
                  <Rating
                    value={exchangeData.average}
                    precision={0.1}
                    readOnly
                  />
                  <Typography
                    variant="body2"
                    color="textSecondary"
                    sx={{ marginLeft: "5px" }}
                  >
                    ({exchangeData.reviewCount})
                  </Typography>
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
                        sx={{ cursor: "pointer", "&:hover": { boxShadow: 2 } }}
                      >
                        <Box
                          sx={{
                            position: "relative",
                            width: "100%",
                            height: 0,
                            paddingBottom: "75%",
                            overflow: "hidden",
                            borderRadius: "4px",
                          }}
                        >
                          <Image
                            src={item.imageUrl || DEFAULT_IMAGE}
                            alt={item.title}
                            layout="fill"
                            objectFit="cover"
                          />
                        </Box>
                        <Typography
                          variant="caption"
                          display="block"
                          align="center"
                          sx={{
                            whiteSpace: "nowrap", // 한 줄로 표시
                            overflow: "hidden", // 넘치는 텍스트 숨기기
                            textOverflow: "ellipsis", // 넘치는 텍스트를 ...으로 표시
                          }}
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

            {/* 삭제 확인 모달 */}
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
