import React from 'react';
import { useRouter } from 'next/router';
import RequestPasswordReset from '../../components/RequestPasswordReset';
import PasswordResetForm from '../../components/PasswordResetForm';

const ForgotPasswordPage: React.FC = () => {
  const router = useRouter();
  const { token } = router.query;

  // 수정 사항 1: 토큰 유무에 따라 다른 컴포넌트 렌더링
  return token ? <PasswordResetForm token={token as string} /> : <RequestPasswordReset />;
};

export default ForgotPasswordPage;