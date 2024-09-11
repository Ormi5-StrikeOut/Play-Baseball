import React, { useState, useEffect } from "react";
import { useRouter } from "next/router";
import {
  Box,
  TextField,
  Button,
  Typography,
  MenuItem,
  Select,
  SelectChangeEvent,
  Container,
} from "@mui/material";
import Image from "next/image";
import axios from "axios";
import Wrapper from "../../../components/Wrapper";
import { EXCHANGE } from "@/constants/endpoints";

// Enum 정의
enum ExchangeStatus {
  SALE = "SALE",
  COMPLETE = "COMPLETE",
}

// 이미지 타입 정의
interface ImageData {
  id: number;
  url: string;
}

const EditPostForm = () => {
  const [title, setTitle] = useState<string>("");
  const [price, setPrice] = useState<number>(0);
  const [content, setContent] = useState<string>("");
  const [status, setStatus] = useState<ExchangeStatus>(ExchangeStatus.SALE);
  const [images, setImages] = useState<(File | ImageData)[]>([]); // 기존 URL 이미지와 파일을 함께 저장
  const [loading, setLoading] = useState<boolean>(true);
  const router = useRouter();
  const { id } = router.query;
  const token =
    typeof window !== "undefined"
      ? localStorage.getItem("Authorization")
      : null;

  // 게시물 데이터를 불러오는 useEffect
  useEffect(() => {
    if (id) {
      const fetchPostData = async () => {
        try {
          const response = await axios.get(`${EXCHANGE}/${id}`, {
            headers: {
              Authorization: token,
            },
            withCredentials: true,
          });

          const { title, price, content, status, images } = response.data.data;

          setTitle(title);
          setPrice(price);
          setContent(content);
          setStatus(status as ExchangeStatus);
          setImages(images); // 서버로부터 받은 이미지들 (ImageData 형식)
        } catch (error) {
          console.error("Error fetching post data:", error);
        }
        setLoading(false);
      };
      fetchPostData();
    }
  }, [id]);

  const handleTitleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setTitle(event.target.value);
  };

  const handlePriceChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const value = event.target.value;
    const numericValue = value.replace(/[^0-9]/g, "");
    setPrice(parseInt(numericValue));
  };

  const handleContentChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setContent(event.target.value);
  };

  const handleStatusChange = (event: SelectChangeEvent<ExchangeStatus>) => {
    setStatus(event.target.value as ExchangeStatus);
  };

  // 새 이미지를 추가하는 함수
  const handleImageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) {
      const newImages = Array.from(event.target.files); // 새로 선택된 파일들을 배열로 변환
      setImages((prevImages) => [...prevImages, ...newImages]); // 기존 이미지와 새 이미지를 합침
    }
  };

  // 이미지를 삭제하는 함수
  const handleImageDelete = (index: number) => {
    setImages((prevImages) => prevImages.filter((_, i) => i !== index)); // 해당 인덱스의 이미지 삭제
  };

  // 서버로 폼 데이터를 전송하는 함수
  const handleSubmit = async () => {
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

    const formData = new FormData();
    const jsonData = {
      title,
      price,
      content,
      status,
    };

    // 폼 데이터에 JSON 데이터 추가
    formData.append(
      "exchangeRequestDto",
      new Blob([JSON.stringify(jsonData)], { type: "application/json" })
    );

    // 이미지 파일만 formData에 추가 (기존 URL 이미지는 제외)
    images.forEach((image) => {
      if (image instanceof File) {
        formData.append("images", image); // 새로 추가된 파일만 전송
      }
    });

    try {
      const response = await axios.put(`${EXCHANGE}/${id}`, formData, {
        headers: {
          Authorization: token,
          "Content-Type": "multipart/form-data",
        },
        withCredentials: true,
      });

      router.push({
        pathname: "/result",
        query: {
          isSuccess: "true",
          message: `글이 정상적으로 수정되었습니다. ${title}`,
          buttonText: "수정된 글 확인하기",
          buttonAction: `/exchange/${id}`,
        },
      });
    } catch (error) {
      router.push({
        pathname: "/result",
        query: {
          isSuccess: "false",
          message: `수정 도중 오류가 발생했습니다: ${error}`,
          buttonText: "다시 시도하기",
          buttonAction: `/exchange/write/${id}`,
        },
      });
    }
  };

  if (loading) {
    return (
      <Wrapper>
        <Container maxWidth="lg" style={{ marginTop: "20px" }}>
          <Typography variant="h6">Loading...</Typography>
        </Container>
      </Wrapper>
    );
  }

  return (
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
      <Typography variant="h5">상품 수정</Typography>
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

      <Typography>상태</Typography>
      <Select value={status} onChange={handleStatusChange}>
        <MenuItem value={ExchangeStatus.SALE}>판매중</MenuItem>
        <MenuItem value={ExchangeStatus.COMPLETE}>판매완료</MenuItem>
      </Select>

      <Box>
        <Typography>이미지 ({images.length}/12)</Typography>
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
              {image instanceof File ? (
                <Image
                  src={URL.createObjectURL(image)} // 새로 업로드한 이미지 미리보기
                  alt={`새 이미지 ${index + 1}`}
                  layout="fill"
                  objectFit="cover"
                />
              ) : (
                <Image
                  src={image.url} // 서버에서 가져온 기존 이미지
                  alt={`기존 이미지 ${index + 1}`}
                  layout="fill"
                  objectFit="cover"
                />
              )}
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
      </Box>

      <Button variant="contained" color="primary" onClick={handleSubmit}>
        수정 완료
      </Button>
    </Box>
  );
};

export default EditPostForm;
