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

const MainPage = () => {
  const items = [
    {
      id: 1,
      title: "아이템 1",
      price: "1,430,000 원",
      imageUrl: "/exchange/image.jpg",
      link: "/exchange/1",
    },
    {
      id: 2,
      title: "아이템 2",
      price: "90,000 원",
      imageUrl: "/exchange/image2.jpg",
      link: "/product/2",
    },
    {
      id: 3,
      title: "아이템 3",
      price: "70,000 원",
      imageUrl: "/exchange/image3.jpg",
      link: "/product/3",
    },
    {
      id: 4,
      title: "아이템 4",
      price: "19,000 원",
      imageUrl: "/exchange/image2.jpg",
      link: "/product/4",
    },
    {
      id: 5,
      title: "아이템 5",
      price: "420,000 원",
      imageUrl: "/exchange/image.jpg",
      link: "/product/5",
    },
  ];

  return (
    <Container maxWidth="lg" sx={{ py: 3 }}>
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
            <Link href={item.link} passHref>
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
                    {item.price}
                  </Typography>
                </CardContent>
              </Card>
            </Link>
          </Grid>
        ))}
      </Grid>
    </Container>
  );
};

export default MainPage;
