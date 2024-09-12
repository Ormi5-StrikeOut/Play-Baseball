import React, { useState } from "react";
import {
  Box,
  Grid,
  List,
  ListItem,
  ListItemText,
  Typography,
  TextField,
  Button,
  Divider,
  Avatar,
  ListItemAvatar,
  ButtonBase,
} from "@mui/material";
import Wrapper from '../../components/Wrapper'

interface ChatRoom {
  id: string;
  name: string;
  lastMessage: string;
  time: string;
  unreadCount: number;
  avatarUrl: string;
}

interface ChatMessage {
  id: string;
  sender: string;
  message: string;
  time: string;
  isSent: boolean; // true면 보낸 메시지, false면 받은 메시지
}

// 채팅방 데이터
const chatRooms: ChatRoom[] = [
  {
    id: "1",
    name: "신반도",
    lastMessage: "조이조이님이 이모티콘을 보냈어요.",
    time: "2시간 전",
    unreadCount: 2,
    avatarUrl: "https://example.com/avatar1.jpg",
  },
  {
    id: "2",
    name: "레모네이드주세용",
    lastMessage: "만나서 반가워요",
    time: "5월 27일",
    unreadCount: 0,
    avatarUrl: "https://example.com/avatar2.jpg",
  },
  {
    id: "3",
    name: "당근이네 빵집",
    lastMessage: "사장님 소금빵 너무 맛있어요!",
    time: "5월 27일",
    unreadCount: 1,
    avatarUrl: "https://example.com/avatar3.jpg",
  },
];

const chatMessagesByRoom: { [key: string]: ChatMessage[] } = {
  "1": [
    {
      id: "1",
      sender: "신반도",
      message: "안녕하세요",
      time: "오후 3:42",
      isSent: false,
    },
    {
      id: "2",
      sender: "나",
      message: "혹시 유모차 팔렸나요?",
      time: "오후 3:42",
      isSent: true,
    },
    {
      id: "3",
      sender: "나",
      message: "직거래 하고 싶은데요",
      time: "오후 3:44",
      isSent: true,
    },
    {
      id: "4",
      sender: "신반도",
      message: "아니요 아직 안팔렸어요",
      time: "오후 3:44",
      isSent: false,
    },
    {
      id: "5",
      sender: "신반도",
      message:
        "내일 역삼역 앞으로 7시까지 와주실 수 있나요? 퇴근하고 나가서 드리면 좋을 것 같아요~^^",
      time: "오후 3:45",
      isSent: false,
    },
  ],
  "2": [
    {
      id: "1",
      sender: "레모네이드주세용",
      message: "안녕하세요, 만나서 반가워요!",
      time: "오후 2:30",
      isSent: false,
    },
    {
      id: "2",
      sender: "나",
      message: "네, 반갑습니다. 레모네이드 주문 가능할까요?",
      time: "오후 2:31",
      isSent: true,
    },
  ],
  "3": [
    {
      id: "1",
      sender: "당근이네 빵집",
      message: "사장님 소금빵 너무 맛있어요!",
      time: "오후 2:30",
      isSent: false,
    },
  ],
};

