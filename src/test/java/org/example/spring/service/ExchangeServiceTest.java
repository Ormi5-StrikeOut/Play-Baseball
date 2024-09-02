package org.example.spring.service;

import org.example.spring.domain.exchange.Exchange;
import org.example.spring.domain.exchange.dto.ExchangeModifyRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeResponseDto;
import org.example.spring.domain.member.Member;
import org.example.spring.repository.ExchangeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExchangeServiceTest {

    @Mock
    private ExchangeRepository exchangeRepository;

    @InjectMocks
    private ExchangeService exchangeService;

    private Member testMember;
    private Exchange testExchange;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testMember = createTestMember(1L, "User");
    }

    @Test
    @DisplayName("새로운 게시글 생성")
    void addExchange() {
        // Given
        ExchangeRequestDto requestDto = createExchangeRequestDto(1L, "New Exchange Title", 1000, 1200, "This is a new exchange content");

        Exchange savedExchange = createTestExchange(1L, testMember, "New Exchange Title", 1000, 1200, "This is a new exchange content");

        when(exchangeRepository.save(any(Exchange.class))).thenReturn(savedExchange);

        // When
        ExchangeResponseDto responseDto = exchangeService.addExchange(requestDto);

        // Then
        assertThat(responseDto.getId()).isEqualTo(1L);
        assertThat(responseDto.getTitle()).isEqualTo("New Exchange Title");
        assertThat(responseDto.getPrice()).isEqualTo(1000);
        assertThat(responseDto.getRegularPrice()).isEqualTo(1200);
        assertThat(responseDto.getContent()).isEqualTo("This is a new exchange content");
        assertThat(responseDto.getMemberId()).isEqualTo(1L);

        verify(exchangeRepository, times(1)).save(any(Exchange.class));
    }

    @Test
    @DisplayName("게시글 수정")
    void modifyExchange() {
        // Given
        Long exchangeId = 1L;
        Exchange existingExchange = createTestExchange(exchangeId, testMember, "Old Title", 1000, 1200, "Old Content");

        ExchangeModifyRequestDto requestDto = ExchangeModifyRequestDto.builder()
                .title("New Title")
                .price(1500)
                .regularPrice(1800)
                .content("New Content")
                .build();

        when(exchangeRepository.findById(exchangeId)).thenReturn(Optional.of(existingExchange));
        when(exchangeRepository.save(any(Exchange.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        ExchangeResponseDto responseDto = exchangeService.modifyExchange(exchangeId, requestDto);

        // Then
        assertThat(responseDto.getTitle()).isEqualTo("New Title");
        assertThat(responseDto.getPrice()).isEqualTo(1500);
        assertThat(responseDto.getRegularPrice()).isEqualTo(1800);
        assertThat(responseDto.getContent()).isEqualTo("New Content");

        verify(exchangeRepository, times(1)).findById(exchangeId);
        verify(exchangeRepository, times(1)).save(any(Exchange.class));
    }

    @Test
    @DisplayName("게시글 삭제")
    void deleteExchange() {
        // Given
        Long exchangeId = 1L;
        Exchange existingExchange = createTestExchange(exchangeId, testMember, "Existing Title", 1000, 1200, "Existing Content");

        when(exchangeRepository.findById(exchangeId)).thenReturn(Optional.of(existingExchange));
        when(exchangeRepository.save(any(Exchange.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        exchangeService.deleteExchange(exchangeId);

        // Then
        verify(exchangeRepository, times(1)).findById(exchangeId);
        verify(exchangeRepository, times(1)).save(any(Exchange.class));
        verifyNoMoreInteractions(exchangeRepository);
    }

    @Test
    @DisplayName("모든 게시글 조회")
    void getAllExchanges() {
        // Given
        Exchange exchange1 = createTestExchange(1L, testMember, "Exchange Title 1", 1000, 1200, "Exchange Content 1");
        Exchange exchange2 = createTestExchange(2L, createTestMember(2L, "Another User"), "Exchange Title 2", 2000, 2200, "Exchange Content 2");

        when(exchangeRepository.findAll()).thenReturn(Arrays.asList(exchange1, exchange2));

        // When
        List<ExchangeResponseDto> responseDtoList = exchangeService.getAllExchanges();

        // Then
        assertThat(responseDtoList).hasSize(2);

        assertThat(responseDtoList.get(0).getTitle()).isEqualTo("Exchange Title 1");
        assertThat(responseDtoList.get(1).getTitle()).isEqualTo("Exchange Title 2");

        verify(exchangeRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("최근 5개 게시글 조회")
    void getLatestFiveExchanges() {
        // Given
        List<Exchange> latestExchanges = Arrays.asList(
                createTestExchange(1L, testMember, "Exchange Title 1", 1000, 1200, "Exchange Content 1"),
                createTestExchange(2L, createTestMember(2L, "User 2"), "Exchange Title 2", 2000, 2200, "Exchange Content 2"),
                createTestExchange(3L, createTestMember(3L, "User 3"), "Exchange Title 3", 3000, 3200, "Exchange Content 3"),
                createTestExchange(4L, createTestMember(4L, "User 4"), "Exchange Title 4", 4000, 4200, "Exchange Content 4"),
                createTestExchange(5L, createTestMember(5L, "User 5"), "Exchange Title 5", 5000, 5200, "Exchange Content 5")
        );

        when(exchangeRepository.findTop5ByOrderByCreatedAtDesc()).thenReturn(latestExchanges);

        // When
        List<ExchangeResponseDto> responseDtoList = exchangeService.getLatestFiveExchanges();

        // Then
        assertThat(responseDtoList).hasSize(5);
        assertThat(responseDtoList.get(0).getTitle()).isEqualTo("Exchange Title 1");
        assertThat(responseDtoList.get(4).getTitle()).isEqualTo("Exchange Title 5");

        verify(exchangeRepository, times(1)).findTop5ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("특정 회원이 작성한 게시글 조회")
    void getMyExchanges() {
        // Given
        Long memberId = 1L;
        List<Exchange> myExchanges = Arrays.asList(
                createTestExchange(1L, testMember, "Exchange Title 1", 1000, 1200, "Exchange Content 1"),
                createTestExchange(2L, testMember, "Exchange Title 2", 2000, 2200, "Exchange Content 2")
        );

        when(exchangeRepository.findByWriterId(memberId)).thenReturn(myExchanges);

        // When
        List<ExchangeResponseDto> responseDtoList = exchangeService.getMyExchanges(memberId);

        // Then
        assertThat(responseDtoList).hasSize(2);
        assertThat(responseDtoList.get(0).getTitle()).isEqualTo("Exchange Title 1");
        assertThat(responseDtoList.get(1).getTitle()).isEqualTo("Exchange Title 2");

        verify(exchangeRepository, times(1)).findByWriterId(memberId);
    }

    private Member createTestMember(Long id, String name) {
        return Member.builder()
                .id(id)
                .name(name)
                .build();
    }

    private Exchange createTestExchange(Long id, Member writer, String title, int price, int regularPrice, String content) {
        return Exchange.builder()
                .id(id)
                .writer(writer)
                .title(title)
                .price(price)
                .regularPrice(regularPrice)
                .content(content)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    private ExchangeRequestDto createExchangeRequestDto(Long memberId, String title, int price, int regularPrice, String content) {
        return ExchangeRequestDto.builder()
                .memberId(memberId)
                .title(title)
                .price(price)
                .regularPrice(regularPrice)
                .content(content)
                .build();
    }
}
