package org.example.spring.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * FileUploadService는 파일 업로드 및 삭제 기능을 제공하는 서비스 클래스입니다.
 * 이 서비스는 다중 파일 업로드와 개별 파일 삭제 기능을 지원합니다.
 */
@Service
public class FileUploadService {
    /**
     * 파일이 업로드될 디렉토리 경로입니다.
     * application.properties 또는 application.yml 파일에서 설정됩니다.
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 여러 파일을 업로드하고 각 파일의 URL을 반환합니다.
     *
     * @param files        업로드할 MultipartFile 객체들의 리스트
     * @return             업로드된 파일들의 URL 리스트
     * @throws IOException 파일 쓰기 중 오류가 발생한 경우
     */
    public List<String> uploadFiles(List<MultipartFile> files) throws IOException {
        List<String> fileUrls = new ArrayList<>();
        Path uploadPath = Paths.get(uploadDir);

        if (Files.notExists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile file : files) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir, fileName);
            Files.write(filePath, file.getBytes());
            fileUrls.add("/uploads/" + fileName);
        }
        return fileUrls;
    }

    /**
     * 지정된 URL의 파일을 삭제합니다.
     *
     * @param fileUrl      삭제할 파일의 URL
     * @throws IOException 파일을 찾을 수 없거나 삭제 중 오류가 발생한 경우
     */
    public void deleteFile(String fileUrl) throws IOException {
        Path filePath = Paths.get(uploadDir, fileUrl.replace("/uploads/", ""));

        if (Files.exists(filePath)) {
            Files.delete(filePath);
        } else {
            throw new IOException("File not found: " + filePath);
        }
    }
}
