package org.example.spring.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

	private final AmazonS3 amazonS3;

	public S3Service(AmazonS3 amazonS3) {
		this.amazonS3 = amazonS3;
	}

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;
	@Value("${app.resource-url}")
	private String domain;

	/**
	 * MultipartFile로 받은 리소스를 S3 Bucket에 업로드할 때 사용합니다.
	 *
	 * @param file 요청받은 file 정보
	 * @return 업로드 완료된 url
	 * @throws IOException 업로드 관련 이슈가 생길 시 반환하는 Exception
	 */
	public String uploadFile(MultipartFile file) throws IOException {
		String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
		ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.getSize());
		metadata.setContentType(file.getContentType());

		amazonS3.putObject(new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata));

		// 업로드한 파일의 S3 URL 반환
		return domain + "/" + fileName;
	}

	/**
	 * S3 Bucket에 등록되어있는 이미지를 삭제합니다.
	 * @param fileName 이미지 경로
	 */
	public void deleteFile(String fileName) {
		amazonS3.deleteObject(bucket, fileName);
	}

}
