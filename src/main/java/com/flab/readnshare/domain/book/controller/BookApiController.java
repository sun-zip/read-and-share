package com.flab.readnshare.domain.book.controller;

import com.flab.readnshare.domain.book.dto.SearchBookDetailResponseDto;
import com.flab.readnshare.domain.book.dto.SearchBookResponseDto;
import com.flab.readnshare.domain.book.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * BookApiController는 도서 검색 및 도서 상세 정보 조회 관련 HTTP 요청을 처리하는 컨트롤러
 * 클라이언트 요청에 따라 BookService를 호출하여 검색 결과를 응답
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/book")
@Validated
@Tag(name = "Book", description = "도서 검색 및 상세 정보 조회")
public class BookApiController {
    private final BookService bookService;

    /**
     * 도서 검색 API
     *
     * @param keyword 검색어
     * @param page 검색 시작 인덱스(페이지네이션)
     * @return 검색 결과(SearchBookReponseDto)를 포함하는 HTTP 응답 (200 OK)
     */
    @GetMapping("/search")
    @Operation(summary = "도서 검색", description = "도서 검색 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "서적 검색 성공."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(파라미터 오류)", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "올바르지 않은 입력값", value = "{ \"code\": \"INVALID_INPUT_PARAMETER\", \"message\": \"입력값을 확인하세요.\" }")
            })),
            @ApiResponse(responseCode = "401", description = "토큰이 없습니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "JWT_NULL", value = "{ \"code\": \"JWT_NULL\", \"message\": \"토큰이 없습니다.\" }")
                    })),
    })
    public ResponseEntity<SearchBookResponseDto> searchBook(
            @RequestParam @NotBlank String keyword,
            @RequestParam(required = false, defaultValue = "1") @Min(1) int page,
            @RequestParam(required = false,  defaultValue = "10") @Min(1) int display){

        return new ResponseEntity<>(bookService.searchBook(keyword, page, display), HttpStatus.OK);
    }

    /**
     * 도서 상세 정보 조회 API
     *
     * @param isbn 도서의 ISBN 번호
     * @return 도서 상세 정보(SearchBookDetailReponseDto)를 포함하는 HTTP 응답 (200 OK)
     */
    @GetMapping("/detail")
    @Operation(summary = "도서 상세 정보 조회", description = "도서 상세 정보 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "도서 상세 정보 조회 성공."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(파라미터 오류)", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "올바르지 않은 입력값", value = "{ \"code\": \"INVALID_INPUT_PARAMETER\", \"message\": \"입력값을 확인하세요.\" }")
            })),
            @ApiResponse(responseCode = "401", description = "토큰이 없습니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "JWT_NULL", value = "{ \"code\": \"JWT_NULL\", \"message\": \"토큰이 없습니다.\" }")
                    })),
    })
    public ResponseEntity<SearchBookDetailResponseDto> searchBookDetail(@RequestParam String isbn){
        return new ResponseEntity<>(bookService.searchBookDetail(isbn), HttpStatus.OK);
    }
}
