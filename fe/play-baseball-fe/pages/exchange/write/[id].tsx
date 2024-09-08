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
} from "@mui/material";
import Image from "next/image";
import axios from "axios";
import { EXCHANGE } from "@/constants/endpoints";

const EditPostForm = () => {
  const [title, setTitle] = useState("");
  const [price, setPrice] = useState("");
  const [content, setContent] = useState("");
  const [status, setStatus] = useState("");
  const [images, setImages] = useState<(File | string)[]>([]);
  const router = useRouter();
  const { id } = router.query;

  useEffect(() => {
    if (id) {
      const fetchPostData = async () => {
        try {
          const response = await axios.get(`/api/exchanges/${id}`);
          const { title, price, content, status, images } = response.data.data;
          setTitle(title);
          setPrice(price);
          setContent(content);
          setStatus(status);
          setImages(images);
        } catch (error) {
          console.error("Error fetching post data:", error);
        }
      };
      fetchPostData();
    }
  }, [id]);

  const handleTitleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setTitle(event.target.value);
  };

  const handlePriceChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setPrice(event.target.value);
  };

  const handleContentChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setContent(event.target.value);
  };

  const handleStatusChange = (event: SelectChangeEvent<string>) => {
    setStatus(event.target.value as string);
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
    const formData = new FormData();
    const token =
      typeof window !== "undefined"
        ? localStorage.getItem("Authorization")
        : null;
    const jsonData = {
      title,
      price,
      content,
      status,
    };
    formData.append(
      "exchangeRequestDto",
      new Blob([JSON.stringify(jsonData)], { type: "application/json" })
    );

    images.forEach((image) => {
      // 파일과 기존 URL 이미지를 분리하여 처리
      if (typeof image !== "string") {
        formData.append("images", image);
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
      <TextField label="가격" value={price} onChange={handlePriceChange} />
      <TextField
        label="설명"
        multiline
        rows={4}
        value={content}
        onChange={handleContentChange}
      />

      <Typography>상태</Typography>
      <Select value={status} onChange={handleStatusChange}>
        <MenuItem value="SALE">판매중</MenuItem>
        <MenuItem value="COMPLETE">판매완료</MenuItem>
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
              {typeof image === "string" ? (
                <Image
                  src={image}
                  alt={`기존 이미지 ${index + 1}`}
                  layout="fill"
                  objectFit="cover"
                />
              ) : (
                <Image
                  src={URL.createObjectURL(image)}
                  alt={`새 이미지 ${index + 1}`}
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
