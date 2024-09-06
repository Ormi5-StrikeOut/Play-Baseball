import { useRouter } from "next/router";
import ResultPage from "./result-page"; // ResultPage 컴포넌트

const Result = () => {
  const router = useRouter();
  const { isSuccess, message, buttonText, buttonAction } = router.query;

  // 버튼 클릭 시 이동할 경로 설정
  const handleButtonClick = () => {
    router.push(buttonAction as string);
  };

  return (
    <ResultPage
      isSuccess={isSuccess === "true"}
      message={message as string}
      buttonText={buttonText as string}
      buttonAction={handleButtonClick}
    />
  );
};

export default Result;
