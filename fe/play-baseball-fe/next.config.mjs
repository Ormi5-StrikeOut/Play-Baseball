/** @type {import('next').NextConfig} */
const nextConfig = {
  output: "standalone",
  images:{
    domains:['second-inning-bucket-1.s3.ap-northeast-2.amazonaws.com'],
  },
};

export default nextConfig;
