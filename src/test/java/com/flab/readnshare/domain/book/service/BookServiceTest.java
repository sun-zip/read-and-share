package com.flab.readnshare.domain.book.service;

import com.flab.readnshare.domain.book.dto.SearchBookDetailResponseDto;
import com.flab.readnshare.domain.book.dto.SearchBookResponseDto;
import com.flab.readnshare.domain.book.repository.BookRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private BookRepository bookRepository;
    @InjectMocks
    private BookService bookService;

    @Nested
    @DisplayName("searchBook 테스트")
    class searchBookTest {
        @Test
        @DisplayName("책 검색 성공")
        void success() {
            // given
            String keyword = "스프링부트";
            int page = 2;
            int display = 10;
            int start = (page - 1) * display + 1;
            SearchBookResponseDto mockResponse = SearchBookResponseDto.builder()
                    .total(56)
                    .display(display)
                    .items(new ArrayList<>())
                    .build();

            ResponseEntity<SearchBookResponseDto> responseEntity = ResponseEntity.ok(mockResponse);

            // when
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(SearchBookResponseDto.class)))
                    .thenReturn(responseEntity);

            SearchBookResponseDto result = bookService.searchBook(keyword, page, display);

            // then
            Assertions.assertEquals(6, result.getTotalPage());
            Assertions.assertEquals(2, result.getCurrentPage());
            Assertions.assertEquals(display, result.getDisplay());

            verify(restTemplate).exchange(Mockito.argThat(uri -> {
                String query = uri.getQuery();
                return query.contains("start=" + start);
            }), eq(HttpMethod.GET), any(HttpEntity.class), eq(SearchBookResponseDto.class));
        }
    }

    @Nested
    @DisplayName("searchBookDetail 테스트")
    class searchBookDetailTest {

        @Test
        @DisplayName("책 상세 검색 성공")
        void searchBookDetail_success() {
            // given
            String isbn = "9781234567890";
            SearchBookDetailResponseDto mockResponse = new SearchBookDetailResponseDto();
            ResponseEntity<SearchBookDetailResponseDto> responseEntity = ResponseEntity.ok(mockResponse);
            // when
            when(restTemplate.exchange(any(URI.class), eq(HttpMethod.GET), any(HttpEntity.class), eq(SearchBookDetailResponseDto.class)))
                    .thenReturn(responseEntity);
            SearchBookDetailResponseDto result = bookService.searchBookDetail(isbn);

            //then
            Assertions.assertNotNull(result);
        }
    }
}
