import React, { useState } from "react";
import {
  Box,
  TextField,
  Button,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Typography,
} from "@mui/material";
import Image from "next/image";
import { SelectChangeEvent } from "@mui/material/Select";
import Wrapper from '../../components/Wrapper'

const PostCreationForm = () => {
  const [title, setTitle] = useState("");
  const [category, setCategory] = useState("");
  const [price, setPrice] = useState("");
  const [description, setDescription] = useState("");
  const [images, setImages] = useState<File[]>([]);
  const [mainImageIndex, setMainImageIndex] = useState<number | null>(null);

  const handleTitleChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setTitle(event.target.value);
  };

  const handleCategoryChange = (event: SelectChangeEvent<string>) => {
    setCategory(event.target.value);
  };

  const handlePriceChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setPrice(event.target.value);
  };

  const handleDescriptionChange = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setDescription(event.target.value);
  };

  const handleImageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.files) {
      const newImages = Array.from(event.target.files);
      setImages((prevImages) => [...prevImages, ...newImages]);
    }
  };

  const handleImageClick = (index: number) => {
    setMainImageIndex(index);
  };

  const handleImageDelete = (index: number) => {
    setImages((prevImages) => prevImages.filter((_, i) => i !== index));
    if (mainImageIndex === index) {
      setMainImageIndex(null);
    }
  };

  const handleSubmit = () => {
    console.log({
      title,
      category,
      price,
      description,
      images,
      mainImageIndex,
    });
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
        <FormControl>
          <InputLabel id="category-label">카테고리</InputLabel>
          <Select
            labelId="category-label"
            value={category}
            onChange={handleCategoryChange}
          >
            <MenuItem value="전자기기">전자기기</MenuItem>
            <MenuItem value="가구">가구</MenuItem>
            <MenuItem value="의류">의류</MenuItem>
            {/* 필요한 카테고리 추가 */}
          </Select>
        </FormControl>
        <TextField label="가격" value={price} onChange={handlePriceChange} />
        <TextField
          label="설명"
          multiline
          rows={4}
          value={description}
          onChange={handleDescriptionChange}
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
                  onClick={() => handleImageClick(index)}
                  style={{
                    border: mainImageIndex === index ? "2px solid blue" : "none",
                  }}
                />
                {mainImageIndex === index && (
                  <Typography
                    sx={{
                      position: "absolute",
                      top: 0,
                      left: 0,
                      backgroundColor: "white",
                      padding: "2px 4px",
                    }}
                  >
                    대표이미지
                  </Typography>
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
          작성
        </Button>
      </Box>
    </Wrapper>
  );
};

export default PostCreationForm;
