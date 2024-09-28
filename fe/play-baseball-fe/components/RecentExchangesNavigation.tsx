import React, { useEffect, useState } from "react";
import {
    Box,
    Typography,
    Divider,
    List,
    Avatar,
    ListItemButton,
    ListItemText,
} from "@mui/material";
import VisibilityIcon from "@mui/icons-material/Visibility";
import Link from "next/link";
import axios from "axios";
import { DEFAULT_IMAGE, EXCHANGE } from "@/constants/endpoints";

interface ExchangeNavigationResponseDto {
    title: string;
    price: number;
    url: string;
    imageUrl: string;
    updatedAt: string;
    viewCount: number;
    status: "SALE" | "COMPLETE";
}

const formatTimeAgo = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInSeconds = Math.floor((now.getTime() - date.getTime()) / 1000);

    if (diffInSeconds < 60) return "방금 전";
    const diffInMinutes = Math.floor(diffInSeconds / 60);
    if (diffInMinutes < 60) return `${diffInMinutes}분 전`;
    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) return `${diffInHours}시간 전`;
    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 30) return `${diffInDays}일 전`;
    const diffInMonths = Math.floor(diffInDays / 30);
    if (diffInMonths < 12) return `${diffInMonths}개월 전`;
    const diffInYears = Math.floor(diffInMonths / 12);
    return `${diffInYears}년 전`;
};

const truncateText = (text: string, maxLength: number) => {
    if (text.length > maxLength) {
        return text.slice(0, maxLength) + "...";
    }
    return text;
};

const RecentExchangesNavigation: React.FC = () => {
    const [recentPosts, setRecentPosts] = useState<ExchangeNavigationResponseDto[]>([]);

    useEffect(() => {
        const fetchRecentPosts = async () => {
            try {
                const response = await axios.get(`${EXCHANGE}/five`);
                const posts = response.data.data;
                setRecentPosts(posts.slice(0, 5));
            } catch (error) {
                console.error("Error fetching recent posts:", error);
            }
        };

        fetchRecentPosts();
    }, []);

    if (recentPosts.length === 0) {
        return null;
    }

    return (
        <Box
            sx={{
                width: { xs: "100%", sm: "80%", md: "60%", lg: "50%", xl: "360px" },
                maxWidth: "100%",
                bgcolor: "background.paper",
                borderRadius: 2,
                boxShadow: 3,
                mx: "auto",
            }}
        >
            <Box
                sx={{
                    bgcolor: "#7B4EFF",
                    padding: "16px",
                    borderRadius: "8px 8px 0 0",
                }}
            >
                <Typography variant="h6" color="white" align="center" component="div">
                    최근 게시물
                </Typography>
            </Box>

            <List>
                {recentPosts.map((exchange, index) => (
                    <React.Fragment key={index}>
                        <ListItemButton component={Link} href={exchange.url}>
                            <Avatar
                                src={exchange.imageUrl || DEFAULT_IMAGE}
                                sx={{ width: 50, height: 50, marginRight: "16px" }}
                            />
                            <div>
                                <ListItemText
                                    primary={
                                        <Typography variant="body1" noWrap component="div">
                                            {truncateText(exchange.title, 20)}
                                        </Typography>
                                    }
                                    secondary={
                                        <Box component="span" sx={{ display: "flex", alignItems: "center" }}>
                                            <VisibilityIcon
                                                sx={{
                                                    fontSize: 16,
                                                    marginRight: "4px",
                                                    marginTop: "2px",
                                                }}
                                            />
                                            <Typography variant="body2" color="textSecondary" component="span">
                                                {exchange.viewCount} | {formatTimeAgo(exchange.updatedAt)}
                                            </Typography>
                                        </Box>
                                    }
                                />
                            </div>
                        </ListItemButton>
                        {index < recentPosts.length - 1 && <Divider />}
                    </React.Fragment>
                ))}
            </List>
        </Box>
    );
};

export default RecentExchangesNavigation;