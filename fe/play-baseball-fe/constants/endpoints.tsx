const REQUEST_URL: string = "http://localhost:8080/api";

// Member Endpoints
const MEMBER: string = `${REQUEST_URL}/members`;
const MEMBER_SIGNUP: string = `${MEMBER}/join`;
const MEMBER_MODIFY: string = `${MEMBER}/{memberId}`;
const MEMBER_VERIFY: string = `${MEMBER}/verify/{memberId}`;
const MEMBER_RESIGN: string = `${MEMBER}/my`;
const MEMBER_LOGIN: string = `${MEMBER}/login`;
const MEMBER_GET_ALL: string = `${MEMBER}`;
const MEMBER_REISSUE_TOKEN: string = `${REQUEST_URL}/auth/reissue-token/{memberId}`;
const MEMBER_GET: string = `${MEMBER}/{memberId}`;
const MEMBER_LOGOUT: string = `${REQUEST_URL}/logout`;
const MEMBER_MODIFY_ROLE: string = `${MEMBER}/verify-role/{memberId}`;

// Exchange Endpoints
const EXCHANGE: string = `${REQUEST_URL}/exchanges`;
const EXCHANGE_ADD: string = `${EXCHANGE}`;
const EXCHANGE_MODIFY: string = `${EXCHANGE}/{id}`;
const EXCHANGE_DELETE: string = `${EXCHANGE}/{id}`;
const EXCHANGE_GET_ALL: string = `${EXCHANGE}`;
const EXCHANGE_GET_LATEST_FIVE: string = `${EXCHANGE}/five`;
const EXCHANGE_MODIFY_SALES_STATUS: string = `${EXCHANGE}/{id}`;
const EXCHANGE_GET_MY: string = `${EXCHANGE}/{memberId}`;

// Review Endpoints
const REVIEW: string = `${REQUEST_URL}/reviews`;
const REVIEW_ADD: string = `${REVIEW}`;
const REVIEW_MODIFY: string = `${REVIEW}/{id}`;
const REVIEW_GET_MEMBER: string = `${REVIEW}/member/{id}`;
const REVIEW_GET_MY: string = `${REVIEW}/my`;

// Like Endpoints
const LIKE: string = `${REQUEST_URL}/exchange-likes`;
const LIKE_ADD: string = `${LIKE}/{postId}`;
const LIKE_DELETE: string = `${LIKE}/{postId}`;
const LIKE_GET_MY: string = `${LIKE}/{userId}`;

// Chat Endpoints
const CHATROOM: string = `${REQUEST_URL}/chatrooms`;
const CHATROOM_ADD: string = `${CHATROOM}`;
const CHATROOM_DELETE: string = `${CHATROOM}`;
const CHAT: string = `${REQUEST_URL}/chat`;
const CHAT_POST_MESSAGE: string = `${CHAT}`;