const ChatInterface = () => {
  const [selectedChatRoom, setSelectedChatRoom] = useState<ChatRoom | null>(
    null
  );
  const [newMessage, setNewMessage] = useState("");

  const handleChatRoomClick = (chatRoom: ChatRoom) => {
    setSelectedChatRoom(chatRoom);
  };

  const handleSendMessage = () => {
    if (newMessage.trim() !== "" && selectedChatRoom) {
      const newChatMessage: ChatMessage = {
        id: (chatMessagesByRoom[selectedChatRoom.id].length + 1).toString(),
        sender: "나",
        message: newMessage,
        time: "오후 3:46",
        isSent: true,
      };
      chatMessagesByRoom[selectedChatRoom.id].push(newChatMessage);
      setNewMessage("");
    }
  };

  return (
    <Wrapper>
      <Box
        sx={{
          flexGrow: 1,
          p: 2,
          maxWidth: "60%",
          margin: "0 auto",
          minHeight: "80vh",
        }}
      >
        <Grid container spacing={2} sx={{ height: "955px" }}>
          {/* 채팅방 목록 */}
          <Grid item xs={12} md={4} sx={{ height: "100%" }}>
            <Box
              sx={{
                border: "1px solid #e0e0e0",
                borderRadius: "8px",
                height: "100%",
                overflowY: "auto",
              }}
            >
              <Typography variant="h6" sx={{ p: 2 }}>
                닉네임
              </Typography>
              <Divider />
              <List>
                {chatRooms.map((chatRoom) => (
                  <React.Fragment key={chatRoom.id}>
                    <ListItem
                      component={ButtonBase}
                      onClick={() => handleChatRoomClick(chatRoom)}
                      sx={{
                        backgroundColor:
                          selectedChatRoom?.id === chatRoom.id
                            ? "#e0f7fa"
                            : "inherit",
                      }}
                    >
                      {/* <ListItemAvatar>
                        <Avatar alt={chatRoom.name} src={chatRoom.avatarUrl} />
                      </ListItemAvatar> */}
                      <ListItemText
                        primary={chatRoom.name}
                        secondary={`${chatRoom.time} — ${chatRoom.lastMessage}`}
                      />
                      {chatRoom.unreadCount > 0 && (
                        <Box sx={{ ml: 1 }}>
                          {/* <Avatar
                            sx={{
                              bgcolor: "red",
                              width: 20,
                              height: 20,
                              fontSize: "0.8rem",
                            }}
                          >
                            {chatRoom.unreadCount}
                          </Avatar> */}
                        </Box>
                      )}
                    </ListItem>
                    <Divider />
                  </React.Fragment>
                ))}
              </List>
            </Box>
          </Grid>

          {/* 선택된 채팅방 대화 내역 */}
          <Grid item xs={12} md={8} sx={{ height: "100%" }}>
            {selectedChatRoom ? (
              <Box
                sx={{
                  border: "1px solid #e0e0e0",
                  borderRadius: "8px",
                  height: "100%",
                  display: "flex",
                  flexDirection: "column",
                }}
              >
                <Box sx={{ display: "flex", alignItems: "center", p: 2 }}>
                  {/* <Avatar
                    alt={selectedChatRoom.name}
                    src={selectedChatRoom.avatarUrl}
                  /> */}
                  <Box sx={{ ml: 2 }}>
                    <Typography variant="h6">{selectedChatRoom.name}</Typography>
                    <Typography variant="body2" color="textSecondary">
                      보통 1시간 이내 응답
                    </Typography>
                  </Box>
                </Box>
                <Divider />
                <Box sx={{ flexGrow: 1, overflowY: "auto", p: 2 }}>
                  {chatMessagesByRoom[selectedChatRoom.id].map((msg) => (
                    <Box key={msg.id} sx={{ mb: 3 }}>
                      <Box
                        sx={{
                          display: "flex",
                          justifyContent: msg.isSent ? "flex-end" : "flex-start",
                        }}
                      >
                        <Box
                          sx={{
                            backgroundColor: msg.isSent ? "#ffcc80" : "#f0f0f0",
                            color: "#000",
                            padding: "8px 12px",
                            borderRadius: "16px",
                            maxWidth: "60%",
                          }}
                        >
                          <Typography variant="body1">{msg.message}</Typography>
                        </Box>
                      </Box>
                      <Typography
                        variant="caption"
                        sx={{
                          display: "block",
                          textAlign: msg.isSent ? "right" : "left",
                          color: "#888",
                        }}
                      >
                        {msg.time}
                      </Typography>
                    </Box>
                  ))}
                </Box>
                <Box
                  sx={{
                    display: "flex",
                    alignItems: "center",
                    borderTop: "1px solid #e0e0e0",
                    padding: 1,
                  }}
                >
                  <TextField
                    fullWidth
                    variant="outlined"
                    placeholder="메시지를 입력하세요..."
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    sx={{ mr: 1 }}
                  />
                  <Button
                    variant="contained"
                    color="primary"
                    onClick={handleSendMessage}
                  >
                    전송
                  </Button>
                </Box>
              </Box>
            ) : (
              <Typography variant="body1">대화방을 선택하세요.</Typography>
            )}
          </Grid>
        </Grid>
      </Box>
    </Wrapper>
  );
};

export default ChatInterface;
