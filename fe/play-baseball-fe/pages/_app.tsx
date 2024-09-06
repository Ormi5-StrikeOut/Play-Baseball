import React from "react";
import { AppProps } from "next/app";
import Header from "../components/header";
import { CssBaseline, Container, GlobalStyles } from "@mui/material";
import { ThemeProvider, createTheme } from "@mui/material/styles";

const theme = createTheme();

const globalStyles = (
  <GlobalStyles
    styles={{
      body: {
        backgroundColor: "#F0F0F0",
        margin: 0,
        padding: 0,
        boxSizing: "border-box",
      },
      a: {
        textDecoration: "none",
      },
    }}
  />
);

const MyApp = ({ Component, pageProps }: AppProps) => {
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Header />
      {globalStyles}
      <Container>
        <Component {...pageProps} />
      </Container>
    </ThemeProvider>
  );
};

export default MyApp;
