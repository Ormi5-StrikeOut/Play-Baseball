package org.example.spring.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.exchangeImage.ExchangeImage;
import org.example.spring.repository.ExchangeImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ExchangeImageService {
	private final ExchangeImageRepository exchangeImageRepository;
	private static final String UPLOAD_DIR = "public/resources/exchange_image/";

	@Autowired
	public ExchangeImageService(ExchangeImageRepository exchangeImageRepository) {
		this.exchangeImageRepository = exchangeImageRepository;
	}

	/**
	 * Param으로 들어온 이미지를 서버에 업로드 후 table에 저장합니다.
	 *
	 * @param requestImage 업로드 요청 대상 이미지 파일
	 * @param exchange 이미지가 속해있는 게시글
	 * @return table에 저장된 image
	 */
	public ExchangeImage uploadImage(MultipartFile requestImage, Exchange exchange) {
		if (requestImage.isEmpty()) {
			throw new IllegalArgumentException("파일이 존재하지 않습니다.");
		}

		try {
			Path uploadPath = Paths.get(UPLOAD_DIR);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			String fileName = UUID.randomUUID().toString() + "_" + requestImage.getOriginalFilename();
			Path path = Paths.get(UPLOAD_DIR + fileName);
			Files.write(path, requestImage.getBytes());

			String fileUrl = "/exchange_image/" + fileName;
			ExchangeImage image = ExchangeImage.builder().url(fileUrl).exchange(exchange).build();

			exchangeImageRepository.save(image);

			return image;
		} catch (IOException e) {
			throw new RuntimeException("이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
		}
	}
}
