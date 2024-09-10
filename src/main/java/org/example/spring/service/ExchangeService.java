package org.example.spring.service;

import java.nio.file.AccessDeniedException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.example.spring.constants.SalesStatus;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.exchange.dto.ExchangeAddRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeDetailResponseDto;
import org.example.spring.domain.exchange.dto.ExchangeModifyRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeNavigationResponseDto;
import org.example.spring.domain.exchange.dto.ExchangeResponseDto;
import org.example.spring.domain.exchangeImage.ExchangeImage;
import org.example.spring.domain.likeOverview.LikeOverview;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.reviewOverview.ReviewOverview;
import org.example.spring.exception.AuthenticationFailedException;
import org.example.spring.repository.ExchangeImageRepository;
import org.example.spring.repository.ExchangeRepository;
import org.example.spring.repository.LikeOverviewRepository;
import org.example.spring.repository.ReviewOverviewRepository;
import org.example.spring.security.jwt.JwtTokenValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ExchangeService {

	@Value("${app.fe-url}")
	private String frontendBaseUrl;
	private final String EXCHANGE = "/exchange";
	private final String REGULAR_PRICE_PROMPT =
		"질문: 의 출시 가격은 얼마인지 말해줄래? \n" + "답변 조건: \n" + "1. 오직 질문에 해당하는 숫자 데이터 하나만 제시할 것\n"
			+ "2. 숫자 외 그 어떤 문자도 포함 금지 (단위, 문구, 설명 일절 불가)\n" + "3. 모든 조건을 지키지 않을 시 답변으로 인정하지 않음\n"
			+ "4. 위 조건을 모두 준수하여 숫자로만 답해주세요";

	private final ExchangeRepository exchangeRepository;
	private final ExchangeImageRepository exchangeImageRepository;
	private final ReviewOverviewRepository reviewOverviewRepository;
	private final LikeOverviewRepository likeOverviewRepository;
	private final S3Service s3Service;
	private final AlanAPIService alanAPIService;
	private final JwtTokenValidator jwtTokenValidator;

	@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
	public ExchangeService(ExchangeRepository exchangeRepository, ExchangeImageRepository exchangeImageRepository,
		JwtTokenValidator jwtTokenValidator, S3Service s3Service, ReviewOverviewRepository reviewOverviewRepository,
		LikeOverviewRepository likeOverviewRepository, AlanAPIService alanAPIService) {
		this.exchangeRepository = exchangeRepository;
		this.exchangeImageRepository = exchangeImageRepository;
		this.reviewOverviewRepository = reviewOverviewRepository;
		this.likeOverviewRepository = likeOverviewRepository;
		this.s3Service = s3Service;
		this.jwtTokenValidator = jwtTokenValidator;
		this.alanAPIService = alanAPIService;
	}

	/**
	 * 게시물을 추가합니다.
	 *
	 * @param request 요청이 들어온 http 정보로 요청한 자의 정보와 권한을 검사하기 위해 사용. 비회원은 게시물을 발행할 수 없습니다.
	 * @param exchangeAddRequestDto 작성할 글 정보
	 * @param images 작성할 글에 해당하는 이미지 정보
	 * @return 작성이 정상적으로 진행될 경우 글의 정보를 반환합니다.
	 */
	@Transactional
	public ExchangeResponseDto addExchange(HttpServletRequest request, ExchangeAddRequestDto exchangeAddRequestDto,
		List<MultipartFile> images) {

		// token 유효성 검사 후 요청한 member 정보
		Member member = jwtTokenValidator.validateTokenAndGetMember(jwtTokenValidator.extractTokenFromHeader(request));

		int regularPrice = Integer.parseInt(
			alanAPIService.getDataAsString(exchangeAddRequestDto.getTitle() + REGULAR_PRICE_PROMPT));

		Exchange exchange = Exchange.builder()
			.member(member)
			.title(exchangeAddRequestDto.getTitle())
			.price(exchangeAddRequestDto.getPrice())
			.regularPrice(regularPrice)
			.content(exchangeAddRequestDto.getContent())
			.status(SalesStatus.SALE)
			.build();

		exchangeRepository.save(exchange);

		if (images != null) {
			for (MultipartFile image : images) {
				try {
					String fileUrl = s3Service.uploadFile(image);
					ExchangeImage exchangeImage = ExchangeImage.builder().url(fileUrl).exchange(exchange).build();

					exchange.addImage(exchangeImage);
					exchangeImageRepository.save(exchangeImage);
				} catch (Exception e) {
					log.error("이미지 업로드 중 오류 발생: {}", e.getMessage());
					throw new RuntimeException("Image upload failed");
				}
			}
		}

		exchangeRepository.save(exchange);

		return ExchangeResponseDto.fromExchange(exchange);
	}

	/**
	 * 모든 게시글 중 삭제하지 않은 글 목록을 조회합니다.
	 *
	 * @param page 게시물이 포함된 페이지
	 * @param size 한 번에 렌더링할 게시물 개수
	 * @return 게시글 목록을 page와 size에 따라 반환
	 */
	@Transactional(readOnly = true)
	public Page<ExchangeNavigationResponseDto> getAllExchanges(SalesStatus status, int page, int size) {

		Pageable pageable = PageRequest.of(page, size);
		Page<Exchange> exchanges;

		if (status == SalesStatus.SALE || status == SalesStatus.COMPLETE) {
			exchanges = exchangeRepository.findByDeletedAtIsNullAndStatusOrderByCreatedAtDesc(status, pageable);
		} else {
			exchanges = exchangeRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable);
		}

		return exchanges.map(
			exchange -> ExchangeNavigationResponseDto.fromExchange(exchange, frontendBaseUrl + EXCHANGE));
	}

	/**
	 * 가장 최근에 올라온 글 중 삭제하지 않은 글 5개를 조회합니다.
	 *
	 * @return 삭제 처리하지 않은 최근 개시물 5개를 반환
	 */
	@Transactional(readOnly = true)
	public List<ExchangeNavigationResponseDto> getLatestFiveExchanges() {
		return exchangeRepository.findTop5ByDeletedAtIsNullOrderByCreatedAtDesc()
			.stream()
			.map(exchange -> ExchangeNavigationResponseDto.fromExchange(exchange, frontendBaseUrl + EXCHANGE))
			.collect(Collectors.toList());
	}

	/**
	 * 특정 회원이 작성한 게시글 중 삭제하지 않은 글 목록을 조회합니다.
	 *
	 * @param memberId 대상 회원 id
	 * @param page 게시물이 포함된 페이지
	 * @param size 한 번에 렌더링할 게시물 개수
	 * @return 게시글 목록을 page와 size에 따라 반환
	 */
	@Transactional(readOnly = true)
	public Page<ExchangeNavigationResponseDto> getUserExchanges(Long memberId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return exchangeRepository.findByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(memberId, pageable)
			.map(exchange -> ExchangeNavigationResponseDto.fromExchange(exchange, frontendBaseUrl + EXCHANGE));
	}

	/**
	 * 제목을 포함하고 있는 삭제되지 않은 게시물 모두 조회합니다.
	 *
	 * @param keyword 검색에 포함할 제목 키워드
	 * @param page 게시물이 포함된 페이지
	 * @param size 한 번에 렌더링할 게시물 개수
	 * @return 제목이 키워드에 포함되어있는 게시물 목록을 page와 size에 따라 반환
	 */
	@Transactional
	public Page<ExchangeNavigationResponseDto> getExchangesByTitleContaining(String keyword, SalesStatus status,
		int page,
		int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<Exchange> exchanges;

		if (keyword.equals("")) {
			if (status == SalesStatus.SALE || status == SalesStatus.COMPLETE) {
				exchanges = exchangeRepository.findByDeletedAtIsNullAndStatusOrderByCreatedAtDesc(status, pageable);
			} else {
				exchanges = exchangeRepository.findByDeletedAtIsNullOrderByCreatedAtDesc(pageable);
			}
		} else {
			if (status == SalesStatus.SALE || status == SalesStatus.COMPLETE) {
				exchanges = exchangeRepository.findByTitleContainingAndDeletedAtIsNullAndStatusOrderByCreatedAtDesc(
					keyword,
					status, pageable);
			} else {
				exchanges = exchangeRepository.findByTitleContainingAndDeletedAtIsNullOrderByCreatedAtDesc(keyword,
					pageable);
			}
		}

		return exchanges.map(
			exchange -> ExchangeNavigationResponseDto.fromExchange(exchange, frontendBaseUrl + EXCHANGE));
	}

	/**
	 * id에 해당하는 게시물 1개를 상세 조회합니다.
	 *
	 * @param request 요청이 들어온 http 정보로 요청한 자가 게시글을 작성한 본인인지 판단 여부를 위해 사용
	 * @param id 게시물 id
	 * @return 게시물이 있고 삭제처리가 되어있지 않은 경우 게시물 정보와 작성자 여부를 반환합니다.
	 */
	@Transactional
	public ExchangeDetailResponseDto getExchangeDetail(HttpServletRequest request, Long id) {

		Exchange exchange = exchangeRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new EntityNotFoundException("작성된 글이 아니거나 삭제되었습니다."));

		Pageable pageable = PageRequest.of(0, 3);
		List<ExchangeNavigationResponseDto> recentExchangesByMember = exchangeRepository.findByMemberIdAndDeletedAtIsNullOrderByCreatedAtDesc(
				exchange.getMember().getId(), pageable)
			.map(exchangeItem -> ExchangeNavigationResponseDto.fromExchange(exchangeItem, frontendBaseUrl + EXCHANGE))
			.getContent();

		boolean isWriter = isWriter(request, id);

		ReviewOverview reviewOverview = reviewOverviewRepository.findByMemberId(exchange.getMember().getId())
			.orElse(ReviewOverview.builder().count(0).average(0.0).build());

		long reviewCount = reviewOverview.getCount();
		double average = reviewOverview.getAverage();

		LikeOverview likeOverview = likeOverviewRepository.findByExchangeId(id)
			.orElse(LikeOverview.builder().count(0).build());

		long likeCount = likeOverview.getCount();

		exchangeRepository.incrementViewCount(id);

		return ExchangeDetailResponseDto.fromExchange(exchange, recentExchangesByMember, isWriter, reviewCount,
			average, likeCount);
	}

	/**
	 * 게시물을 수정합니다. 삭제처리된 글은 수정할 수 없습니다.
	 *
	 * @param request 요청이 들어온 http 정보로 요청한 자의 정보와 권한을 검사하기 위해 사용. 작성자가 아니라면 수정이 불가능합니다.
	 * @param id 글 id
	 * @param exchangeModifyRequestDto 수정할 글의 정보
	 * @param images 글에 첨부할 이미지
	 * @return 수정이 정상적으로 진행될 경우 수정된 글의 정보를 반환합니다.
	 */
	@Transactional
	public ExchangeResponseDto modifyExchange(HttpServletRequest request, Long id,
		ExchangeModifyRequestDto exchangeModifyRequestDto, List<MultipartFile> images) {

		// token 유효성 검사 후 요청한 member 정보
		Member member = jwtTokenValidator.validateTokenAndGetMember(jwtTokenValidator.extractTokenFromHeader(request));

		Exchange exchange = exchangeRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new RuntimeException("게시글을 찾을수 없습니다."));

		try {
			if (!member.equals(exchange.getMember())) {
				throw new AccessDeniedException("Warning: Another user attempted to modify the post.");
			}

			List<ExchangeImage> imagesCopy = new ArrayList<>(exchange.getImages());
			for (ExchangeImage image : imagesCopy) {
				exchange.removeImage(image);
			}

			int regularPrice = exchange.getRegularPrice();
			if (!exchangeModifyRequestDto.getTitle().equals(exchange.getTitle())) {
				regularPrice = Integer.parseInt(
					alanAPIService.getDataAsString(exchangeModifyRequestDto.getTitle() + REGULAR_PRICE_PROMPT));
			}

			Exchange updateExchange = exchange.toBuilder()
				.title(exchangeModifyRequestDto.getTitle())
				.price(exchangeModifyRequestDto.getPrice())
				.regularPrice(regularPrice)
				.content(exchangeModifyRequestDto.getContent())
				.updatedAt(new Timestamp(System.currentTimeMillis()))
				.status(exchangeModifyRequestDto.getStatus())
				.build();

			if (images != null) {
				for (MultipartFile image : images) {
					try {
						String fileUrl = s3Service.uploadFile(image);
						ExchangeImage exchangeImage = ExchangeImage.builder().url(fileUrl).exchange(exchange).build();

						exchange.addImage(exchangeImage);
						exchangeImageRepository.save(exchangeImage);
					} catch (Exception e) {
						log.error("이미지 업로드 중 오류 발생: {}", e.getMessage());
						throw new RuntimeException("Image upload failed");
					}
				}
			}

			return ExchangeResponseDto.fromExchange(exchangeRepository.save(updateExchange));
		} catch (AccessDeniedException e) {
			log.error("{} [{} -> {}]", e.getMessage(), member.getEmail(), exchange.getTitle());
			throw new AuthenticationFailedException(
				"Warning: Access denied. You do not have permission to modify the post.");
		}
	}

	/**
	 * 게시물을 삭제합니다. 이미 삭제 처리된 글은 처리하지 않습니다.
	 *
	 * @param request 요청이 들어온 http 정보로 요청한 자의 정보와 권한을 검사하기 위해 사용. 작성자가 아니라면 삭제가 불가능합니다.
	 * @param id 삭제 게시물 대상 id
	 */
	@Transactional
	public void deleteExchange(HttpServletRequest request, Long id) {

		// token 유효성 검사 후 요청한 member 정보
		Member member = jwtTokenValidator.validateTokenAndGetMember(jwtTokenValidator.extractTokenFromHeader(request));

		Exchange exchange = exchangeRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new RuntimeException("게시글을 찾을수 없습니다."));

		try {
			if (!member.equals(exchange.getMember())) {
				throw new AccessDeniedException("Warning: Another user has attempted to delete the post.");
			}

			Exchange deletedExchange = exchange.toBuilder()
				.deletedAt(new Timestamp(System.currentTimeMillis()))
				.build();
			exchangeRepository.save(deletedExchange);

		} catch (AccessDeniedException e) {
			log.error("{} [{} -> {}]", e.getMessage(), member.getEmail(), exchange.getTitle());
			throw new AuthenticationFailedException(
				"Warning: Access denied. You do not have permission to delete the post.");
		}
	}

	/**
	 * 게시물 상세 조회 영역에서 작성자가 본인 글을 조회했는지 판단합니다.
	 *
	 * @param request 요청한 자의 정보
	 * @param id 대상 글 id
	 * @return 본인이 작성한 글로 판단될 경우 true를 반환합니다.
	 */
	private boolean isWriter(HttpServletRequest request, Long id) {
		Member member = jwtTokenValidator.validateTokenAndGetMember(jwtTokenValidator.extractTokenFromHeader(request));

		Exchange exchange = exchangeRepository.findByIdAndDeletedAtIsNull(id)
			.orElseThrow(() -> new RuntimeException("게시글을 찾을수 없습니다."));

		return member.equals(exchange.getMember());
	}
}
