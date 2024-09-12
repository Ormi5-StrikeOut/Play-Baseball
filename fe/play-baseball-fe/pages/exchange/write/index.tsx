import React, { useEffect, useState } from "react";
import { useRouter } from "next/router";
import {
  Box,
  Button,
  TextField,
  Typography,
  CircularProgress,
} from "@mui/material";
import Image from "next/image";
import Wrapper from "../../../components/Wrapper";
import { EXCHANGE_ADD } from "@/constants/endpoints";
import axiosInstance from "@/components/axiosInstance";

const PostCreationForm = () => {
  const [title, setTitle] = useState("");
  const [price, setPrice] = useState("");
  const [content, setContent] = useState("");
  const [images, setImages] = useState<File[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem("Authorization");
    if (!token) {
      router.push("/auth/login");
    }
  }, [router]);

  const handleTitleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setTitle(event.target.value);
  };

  const handlePriceChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    const numericValue = value.replace(/[^0-9]/g, "");

    setPrice(numericValue);
  };

  const handleContentChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setContent(event.target.value);
  };

  const handleImageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) {
      const newImages = Array.from(event.target.files);
      setImages((prevImages) => [...prevImages, ...newImages]);
    }
  };

  const handleImageDelete = (index: number) => {
    setImages((prevImages) => prevImages.filter((_, i) => i !== index));
  };

  const handleSubmit = async () => {
    if (isLoading) return;

    // 입력값 검사
    if (!title || title.trim() === "") {
      alert("제목을 입력해주세요.");
      return; // 제출 중단
    }
    if (!content || content.trim() === "") {
      alert("설명을 입력해주세요.");
      return; // 제출 중단
    }
    // price 값 유효성 확인
    if (isNaN(Number(price))) {
      alert("가격에 올바른 숫자를 입력해주세요.");
      return; // 제출 중단
    }

    const token = localStorage.getItem("Authorization");
    if (!token) {
      router.push("/auth/login");
      return;
    }

    // 로딩 상태로 전환
    setIsLoading(true);

    const formData = new FormData();
    const jsonData = {
      title,
      price,
      content,
    };
    formData.append(
      "exchangeRequestDto",
      new Blob([JSON.stringify(jsonData)], { type: "application/json" })
    );

    images.forEach((image) => {
      formData.append("images", image);
    });

    try {
      const response = await axiosInstance.post(EXCHANGE_ADD, formData, {
        headers: {
          "Content-Type": "multipart/form-data",
        },
      });
      router.push({
        pathname: "/result",
        query: {
          isSuccess: "true",
          message: `글이 정상적으로 작성되었습니다. ${title}`,
          buttonText: "작성한 글 확인하기",
          buttonAction: `/exchange/${response.data.data.id}`,
        },
      });
    } catch (error) {
      router.push({
        pathname: "/result",
        query: {
          isSuccess: "false",
          message: `통신 오류가 발생했습니다: ${error}`,
          buttonText: "다시 시도하기",
          buttonAction: "/",
        },
      });
    } finally {
      // 로딩 상태 해제
      setIsLoading(false);
    }
  };

  return (
    <Wrapper>
      <Box
        component="form"
        sx={{
          display: "flex",
          flexDirection: "column",
          gap: 2,
          maxWidth: 600,
          margin: "0 auto",
        }}
      >
        <Typography variant="h5">상품 등록</Typography>
        <TextField label="제목" value={title} onChange={handleTitleChange} />
        <TextField
          label="가격"
          value={price}
          onChange={handlePriceChange}
          InputProps={{
            inputProps: {
              inputMode: "numeric", // 숫자 키패드가 뜨도록 설정
              pattern: "[0-9]*", // 숫자만 입력되도록 제한
            },
          }}
        />
        <TextField
          label="설명"
          multiline
          rows={4}
          value={content}
          onChange={handleContentChange}
        />

        <Box>
          <Typography>상품 이미지 ({images.length}/12)</Typography>
          <Button variant="outlined" component="label">
            이미지 등록
            <input type="file" hidden multiple onChange={handleImageChange} />
          </Button>
          <Box sx={{ display: "flex", gap: 1, marginTop: 2 }}>
            {images.map((image, index) => (
              <Box
                key={index}
                sx={{ position: "relative", width: 100, height: 100 }}
              >
                <Image
                  src={URL.createObjectURL(image)}
                  alt={`상품 이미지 ${index + 1}`}
                  layout="fill"
                  objectFit="cover"
                />
                <Button
                  variant="contained"
                  color="secondary"
                  sx={{ position: "absolute", top: 0, right: 0 }}
                  onClick={() => handleImageDelete(index)}
                >
                  X
                </Button>
              </Box>
            ))}
          </Box>

          <Button
            variant="contained"
            color="primary"
            onClick={handleSubmit}
            disabled={isLoading} // 로딩 중일 때 버튼 비활성화
          >
            {isLoading ? (
              <CircularProgress size={24} /> // 로딩 애니메이션
            ) : (
              "작성"
            )}
          </Button>
        </Box>
      </Box>
    </Wrapper>
  );
};

export default PostCreationForm;
