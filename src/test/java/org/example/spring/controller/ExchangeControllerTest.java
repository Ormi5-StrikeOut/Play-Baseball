package org.example.spring.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.spring.common.ApiResponseDto;
import org.example.spring.domain.exchange.dto.ExchangeModifyRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeRequestDto;
import org.example.spring.domain.exchange.dto.ExchangeResponseDto;
import org.example.spring.service.ExchangeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ExchangeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExchangeService exchangeService;

    @InjectMocks
    private ExchangeController exchangeController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(exchangeController).build();
    }

    @Test
    void testAddExchange() throws Exception {
        // Given
        ExchangeRequestDto requestDto = ExchangeRequestDto.builder()
                .memberId(1L)
                .title("Test Title")
                .price(1000)
                .regularPrice(1500)
                .content("Test Content")
                .build();

        ExchangeResponseDto responseDto = ExchangeResponseDto.builder()
                .id(1L)
                .memberId(1L)
                .title("Test Title")
                .price(1000)
                .regularPrice(1500)
                .content("Test Content")
                .viewCount(0)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();

        when(exchangeService.addExchange(any(ExchangeRequestDto.class))).thenReturn(responseDto);

        // When
        mockMvc.perform(post("/api/exchanges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Test Title"))
                .andExpect(jsonPath("$.data.id").value(1));
    }

    @Test
    void testModifyExchange() throws Exception {
        // Given
        ExchangeModifyRequestDto requestDto = ExchangeModifyRequestDto.builder()
                .title("Modified Title")
                .price(2000)
                .regularPrice(2500)
                .content("Modified Content")
                .build();

        ExchangeResponseDto responseDto = ExchangeResponseDto.builder()
                .id(1L)
                .memberId(1L)
                .title("Modified Title")
                .price(2000)
                .regularPrice(2500)
                .content("Modified Content")
                .viewCount(0)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        when(exchangeService.modifyExchange(eq(1L), any(ExchangeModifyRequestDto.class))).thenReturn(responseDto);

        // When
        mockMvc.perform(put("/api/exchanges/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Modified Title"))
                .andExpect(jsonPath("$.data.price").value(2000));
    }

    @Test
    void testDeleteExchange() throws Exception {
        // When
        mockMvc.perform(delete("/api/exchanges/1"))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시물이 삭제 되었습니다."))
                .andExpect(jsonPath("$.data").doesNotExist());
    }

    @Test
    void testGetAllExchanges() throws Exception {
        // Given
        List<ExchangeResponseDto> exchanges = Arrays.asList(
                ExchangeResponseDto.builder().id(1L).title("Title 1").build(),
                ExchangeResponseDto.builder().id(2L).title("Title 2").build()
        );

        when(exchangeService.getAllExchanges()).thenReturn(exchanges);

        // When
        mockMvc.perform(get("/api/exchanges"))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("모든 게시물 조회 성공."))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[1].id").value(2));
    }

    @Test
    void testGetLatestFiveExchanges() throws Exception {
        // Given
        List<ExchangeResponseDto> exchanges = Arrays.asList(
                ExchangeResponseDto.builder().id(1L).title("Title 1").build(),
                ExchangeResponseDto.builder().id(2L).title("Title 2").build(),
                ExchangeResponseDto.builder().id(3L).title("Title 3").build(),
                ExchangeResponseDto.builder().id(4L).title("Title 4").build(),
                ExchangeResponseDto.builder().id(5L).title("Title 5").build()
        );

        when(exchangeService.getLatestFiveExchanges()).thenReturn(exchanges);

        // When
        mockMvc.perform(get("/api/exchanges/five"))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("게시물 5개 조회 성공."))
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[4].id").value(5));
    }

    @Test
    void testModifySalesStatus() throws Exception {
        // Given
        ExchangeResponseDto responseDto = ExchangeResponseDto.builder()
                .id(1L)
                .memberId(1L)
                .title("Test Title")
                .price(1000)
                .regularPrice(1500)
                .content("Test Content")
                .viewCount(0)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .updatedAt(new Timestamp(System.currentTimeMillis()))
                .build();

        when(exchangeService.modifySalesStatus(1L)).thenReturn(responseDto);

        // When
        mockMvc.perform(put("/api/exchanges/1/status"))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    void testGetMyExchanges() throws Exception {
        // Given
        List<ExchangeResponseDto> exchanges = Arrays.asList(
                ExchangeResponseDto.builder().id(1L).title("Title 1").memberId(1L).build(),
                ExchangeResponseDto.builder().id(2L).title("Title 2").memberId(1L).build()
        );

        when(exchangeService.getMyExchanges(1L)).thenReturn(exchanges);

        // When
        mockMvc.perform(get("/api/exchanges/1"))
                // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("회원의 게시물 조회 성공"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[1].id").value(2));
    }
}