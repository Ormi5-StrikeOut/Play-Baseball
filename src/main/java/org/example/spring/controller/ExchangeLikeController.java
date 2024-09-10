package org.example.spring.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.spring.common.ApiResponseDto;
import org.example.spring.domain.like.dto.AddLikeRequest;
import org.example.spring.service.ExchangeLikeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 중고 거래 게시글의 좋아요 기능을 처리하는 컨트롤러 클래스입니다.
 * 이 컨트롤러는 Exchange 시스템에서 좋아요를 관리하는 엔드포인트를 제공합니다.
 */
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class ExchangeLikeController {
    private final ExchangeLikeService exchangeLikeService;

    /**
     * 중고 거래 게시글에 좋아요를 추가합니다.
     * 이 엔드포인트는 사용자가 중고 거래 게시글에 좋아요를 추가하거나 취소할 수 있습니다.
     *
     * @param request        요청 세부 정보가 포함된 HttpServletRequest 객체
     * @param addLikeRequest 좋아요 세부 정보가 포함된 요청 본문
     * @return               성공 메시지와 데이터가 없는 ApiResponseDto를 포함하는 ResponseEntity
     *                       성공적인 작업 시 HTTP 상태 201(CREATED)을 반환합니다.
     */
    @PostMapping
    public ResponseEntity<ApiResponseDto<Void>> addLike(HttpServletRequest request, @RequestBody AddLikeRequest addLikeRequest) {
        exchangeLikeService.addLike(request, addLikeRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success("좋아요 상태를 변경하였습니다.", null));
    }
}
