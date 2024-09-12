export const SERVER_URL = process.env.NEXT_PUBLIC_NEC; // FE 서버 주소
const REQUEST_URL = process.env.NEXT_PUBLIC_API_URL; // BE 서버 주소
const RESOURCE_URL = "https://resource.ioshane.com";

// Resources
export const DEFAULT_IMAGE = `${RESOURCE_URL}/default.jpg`;
export const DEFAULT_BANNER = `${RESOURCE_URL}/banner.webp`;

// Member Endpoints
export const MEMBER: string = `${REQUEST_URL}/members`;
export const MEMBER_SIGNUP: string = `${MEMBER}/join`;
export const MEMBER_MODIFY: string = `${MEMBER}/my/modify-member`;
export const MEMBER_VERIFY: string = `${MEMBER}/verify-email`;
export const MEMBER_VERIFY_RESEND: string = `${MEMBER}/resend-verification-email`;
export const MEMBER_REQUEST_PASSWORD_RESET: string = `${MEMBER}/request-password-reset`;
export const MEMBER_RESET_PASSWORD: string = `${MEMBER}/reset-password`;
export const MEMBER_MY: string = `${MEMBER}/my`;
export const MEMBER_RESIGN: string = `${MEMBER}/my/resign`
export const MEMBER_LOGIN: string = `${REQUEST_URL}/auth/login`;
export const MEMBER_GET_ALL: string = `${MEMBER}`;
export const MEMBER_GET: string = `${MEMBER}/{memberId}`;
export const MEMBER_LOGOUT: string = `${REQUEST_URL}/auth/logout`;
export const MEMBER_MODIFY_ROLE: string = `${MEMBER}/verify-role/{memberId}`;

// Exchange Endpoints
export const EXCHANGE: string = `${REQUEST_URL}/exchanges`;
export const EXCHANGE_SEARCH: string = `${EXCHANGE}/search`;
export const EXCHANGE_ADD: string = `${EXCHANGE}`;
export const EXCHANGE_MODIFY: string = `${EXCHANGE}/{id}`;
export const EXCHANGE_DELETE: string = `${EXCHANGE}/{id}`;
export const EXCHANGE_GET_ALL: string = `${EXCHANGE}`;
export const EXCHANGE_GET_LATEST_FIVE: string = `${EXCHANGE}/five`;
export const EXCHANGE_MODIFY_SALES_STATUS: string = `${EXCHANGE}/{id}`;
export const EXCHANGE_GET_MY: string = `${EXCHANGE}/{memberId}`;
export const EXCHANGE_LIKE: string = `${REQUEST_URL}/likes`;

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

// Pages
export const PAGE_SEARCH: string = `${SERVER_URL}/search`;
