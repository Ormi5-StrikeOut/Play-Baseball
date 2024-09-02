const REQUEST_URL: string = "http://3.36.114.27:8080/api";

// Member Endpoints
export const MEMBER: string = `${REQUEST_URL}/members`;
export const MEMBER_SIGNUP: string = `${MEMBER}/join`;
export const MEMBER_MODIFY: string = `${MEMBER}/{memberId}`;
export const MEMBER_VERIFY: string = `${MEMBER}/verify/{memberId}`;
export const MEMBER_RESIGN: string = `${MEMBER}/my`;
export const MEMBER_LOGIN: string = `${MEMBER}/login`;
export const MEMBER_GET_ALL: string = `${MEMBER}`;
export const MEMBER_REISSUE_TOKEN: string = `${REQUEST_URL}/auth/reissue-token/{memberId}`;
export const MEMBER_GET: string = `${MEMBER}/{memberId}`;
export const MEMBER_LOGOUT: string = `${REQUEST_URL}/logout`;
export const MEMBER_MODIFY_ROLE: string = `${MEMBER}/verify-role/{memberId}`;

// Exchange Endpoints
export const EXCHANGE: string = `${REQUEST_URL}/exchanges`;
export const EXCHANGE_ADD: string = `${EXCHANGE}`;
export const EXCHANGE_MODIFY: string = `${EXCHANGE}/{id}`;
export const EXCHANGE_DELETE: string = `${EXCHANGE}/{id}`;
export const EXCHANGE_GET_ALL: string = `${EXCHANGE}`;
export const EXCHANGE_GET_LATEST_FIVE: string = `${EXCHANGE}/five`;
export const EXCHANGE_MODIFY_SALES_STATUS: string = `${EXCHANGE}/{id}`;
export const EXCHANGE_GET_MY: string = `${EXCHANGE}/{memberId}`;

// Review Endpoints
export const REVIEW: string = `${REQUEST_URL}/reviews`;
export const REVIEW_ADD: string = `${REVIEW}`;
export const REVIEW_MODIFY: string = `${REVIEW}/{id}`;
export const REVIEW_GET_MEMBER: string = `${REVIEW}/member/{id}`;
export const REVIEW_GET_MY: string = `${REVIEW}/my`;

// Like Endpoints
export const LIKE: string = `${REQUEST_URL}/exchange-likes`;
export const LIKE_ADD: string = `${LIKE}/{postId}`;
export const LIKE_DELETE: string = `${LIKE}/{postId}`;
export const LIKE_GET_MY: string = `${LIKE}/{userId}`;

// Chat Endpoints
export const CHATROOM: string = `${REQUEST_URL}/chatrooms`;
export const CHATROOM_ADD: string = `${CHATROOM}`;
export const CHATROOM_DELETE: string = `${CHATROOM}`;
export const CHAT: string = `${REQUEST_URL}/chat`;
export const CHAT_POST_MESSAGE: string = `${CHAT}`;
