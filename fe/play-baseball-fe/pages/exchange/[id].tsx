import React, { useState } from "react";
import {
  Container,
  Box,
  Typography,
  Divider,
  Button,
  IconButton,
  Card,
  CardContent,
  Grid,
  Fade,
  Rating,
  Paper,
} from "@mui/material";
import { ArrowBack, ArrowForward } from "@mui/icons-material";
import Image from "next/image";
import Wrapper from '../../components/Wrapper'

// Temporary data
const itemImages = [
  { img: "/exchange/image.jpg", title: "Image 1" },
  { img: "/exchange/image2.jpg", title: "Image 2" },
  { img: "/exchange/image3.jpg", title: "Image 3" },
];

const recommendedItems = [
  { img: "/exchange/image.jpg", title: "Product 1", price: "10,000원" },
  { img: "/exchange/image2.jpg", title: "Product 2", price: "15,000원" },
  { img: "/exchange/image3.jpg", title: "Product 3", price: "20,000원" },
  { img: "/exchange/image.jpg", title: "Product 4", price: "25,000원" },
];

const ItemDetail: React.FC = () => {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [hover, setHover] = useState(false);

  const handlePrev = () => {
    setCurrentIndex(
      (prevIndex) => (prevIndex - 1 + itemImages.length) % itemImages.length
    );
  };

  const handleNext = () => {
    setCurrentIndex((prevIndex) => (prevIndex + 1) % itemImages.length);
  };

  const handleMouseEnter = () => {
    setHover(true);
  };

  const handleMouseLeave = () => {
    setHover(false);
  };

  return (
    <Wrapper>
      <Container maxWidth="lg" style={{ marginTop: "20px" }}>
        `<Grid container spacing={2}>
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
                  src={itemImages[currentIndex].img}
                  alt={itemImages[currentIndex].title}
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
                {itemImages.map((_, index) => (
                  <Box
                    key={index}
                    onClick={() => setCurrentIndex(index)}
                    sx={{
                      width: "10px",
                      height: "10px",
                      margin: "0 5px",
                      borderRadius: "50%",
                      backgroundColor: currentIndex === index ? "black" : "gray",
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
              <Typography variant="h5">휴대폰</Typography>
              <Typography variant="body1">휴대폰-삼성</Typography>
              <Typography variant="h5" color="primary" sx={{ marginTop: "10px" }}>
                191,000원
              </Typography>
              <Divider sx={{ margin: "20px 0" }} />
              <Typography variant="body1">주소</Typography>
              <Typography variant="body1">상태</Typography>
              <Divider sx={{ margin: "20px 0" }} />
              <Typography variant="body1">
                이 상품의 정가는 1,000,000원 입니다.
              </Typography>
              <Divider sx={{ margin: "20px 0" }} />
              <Button variant="contained" fullWidth>
                채팅하기
              </Button>
              <Button variant="contained" fullWidth sx={{ mt: 2 }}>
                결제하기
              </Button>
            </Paper>
          </Grid>

          {/* Details */}
          <Grid item xs={12} md={6}>
            <Paper elevation={3} sx={{ padding: "20px" }}>
              <Typography variant="h4">상품 정보</Typography>
              <Typography color="textSecondary" sx={{ marginTop: "10px" }}>
                작성일: 2024년 8월 29일
              </Typography>
              <Typography color="textSecondary">조회: 123 채팅: 1212</Typography>
              <Divider sx={{ margin: "20px 0" }} />
              <Typography variant="body1">
                휴대폰 팝니다 <br />
                휴대폰 팝니다 <br />
                휴대폰 팝니다 <br />
                휴대폰 팝니다 <br />
                휴대폰 팝니다 <br />
                휴대폰 팝니다 <br />
                휴대폰 팝니다 <br />
                휴대폰 팝니다 <br />
                휴대폰 팝니다 <br />
                휴대폰 팝니다 <br />
                휴대폰 팝니다 <br />
                휴대폰 팝니다 <br />
                휴대폰 팝니다 <br />
                휴대폰 팝니다 <br />
              </Typography>
            </Paper>
          </Grid>

          {/* Seller Info */}
          <Grid item xs={12} md={6}>
            <Paper elevation={3} sx={{ padding: "20px" }}>
              <Typography variant="h6">판매자 정보</Typography>
              <Box display="flex" alignItems="center">
                <Box display="flex" alignItems="center">
                  <Typography variant="h6">닉네임</Typography>
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
                {recommendedItems.slice(0, 3).map((item, index) => (
                  <Grid item xs={4} key={index}>
                    <Image
                      src={item.img}
                      alt={item.title}
                      layout="responsive"
                      width={100}
                      height={100}
                      objectFit="cover"
                      style={{ borderRadius: "4px" }}
                    />
                    <Typography variant="caption" display="block" align="center">
                      {item.title}
                    </Typography>
                    <Typography variant="caption" display="block" align="center">
                      {item.price}
                    </Typography>
                  </Grid>
                ))}
              </Grid>
            </Paper>
          </Grid>

          {/* Recommended Products */}
          <Grid item xs={12} md={12}>
            <Typography variant="h5" gutterBottom>
              이런 상품은 어떠세요?
            </Typography>
            <Grid container spacing={2}>
              {recommendedItems.map((item, index) => (
                <Grid item xs={6} sm={4} key={index}>
                  <Card>
                    <Image
                      src={item.img}
                      alt={item.title}
                      layout="responsive"
                      width={100}
                      height={100}
                      objectFit="cover"
                    />
                    <CardContent>
                      <Typography variant="body1">{item.title}</Typography>
                      <Typography variant="body2" color="primary">
                        {item.price}
                      </Typography>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </Grid>
        </Grid>
      </Container>
    </Wrapper>
  );
};

export default ItemDetail;
