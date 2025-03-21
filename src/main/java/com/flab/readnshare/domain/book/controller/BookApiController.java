package com.flab.readnshare.domain.book.controller;

import com.flab.readnshare.domain.book.dto.SearchBookDetailResponseDto;
import com.flab.readnshare.domain.book.dto.SearchBookResponseDto;
import com.flab.readnshare.domain.book.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/book")
public class BookApiController {
    private final BookService bookService;

    /**
     * 도서 검색 API
     *
     * @param keyword 검색어
     * @param start 검색 시작 인덱스(페이지네이션)
     * @return 검색 결과(SearchBookReponseDto)를 포함하는 HTTP 응답 (200 OK)
     */
    @GetMapping("/search")
    public ResponseEntity<SearchBookResponseDto> searchBook(@RequestParam String keyword, @RequestParam int start){
        return new ResponseEntity<>(bookService.searchBook(keyword, start), HttpStatus.OK);
    }

    /**
     * 도서 상세 정보 조회 API
     *
     * @param isbn 도서의 ISBN 번호
     * @return 도서 상세 정보(SearchBookDetailReponseDto)를 포함하는 HTTP 응답 (200 OK)
     */
    @GetMapping("/detail")
    public ResponseEntity<SearchBookDetailResponseDto> searchBookDetail(@RequestParam String isbn){
        return new ResponseEntity<>(bookService.searchBookDetail(isbn), HttpStatus.OK);
    }
}
