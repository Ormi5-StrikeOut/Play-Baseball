import React, { useState, useEffect, useRef } from "react";
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
import Wrapper from '../../components/Wrapper';
import axios from "axios";
import { config } from "process";
import SockJS from "sockjs-client";
import * as StompJs from "@stomp/stompjs";

const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleString('ko-KR', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit', 
    hour: '2-digit', 
    minute: '2-digit', 
    second: '2-digit' 
  });
};

axios.defaults.baseURL = process.env.NEXT_PUBLIC_API_URL;
axios.interceptors.request.use(
  (config) => {
    config.headers.Authorization = localStorage.getItem('Authorization');
    return config;
  },
  (err) => {
    console.log(err);
    return Promise.reject(err);
  }
);

const ChatInterface = () => {
  const [selectedChatRoom, setSelectedChatRoom] = useState(null);
  const [messageList, setMessageList] = useState([]);
  const [newMessage, setNewMessage] = useState("");
  const [page, setPage] = useState(0);
  const [member, setMember] = useState(null);
  const [messageRoomList, setMessageRoomList] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [opponentNicknames, setOpponentNicknames] = useState(null);
  const client = useRef(null);
  const URL = "https://api.ioshane.com/stomp/content";

  useEffect(() => {
    axios.get(`/messages/member?page=${page}`)
      .then((response) => {
        console.log(response.data.data)
        setTotalPages(response.data.totalPages);
        setMessageRoomList(response.data.data);

      })
      .catch((error) => {
        console.error("메시지 방 목록 요청 실패:", error);
      });

    axios.get('/members/my')
      .then(response => {
        console.log(response.data.data)
        setMember(response.data.data);
      })
      .catch(error => {
        console.error('회원 정보 요청 실패:', error);
      });

  }, [page]);

  const fetchMembers = async (roomId) => {
    try {
      const response = await axios.get(`/messages/room/${roomId}/members`);
        console.log(response.data)
  
        response.data.forEach(target => {
          
          if (member.id !== target.id) { 
            setOpponentNicknames(target.nickname); 
          }
  
        });
      
    } catch (err) {
      console.log(err) 
    } 
  };

  useEffect(() => {
    fetchMembers(selectedChatRoom);
    fetchAndSetMessages();
    connect();
  }, [selectedChatRoom]);

  const fetchAndSetMessages = () => {
    if (selectedChatRoom !== null) {
      axios.get(`/messages/rooms/member/${selectedChatRoom}`)
        .then(response => {
          const messages = response.data.data.messages;
          const sortedMessages = messages.reverse();
          setMessageList(sortedMessages);
        })
        .catch(error => {
          console.error('메시지 조회 실패:', error);
        });
    }
  };

  const connect = () => {
    console.log("Attempting to connect to WebSocket...");

    client.current = new StompJs.Client({

      webSocketFactory: () => new SockJS(URL),

      connectHeaders: {
        Authorization: localStorage.getItem('Authorization'),
      },

      // debug: (str) => console.log("STOMP: " + str),

      reconnectDelay: 3000,
      heartbeatIncoming: 2000,
      heartbeatOutgoing: 2000,

      onConnect: () => {
        console.log("웹소켓 연결 성공!");
        subscribe();
      },

      onStompError: (frame) => {
        console.error("STOMP 오류 발생!");
        console.error("Command:", frame.command);
        console.error("Headers:", frame.headers);
        console.error("Body:", frame.body);
      },

      onWebSocketClose: () => {
        console.log("웹소켓 연결이 닫혔습니다.");
      },

      onWebSocketError: (event) => {
        console.error("WebSocket 오류:", event);

      },
    });

    client.current.activate();
  };

  const subscribe = () => {
    if (client.current) {
      client.current.subscribe(`/sub/room/${selectedChatRoom}`, (res) => {
        fetchAndSetMessages();
      });
      // console.log(`채팅방 ${selectedChatRoom}에 구독 완료`);
    }
  };

  const handleChatRoomClick = (chatRoomId, isOtherUser) => {
    setSelectedChatRoom(chatRoomId);
    console.log(isOtherUser)
    setOpponentNicknames(isOtherUser);
  };

  const handleSendMessage = () => {
    publish();
  };

  const publish = () => {
    if (!client.current || !client.current.connected) {
      console.log("웹소켓이 연결되어 있지 않습니다. 메시지를 보낼 수 없습니다.");
      return;
    }

    client.current.publish({
      destination: `/pub/chats/${selectedChatRoom}`,
      body: JSON.stringify({
        memberId: member.id,
        messageRoomId: selectedChatRoom,
        messageContent: newMessage,
      }),
    });
    console.log("메시지 전송:", newMessage);
    setNewMessage("");
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
                {member?.nickname}
              </Typography>
              <Divider />
              <List>
              {messageRoomList.map((chatRoom) => {
                let otherUserNickname = null;

                chatRoom.messages.forEach((message) => {
                  if (message.member.nickname !== member.nickname) {
                    otherUserNickname = message.member.nickname;
                  }
                });

                return (
                  <React.Fragment key={chatRoom.messageRoomId}>
                    <ListItem
                      component={ButtonBase}
                      onClick={() => handleChatRoomClick(chatRoom.messageRoomId, otherUserNickname)}
                      sx={{
                        backgroundColor:
                          selectedChatRoom?.messageRoomId === chatRoom.messageRoomId
                            ? "#e0f7fa"
                            : "inherit",
                      }}
                    >
                      <ListItemText
                        primary={otherUserNickname ? otherUserNickname : 'Unknown User'} // 다른 사용자 닉네임 또는 'Unknown User' 표시
                        secondary={formatDate(chatRoom.lastMessageAt)}
                      />
                    </ListItem>
                  </React.Fragment>
                );
                })}
              </List>
            </Box>
          </Grid>

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
                  <Box sx={{ ml: 2 }}>
                    <Typography variant="h6">{opponentNicknames !== member.nickname ? opponentNicknames : "sdsad"}</Typography>
                  </Box>
                </Box>
                <Divider />
                <Box sx={{ flexGrow: 1, overflowY: "auto", p: 2 }}>
                  {messageList.length > 0 && messageList.map((msg) => {
                    const isOtherUser = msg.member.nickname === opponentNicknames;
                    return (
                      <Box key={msg.messageId} sx={{ mb: 3 }}>
                        <Box
                          sx={{
                            display: "flex",
                            justifyContent: !isOtherUser ? "flex-end" : "flex-start",
                          }}
                        >
                          <Box
                            sx={{
                              backgroundColor: !isOtherUser ? "#ffcc80" : "#f0f0f0",
                              color: "#000",
                              padding: "8px 12px",
                              borderRadius: "16px",
                              maxWidth: "60%",
                            }}
                          >
                            <Typography variant="body1">{msg.messageContent}</Typography>
                          </Box>
                        </Box>
                        <Typography
                          variant="caption"
                          sx={{
                            display: "block",
                            textAlign: !isOtherUser ? "right" : "left",
                            color: "#888",
                          }}
                        >
                          {formatDate(msg.createAt)}
                        </Typography>
                      </Box>
                    )
                  })}
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
