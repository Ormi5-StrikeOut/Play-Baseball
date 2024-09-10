import React, { useEffect, useState } from "react";
import {
  Box,
  Typography,
  Divider,
  List,
  ListItemButton,
  ListItemText,
} from "@mui/material";
import { useRouter } from "next/router"; // Next.js의 useRouter 사용
import qs from "qs";

const SaleStatusNavigation: React.FC = () => {
  const router = useRouter();
  const [selectedStatus, setSelectedStatus] = useState<string | null>(null);

  // 현재 URL의 쿼리에서 status 값 가져오기
  useEffect(() => {
    const { status } = router.query;
    if (status === "SALE" || status === "COMPLETE") {
      setSelectedStatus(status as string);
    } else {
      setSelectedStatus(null); // status가 없거나 NONE일 때
    }
  }, [router.query]);

  const handleNavigation = (status: string) => {
    if (selectedStatus === status) {
      // 이미 선택된 상태인 경우 "NONE"으로 변경
      status = "NONE";
    }
    const updatedQuery = { ...router.query, status };
    const url = `${window.location.pathname}?${qs.stringify(updatedQuery)}`;

    window.location.href = url;
  };

  return (
    <Box
      sx={{
        width: "100%",
        maxWidth: 360,
        bgcolor: "background.paper",
        borderRadius: 2,
        boxShadow: 3,
      }}
    >
      {/* 제목 영역 */}
      <Box
        sx={{
          bgcolor: "#7B4EFF",
          padding: "16px",
          borderRadius: "8px 8px 0 0",
        }}
      >
        <Typography variant="h6" color="white" align="center">
          판매 상태
        </Typography>
      </Box>

      {/* 판매 상태 리스트 */}
      <List>
        {/* 판매중 상태 */}
        <ListItemButton
          onClick={() => handleNavigation("SALE")}
          sx={{
            bgcolor: selectedStatus === "SALE" ? "#E0E0E0" : "transparent", // 선택된 경우 배경색 적용
            "&:hover": {
              bgcolor: "#F5F5F5",
            },
          }}
        >
          <ListItemText
            primary={
              <Typography variant="h6" color="textPrimary">
                판매중
              </Typography>
            }
            secondary={
              <Typography variant="body2" color="textSecondary">
                현재 판매중인 게시물만 검색합니다.
              </Typography>
            }
          />
        </ListItemButton>
        <Divider />

        {/* 판매완료 상태 */}
        <ListItemButton
          onClick={() => handleNavigation("COMPLETE")}
          sx={{
            bgcolor: selectedStatus === "COMPLETE" ? "#E0E0E0" : "transparent", // 선택된 경우 배경색 적용
            "&:hover": {
              bgcolor: "#F5F5F5",
            },
          }}
        >
          <ListItemText
            primary={
              <Typography variant="h6" color="textPrimary">
                판매완료
              </Typography>
            }
            secondary={
              <Typography variant="body2" color="textSecondary">
                이미 판매 완료된 게시물만 검색합니다.
              </Typography>
            }
          />
        </ListItemButton>
      </List>
    </Box>
  );
};

export default SaleStatusNavigation;
