package com.flab.readnshare.domain.favorite.controller;

import com.flab.readnshare.domain.book.dto.BookDto;
import com.flab.readnshare.domain.favorite.service.FavoriteBookService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.global.common.resolver.SignInMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(FavoriteController.class)
class FavoriteControllerTest {
    private MockMvc mockMvc;
    private FavoriteBookService favoriteBookService;

    @BeforeEach
    void setUp() {
        // 필요한 서비스 모킹
        favoriteBookService = mock(FavoriteBookService.class);
        FavoriteController favoriteController = new FavoriteController(favoriteBookService);

        // @SignInMember를 처리할 커스텀 인자 해결기 등록
        HandlerMethodArgumentResolver signInMemberResolver = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(SignInMember.class);
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
                .build();
    }

    @Test
    @DisplayName("즐겨찾기 추가 API 테스트")
    void addFavorite() throws Exception {
        String isbn = "12345";

        mockMvc.perform(post("/api/v1/favorites")
                        .param("isbn", isbn))
                .andExpect(status().isCreated());

        verify(favoriteBookService).addFavorite(argThat(member -> member.getId() == 1L), eq(isbn));
    }

    @Test
    @DisplayName("즐겨찾기 삭제 API 테스트")
    void deleteFavorite() throws Exception {
        String isbn = "12345";

        mockMvc.perform(delete("/api/v1/favorites")
                        .param("isbn", isbn))
                .andExpect(status().isOk());

        verify(favoriteBookService).deleteFavorite(argThat(member -> member.getId() == 1L), eq(isbn));
    }

    @Test
    @DisplayName("즐겨찾기 조회 API 테스트")
    void getFavoriteBooks() throws Exception {
        BookDto dummyBook = BookDto.builder()
                .id(10L)
                .isbn("12345")
                .title("Test Book")
                .image("imageUrl")
                .description("Test Description")
                .author("Test Author")
                .publisher("Test Publisher")
                .link("http://test.com")
                .build();
        List<BookDto> dummyList = List.of(dummyBook);

        when(favoriteBookService.getFavoriteBooks(argThat(member -> member.getId() == 1L)))
                .thenReturn(dummyList);

        mockMvc.perform(get("/api/v1/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].isbn").value("12345"))
                .andExpect(jsonPath("$[0].title").value("Test Book"));
    }
}