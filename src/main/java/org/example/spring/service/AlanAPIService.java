package org.example.spring.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AlanAPIService {

	@Value("${alan.host}")
	private String host;
	@Value("${alan.key}")
	private String client_id;
	private RestTemplate restTemplate;

	public AlanAPIService(RestTemplate restTemplate) {

		this.restTemplate = restTemplate;
	}

	public String getDataAsString(String content) {
		String url = UriComponentsBuilder.fromHttpUrl(host + "/api/v1/question")
			.queryParam("content", content)
			.queryParam("client_id", client_id)
			.toUriString();

		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

		if (response.getStatusCode().is2xxSuccessful()) {
			return extractContentFromResponse(response.getBody());
		} else {
			throw new RuntimeException("API 요청에 실패했습니다. 상태 코드:  " + response.getStatusCode());
		}
	}

	private String extractContentFromResponse(String responseBody) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode rootNode = objectMapper.readTree(responseBody);
			JsonNode contentNode = rootNode.path("content");  // "content" 필드 추출
			return contentNode.asText();  // String 값으로 반환
		} catch (Exception e) {
			throw new RuntimeException("JSON 파싱 중 오류가 발생했습니다.", e);
		}
	}
}
