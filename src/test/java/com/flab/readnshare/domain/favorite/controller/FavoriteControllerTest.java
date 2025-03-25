package com.flab.readnshare.domain.favorite.controller;

import com.flab.readnshare.domain.book.dto.BookDto;
import com.flab.readnshare.domain.favorite.service.FavoriteBookService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.global.common.advice.ApiExceptionAdvice;
import com.flab.readnshare.global.common.exception.BookException;
import com.flab.readnshare.global.common.exception.FavoriteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FavoriteControllerTest {

    static final String FAVORITES_API_ENDPOINT = "/api/v1/favorites";

    @Mock
    FavoriteBookService favoriteBookService;

    @InjectMocks
    FavoriteController favoriteController;

    ApiExceptionAdvice apiExceptionAdvice = new ApiExceptionAdvice();

    MockMvc mockMvc;

    @BeforeEach
    void init() {
        // @SignInMember 어노테이션을 처리할 커스텀 인자 해결기 등록
        HandlerMethodArgumentResolver signInMemberResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(com.flab.readnshare.global.common.resolver.SignInMember.class);
            }
            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                return Member.builder().id(1L).build();
            }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(favoriteController)
                .setCustomArgumentResolvers(signInMemberResolver)
                .setControllerAdvice(apiExceptionAdvice)
                .build();
    }

    @Nested
    @DisplayName("addFavorite 테스트")
    class AddFavoriteTest {
        @Test
        @DisplayName("성공 - 즐겨찾기 추가")
        void success() throws Exception {
            String isbn = "1234567890123";

            // 서비스에서는 예외 없이 정상적으로 처리한다고 가정
            ResultActions resultActions = mockMvc.perform(
                    post(FAVORITES_API_ENDPOINT)
                            .param("isbn", isbn)
            );

            resultActions
                    .andDo(print())
                    .andExpect(status().isCreated());
            verify(favoriteBookService).addFavorite(any(Member.class), eq(isbn));
        }

        @Test
        @DisplayName("실패 - 책을 찾을 수 없음")
        void fail_addFavorite_bookNotFound() throws Exception{
            String isbn = "1234567890123";
            // 예: 책을 찾을 수 없는 경우 BookNotFound 예외 발생
            willThrow(new BookException.BookNotFound())
                    .given(favoriteBookService).addFavorite(any(Member.class), eq(isbn));

            ResultActions resultActions = mockMvc.perform(
                    post(FAVORITES_API_ENDPOINT)
                            .param("isbn", isbn)
            );

            resultActions
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    // 예외 처리 Advice에서 전달하는 JSON 형식의 메시지 검증 (메시지는 상황에 따라 변경)
                    .andExpect(jsonPath("$.message").value("존재하지 않는 책입니다."));
        }

        @Test
        @DisplayName("실패 - 즐겨찾기 추가 중 예외 발생")
        void fail_addFavorite() throws Exception {
            String isbn = "1234567890123";
            // 예: 이미 즐겨찾기에 등록된 경우 FavoriteAlreadyExist 예외 발생
            willThrow(new FavoriteException.FavoriteAlreadyExist())
                    .given(favoriteBookService).addFavorite(any(Member.class), eq(isbn));

            ResultActions resultActions = mockMvc.perform(
                    post(FAVORITES_API_ENDPOINT)
                            .param("isbn", isbn)
            );

            resultActions
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    // 예외 처리 Advice에서 전달하는 JSON 형식의 메시지 검증 (메시지는 상황에 따라 변경)
                    .andExpect(jsonPath("$.message").value("이미 즐겨찾기에 등록되어 있는 책입니다."));
        }
    }

    @Nested
    @DisplayName("deleteFavorite 테스트")
    class DeleteFavoriteTest {
        @Test
        @DisplayName("성공 - 즐겨찾기 삭제")
        void success() throws Exception {
            String isbn = "12345";
            willDoNothing().given(favoriteBookService).deleteFavorite(any(Member.class), eq(isbn));

            ResultActions resultActions = mockMvc.perform(
                    delete(FAVORITES_API_ENDPOINT)
                            .param("isbn", isbn)
            );

            resultActions
                    .andDo(print())
                    .andExpect(status().isOk());
            verify(favoriteBookService).deleteFavorite(any(Member.class), eq(isbn));
        }

        @Test
        @DisplayName("실패 - 즐겨찾기 삭제 중 예외 발생")
        void fail_deleteFavorite() throws Exception {
            String isbn = "12345";
            willThrow(new FavoriteException.FavoriteNotFound())
                    .given(favoriteBookService).deleteFavorite(any(Member.class), eq(isbn));

            ResultActions resultActions = mockMvc.perform(
                    delete(FAVORITES_API_ENDPOINT)
                            .param("isbn", isbn)
            );

            resultActions
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("즐겨찾기에 등록된 책이 없습니다."));
        }
    }

    @Nested
    @DisplayName("getFavoriteBooks 테스트")
    class GetFavoriteBooksTest {
        @Test
        @DisplayName("성공 - 즐겨찾기 목록 조회")
        void success() throws Exception {
            BookDto bookDto = BookDto.builder()
                    .id(10L)
                    .isbn("12345")
                    .title("Test Book")
                    .image("imageUrl")
                    .description("Test Description")
                    .author("Test Author")
                    .publisher("Test Publisher")
                    .link("http://test.com")
                    .build();
            List<BookDto> dummyList = List.of(bookDto);

            given(favoriteBookService.getFavoriteBooks(any(Member.class)))
                    .willReturn(dummyList);

            ResultActions resultActions = mockMvc.perform(
                    get(FAVORITES_API_ENDPOINT)
            );

            resultActions
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(10L))
                    .andExpect(jsonPath("$[0].isbn").value("12345"))
                    .andExpect(jsonPath("$[0].title").value("Test Book"));
        }
    }
}