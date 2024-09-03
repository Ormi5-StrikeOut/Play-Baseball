package org.example.spring.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Timestamp;
import java.util.Collections;
import org.example.spring.constants.Gender;
import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.member.Member;
import org.example.spring.domain.member.MemberRole;
import org.example.spring.domain.review.Review;
import org.example.spring.domain.review.dto.CreateReviewRequest;
import org.example.spring.domain.review.dto.GetMyReviewsResponse;
import org.example.spring.domain.review.dto.ModifyReviewRequest;
import org.example.spring.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class ReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    private Member testMember;
    private Exchange testExchange1;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
            .email("test@example.com")
            .password("password")
            .nickname("nickname")
            .name("name")
            .phoneNumber("010-1234-5678")
            .gender(Gender.MALE)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .role(MemberRole.USER)
            .build();

        testExchange1 = Exchange.builder()
            .writer(testMember)
            .title("테스트 중고 거래 게시물 1")
            .price(18000)
            .regularPrice(20000)
            .content("테스트")
            .viewCount(0)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();
    }

    @Test
    @DisplayName("존재하는 Exchange와 Member로 리뷰 작성 시 성공하는 테스트")
    void addReview() throws Exception {
        // Given
        CreateReviewRequest createReviewRequest = CreateReviewRequest.builder()
            .content("테스트")
            .rate(3)
            .isSecret(false)
            .build();

        Review savedReview = Review.builder()
            .exchange(testExchange1)
            .writer(testMember)
            .content(createReviewRequest.getContent())
            .rate(createReviewRequest.getRate())
            .isSecret(createReviewRequest.isSecret())
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();

        // When
        when(reviewService.addReview(any(Member.class), any(CreateReviewRequest.class))).thenReturn(savedReview);

        //  Then
        mockMvc.perform(post("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReviewRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("존재하는 Exchange에 작성한 리뷰 수정 시 성공하는 테스트")
    void modifyReview() throws Exception {
        // Given
        ModifyReviewRequest modifyReviewRequest = ModifyReviewRequest.builder()
                .content("리뷰 수정")
                .build();

        // When & Then
        mockMvc.perform(put("/api/reviews/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(modifyReviewRequest)))
                .andExpect(status().isOk());

        Mockito.verify(reviewService).modifyReview(
            any(Long.class),
            any(Member.class),
            any(ModifyReviewRequest.class)
        );
    }

    @Test
    @DisplayName("내가 작성한 리뷰 가져오기 성공하는 테스트")
    void getMyReviews() throws Exception {
        // Given
        Exchange exchange = testExchange1;

        Review myReview = Review.builder()
            .exchange(testExchange1)
            .writer(testMember)
            .content("테스트")
            .rate(5)
            .isSecret(false)
            .createdAt(new Timestamp(System.currentTimeMillis()))
            .build();

        Page<GetMyReviewsResponse> page = new PageImpl<>(
            Collections.singletonList(GetMyReviewsResponse.from(myReview)),
            PageRequest.of(0, 10),
            1
        );

        // When
        when(reviewService.getMyReviews(any(Member.class), any(Pageable.class))).thenReturn(page);

        // Then
        mockMvc.perform(get("/api/reviews/my")
                .param("page", "0")  // Pagination parameters
                .param("size", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(myReview.getId()))
            .andExpect(jsonPath("$.content[0].content").value("테스트"))
            .andExpect(jsonPath("$.content[0].rate").value(5))
            .andExpect(jsonPath("$.content[0].exchangeInfo.id").value(exchange.getId()))
            .andExpect(jsonPath("$.content[0].exchangeInfo.title").value(exchange.getTitle()));
    }
}