package com.flab.readnshare.domain.book.controller;

import com.flab.readnshare.domain.book.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookApiControllerTest {
    @Mock
    private BookService bookService;

    @InjectMocks
    private BookApiController bookApiController;

    private MockMvc mockMvc;

    @BeforeEach
    public void init(){
        mockMvc = MockMvcBuilders
                .standaloneSetup(bookApiController)
                .build();
    }

    @Nested
    @DisplayName("searchBook 테스트")
    class searchBookTest {

        @Test
        @DisplayName("책 검색 성공")
        void success() throws Exception {
            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get("/api/book/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("keyword", "스프링부트")
            );

            // then
            resultActions.andExpect(status().isOk());
        }

        @Test
        @DisplayName("책 검색 실패(음수 페이지 입력)")
        void fail_page() throws Exception {
            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get("/api/book/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("page", "-1")
            );

            // then
            resultActions.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("책 검색 실패(음수 디스플레이 입력)")
        void fail_display() throws Exception {
            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get("/api/book/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("display", "-1")
            );

            // then
            resultActions.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("searchBookDetailAPI 테스트")
    class searBookDetailAPITest {


        @Test
        @DisplayName("책 상세 검색 성공")
        void search_book_detail_success() throws Exception {
            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get("/api/book/detail")
                            .contentType(MediaType.APPLICATION_JSON)
                            .param("isbn", "9791169210027")
            );

            // then
            resultActions.andExpect(status().isOk());
        }

        @Test
        @DisplayName("책 상세 검색 실패(isbn 누락)")
        void search_book_detail_fail_isbn() throws Exception {
            // when
            ResultActions resultActions = mockMvc.perform(
                    MockMvcRequestBuilders.get("/api/book/search")
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            resultActions.andExpect(status().isBadRequest());
        }
    }
}