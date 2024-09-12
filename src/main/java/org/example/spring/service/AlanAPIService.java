package org.example.spring.service;

import org.example.spring.repository.ExchangeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AlanAPIService {
	private final String REGULAR_PRICE_PROMPT =
		"질문: 의 출시 가격은 얼마인지 말해줄래? \n" + "답변 조건: \n" + "1. 오직 질문에 해당하는 숫자 데이터 하나만 제시할 것\n"
			+ "2. 숫자 외 그 어떤 문자도 포함 금지 (단위, 문구, 설명 일절 불가)\n" + "3. 모든 조건을 지키지 않을 시 답변으로 인정하지 않음\n"
			+ "4. 위 조건을 모두 준수하여 숫자로만 답해주세요";

	@Value("${alan.host}")
	private String host;
	@Value("${alan.key}")
	private String client_id;
	private RestTemplate restTemplate;
	private final ApplicationEventPublisher eventPublisher;
	private final ExchangeRepository exchangeRepository;

	public AlanAPIService(RestTemplate restTemplate, ApplicationEventPublisher eventPublisher,
		ExchangeRepository exchangeRepository) {

		this.restTemplate = restTemplate;
		this.eventPublisher = eventPublisher;
		this.exchangeRepository = exchangeRepository;
	}

	@Async
	@Transactional
	public void fetchRegularPriceAndUpdateExchange(String title, Long exchangeId) {
		try {
			String regularPriceStr = getDataAsString(title + REGULAR_PRICE_PROMPT);
			int regularPrice = Integer.parseInt(regularPriceStr);

			exchangeRepository.updateRegularPrice(exchangeId, regularPrice);

		} catch (RuntimeException e) {
			throw new RuntimeException("Failed to fetch regular price for exchangeId: " + exchangeId);
		}
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
