import React from "react";
import { AppProps } from "next/app";
import Header from "../components/header";
import { CssBaseline, Container } from "@mui/material";
import { ThemeProvider, createTheme } from "@mui/material/styles";

const theme = createTheme();

const MyApp = ({ Component, pageProps }: AppProps) => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Header />
      <Container>
        <Component {...pageProps} />
      </Container>
    </ThemeProvider>
  );
};

export default MyApp;
