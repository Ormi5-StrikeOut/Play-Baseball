import React from "react";
import { Container, Box, Typography, Button } from "@mui/material";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import ErrorIcon from "@mui/icons-material/Error";
import { useRouter } from "next/router";

// ResultPage 컴포넌트가 다양한 메세지와 버튼 동작을 받도록 확장
interface ResultPageProps {
  isSuccess: boolean;
  title?: string;
  message?: string;
  buttonText?: string;
  buttonAction?: () => void;
}

const ResultPage: React.FC<ResultPageProps> = ({
  isSuccess,
  title,
  message,
  buttonText,
  buttonAction,
}) => {
  const router = useRouter();

  // 기본 동작 정의
  const defaultAction = () => {
    router.push("/"); // 기본 경로
  };

  return (
    <Container maxWidth="sm" style={{ marginTop: "50px", textAlign: "center" }}>
      <Box display="flex" flexDirection="column" alignItems="center">
        {isSuccess ? (
          <>
            <CheckCircleIcon color="success" style={{ fontSize: "80px" }} />
            <Typography variant="h4" sx={{ mt: 2 }}>
                {title || "성공"}
            </Typography>

            <Typography variant="h5" sx={{ mt: 2 }}>
              {message || "요청이 성공적으로 완료되었습니다!"}{" "}
            </Typography>
          </>
        ) : (
          <>
            <ErrorIcon color="error" style={{ fontSize: "80px" }} />
              <Typography variant="h4" sx={{ mt: 2 }}>
                  {title || "실패"}
              </Typography>
            <Typography variant="h5" sx={{ mt: 2 }}>
              {message || "요청이 실패하였습니다. 다시 시도해 주세요."}{" "}
            </Typography>
          </>
        )}
        <Button
          variant="contained"
          color={isSuccess ? "primary" : "secondary"}
          sx={{ mt: 4 }}
          onClick={buttonAction || defaultAction}
        >
          {buttonText || (isSuccess ? "다음 단계로" : "다시 시도")}{" "}
        </Button>
      </Box>
    </Container>
  );
};

export default ResultPage;
