package com.flab.readnshare.domain.book.service;

import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.book.dto.BookDto;
import com.flab.readnshare.domain.book.dto.SearchBookDetailResponseDto;
import com.flab.readnshare.domain.book.dto.SearchBookResponseDto;
import com.flab.readnshare.domain.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * BookService는 도서 검색, 상세 검색, 그리고 도서 등록 등 Book 도메인 관련 비즈니스 로직을 처리함
 * 네이버 오픈 API를 활용하여 도서 정보를 검색
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookService {
    @Value("${naver.book.id}")
    private String clientId;

    @Value("${naver.book.secret}")
    private String clientSecret;

    private static final String SEARCH_BOOK_URL = "https://openapi.naver.com/v1/search/book.json?display=10";
    private static final String SEARCH_BOOK_DETAIL_URL = "https://openapi.naver.com/v1/search/book_adv.json";

    private final RestTemplate restTemplate;
    private final BookRepository bookRepository;

    /**
     * 도서 검색을 수행
     *
     * @param keyword 검색어
     * @param page    검색 시작 인덱스(페이지네이션)
     * @return SearchBookReponseDto 형태의 도서 검색 결과
     */
    public SearchBookResponseDto searchBook(String keyword, int page, int display) {
        int start = (page - 1) * display + 1;

        URI targetUrl = UriComponentsBuilder
                .fromUriString(SEARCH_BOOK_URL)
                .queryParam("query", keyword)
                .queryParam("start", start)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        SearchBookResponseDto response = restTemplate.exchange(targetUrl, HttpMethod.GET, getHttpEntity(), SearchBookResponseDto.class).getBody();

        int totalItems = response.getTotal() != null ? response.getTotal() : 0;
        //int totalPages = (totalItems / display) + (totalItems % display == 0 ? 0 : 1);
        int totalPages = (int) Math.ceil((double) totalItems / display);
        response.setDisplay(display);
        response.setCurrentPage(page);
        response.setTotalPage(totalPages);
        return response;
    }

    /**
     * 도서 상세 검색을 수행
     *
     * @param isbn 도서의 ISBN 번호
     * @return SearchBookDetailReponseDto 형태의 도서 상세 검색 결과
     */
    public SearchBookDetailResponseDto searchBookDetail(String isbn) {
        URI targetUrl = UriComponentsBuilder
                .fromUriString(SEARCH_BOOK_DETAIL_URL)
                .queryParam("d_isbn", isbn)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUri();

        return restTemplate.exchange(targetUrl, HttpMethod.GET, getHttpEntity(), SearchBookDetailResponseDto.class).getBody();
    }

    /**
     * 네이버 도서 API 호출에 필요한 HTTP 엔티티를 생성
     * 헤더에 클라이언트 ID와 시크릿을 추가
     *
     * @return HttpEntity 객체
     */
    private HttpEntity<String> getHttpEntity() {
        // 헤더 인증 정보 추가
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("X-Naver-Client-Id", clientId);
        httpHeaders.set("X-Naver-Client-Secret", clientSecret);
        return new HttpEntity<>(httpHeaders);
    }

    /**
     * 도서 등록
     * 만약 동일한 ISBN의 도서가 이미 존재하면 기존 도서를 반환하고,
     * 없으면 새로운 도서를 저장
     *
     * @param dto 도서 정보를 담은 BookDto
     * @return 등록(또는 조회)된 Book 엔티티
     */
    public Book save(BookDto dto) {
        Book book = bookRepository.findByIsbn(dto.getIsbn())
                .orElseGet(() -> bookRepository.save(dto.toEntity()));
        return book;
    }
}