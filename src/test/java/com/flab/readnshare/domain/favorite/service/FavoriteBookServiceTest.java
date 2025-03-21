package com.flab.readnshare.domain.favorite.service;

import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.book.dto.BookDto;
import com.flab.readnshare.domain.book.dto.SearchBookDetailResponseDto;
import com.flab.readnshare.domain.book.repository.BookRepository;
import com.flab.readnshare.domain.book.service.BookService;
import com.flab.readnshare.domain.favorite.domain.FavoriteBook;
import com.flab.readnshare.domain.favorite.repository.FavoriteBookRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.service.MemberService;
import com.flab.readnshare.global.common.exception.FavoriteException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteBookServiceTest {

    @Mock
    private FavoriteBookRepository favoriteBookRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    MemberService memberService;

    @Mock
    private BookService bookService;

    @InjectMocks
    private FavoriteBookService favoriteBookService;


    @Nested
    @DisplayName("addFavorite 테스트")
    class addFavoriteTest {
        @Test
        @DisplayName("즐겨찾기 추가 성공 - 서적 DB에 이미 있는 경우")
        void success_alreadyExist() {
            Member member = Member.builder().id(1L).build();
            String isbn = "12345";

            // 이미 서적 DB에 존재하는 Book 엔티티 생성
            Book existingBook = Book.builder()
                    .id(10L)
                    .isbn(isbn)
                    .title("Test Book")
                    .image("imageUrl")
                    .description("Test Description")
                    .author("Test Author")
                    .publisher("Test Publisher")
                    .link("http://test.com")
                    .build();

            // when: 책이 DB에 이미 있는 경우이므로 Optional.of(existingBook)를 반환
            when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(existingBook));
            // 해당 회원의 즐겨찾기에 아직 등록되지 않았음을 설정
            when(favoriteBookRepository.findByMemberAndBook(member, existingBook)).thenReturn(Optional.empty());

            // When
            favoriteBookService.addFavorite(member, isbn);

            // Then: 즐겨찾기 저장(save)이 1회 호출되었는지 검증
            verify(favoriteBookRepository, times(1)).save(any(FavoriteBook.class));
            // 책 정보 검색(BookService.searchBookDetail) 및 저장(bookRepository.save)는 호출되지 않아야 함
            verify(bookService, never()).searchBookDetail(anyString());
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("즐겨찾기 추가 성공 - 서적db에 없는 경우")
        void success_notExist() {
            Member member = Member.builder()
                    .id(1L)
                    .build();
            String isbn = "12345";

            //when
            when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());


            // SearchBookDetailResponseDto 생성 및 내부 Items 설정 (Reflection 사용)
            SearchBookDetailResponseDto detailResponse = new SearchBookDetailResponseDto();
            SearchBookDetailResponseDto.Items item;
            // BookService.searchBookDetail이 위 DTO를 반환하도록 설정
            when(bookService.searchBookDetail(isbn)).thenReturn(createDummyDetailResponse(isbn));

            // BookRepository.save() 호출 시, 저장된 Book 엔티티 반환
            Book savedBook = Book.builder()
                    .id(10L)
                    .isbn(isbn)
                    .title("Test Book")
                    .image("imageUrl")
                    .description("Test Description")
                    .author("Test Author")
                    .publisher("Test Publisher")
                    .link("http://test.com")
                    .build();

            when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

            // 해당 회원의 즐겨찾기에 이 도서가 등록되어 있지 않음을 설정
            when(favoriteBookRepository.findByMemberAndBook(member, savedBook)).thenReturn(Optional.empty());

            // When
            favoriteBookService.addFavorite(member, isbn);

            // Then: 즐겨찾기 저장(save)이 1회 호출되었는지 검증
            verify(favoriteBookRepository, times(1)).save(any(FavoriteBook.class));

        }

        @Test
        @DisplayName("즐겨찾기 추가 실패 - 이미 즐겨찾기에 등록되어 있음")
        void fail_alreadyExist() {
            // Given
            Member member = Member.builder()
                    .id(1L)
                    .build();
            String isbn = "12345";

            // 이미 존재하는 Book 엔티티
            Book existingBook = Book.builder()
                    .id(10L)
                    .isbn(isbn)
                    .title("Test Book")
                    .build();

            // Book이 이미 존재함
            when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(existingBook));
            // 이미 즐겨찾기에 등록되어 있는 상태
            FavoriteBook existingFavorite = FavoriteBook.builder()
                    .member(member)
                    .book(existingBook)
                    .build();
            when(favoriteBookRepository.findByMemberAndBook(member, existingBook)).thenReturn(Optional.of(existingFavorite));

            // When & Then: 중복 등록 시 FavoriteException.FavoriteAlreadyExist 예외 발생
            assertThrows(FavoriteException.FavoriteAlreadyExist.class, () -> {
                favoriteBookService.addFavorite(member, isbn);
            });
            verify(favoriteBookRepository, never()).save(any(FavoriteBook.class));
        }
    }





    @Nested
    @DisplayName("deleteFavorite 테스트")
    class DeleteFavoriteTest {

        @Test
        @DisplayName("즐겨찾기 삭제 성공")
        void success() {
            // Given: 회원과 ISBN, 그리고 DB에 존재하는 Book과 즐겨찾기 엔티티 생성
            Member member = Member.builder().id(1L).build();
            String isbn = "12345";
            Book existingBook = Book.builder()
                    .id(10L)
                    .isbn(isbn)
                    .title("Test Book")
                    .build();
            FavoriteBook favorite = FavoriteBook.builder()
                    .member(member)
                    .book(existingBook)
                    .build();

            // When: 책이 DB에 존재하고, 해당 즐겨찾기 엔티티도 존재하는 상황
            when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(existingBook));
            when(favoriteBookRepository.findByMemberAndBook(member, existingBook)).thenReturn(Optional.of(favorite));

            // 메서드 호출
            favoriteBookService.deleteFavorite(member, isbn);

            // Then: 즐겨찾기 삭제(delete)가 한 번 호출되었음을 검증
            verify(favoriteBookRepository, times(1)).delete(favorite);
        }

        @Test
        @DisplayName("즐겨찾기 삭제 실패 - 책 없음")
        void fail_bookNotFound() {
            // Given: 회원과 ISBN, 그리고 DB에 해당 책이 없는 상황
            Member member = Member.builder().id(1L).build();
            String isbn = "12345";

            // When: 책을 찾지 못함
            when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

            // Then: 삭제 시도 시 FavoriteException.FavoriteNotFound 예외가 발생함을 검증
            assertThrows(FavoriteException.FavoriteNotFound.class, () -> {
                favoriteBookService.deleteFavorite(member, isbn);
            });
            // 즐겨찾기 삭제 메서드가 호출되지 않았음을 검증
            verify(favoriteBookRepository, never()).delete(any(FavoriteBook.class));
        }

        @Test
        @DisplayName("즐겨찾기 삭제 - 즐겨찾기 엔티티 없음")
        void noFavoriteFound() {
            // Given: 회원과 ISBN, 그리고 DB에는 책은 존재하지만 해당 회원의 즐겨찾기 정보가 없는 경우
            Member member = Member.builder().id(1L).build();
            String isbn = "12345";
            Book existingBook = Book.builder()
                    .id(10L)
                    .isbn(isbn)
                    .title("Test Book")
                    .build();

            // When: 책은 찾았으나 즐겨찾기 엔티티는 존재하지 않음
            when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(existingBook));
            when(favoriteBookRepository.findByMemberAndBook(member, existingBook)).thenReturn(Optional.empty());

            // 메서드 호출
            favoriteBookService.deleteFavorite(member, isbn);

            // Then: 즐겨찾기 삭제(delete)가 호출되지 않았음을 검증
            verify(favoriteBookRepository, never()).delete(any(FavoriteBook.class));
        }
    }

    @Nested
    @DisplayName("getFavoriteBooks 테스트")
    class GetFavoriteBooksTest {

        @Test
        @DisplayName("즐겨찾기 목록 조회 - 즐겨찾기가 있는 경우")
        void getFavoriteBooksWithFavorites() {
            // Given
            Member member = Member.builder().id(1L).build();

            // 첫 번째 도서 생성
            Book book1 = Book.builder()
                    .id(10L)
                    .isbn("isbn1")
                    .title("Book One")
                    .image("img1")
                    .description("desc1")
                    .author("author1")
                    .publisher("publisher1")
                    .link("link1")
                    .build();

            // 두 번째 도서 생성
            Book book2 = Book.builder()
                    .id(11L)
                    .isbn("isbn2")
                    .title("Book Two")
                    .image("img2")
                    .description("desc2")
                    .author("author2")
                    .publisher("publisher2")
                    .link("link2")
                    .build();

            // 해당 도서들에 대한 즐겨찾기 엔티티 생성
            FavoriteBook fav1 = FavoriteBook.builder()
                    .member(member)
                    .book(book1)
                    .build();
            FavoriteBook fav2 = FavoriteBook.builder()
                    .member(member)
                    .book(book2)
                    .build();

            // 즐겨찾기 목록 반환 설정
            when(favoriteBookRepository.findFavoritesWithBookByMember(member))
                    .thenReturn(List.of(fav1, fav2));

            // When
            List<BookDto> favoriteBooks = favoriteBookService.getFavoriteBooks(member);

            // Then: 반환된 BookDto 리스트의 크기와 필드 값 검증
            assertEquals(2, favoriteBooks.size());

            BookDto dto1 = favoriteBooks.get(0);
            BookDto dto2 = favoriteBooks.get(1);

            // dto1 검증
            assertAll("dto1",
                    () -> assertEquals(book1.getId(), dto1.getId()),
                    () -> assertEquals(book1.getIsbn(), dto1.getIsbn()),
                    () -> assertEquals(book1.getTitle(), dto1.getTitle()),
                    () -> assertEquals(book1.getImage(), dto1.getImage()),
                    () -> assertEquals(book1.getDescription(), dto1.getDescription()),
                    () -> assertEquals(book1.getAuthor(), dto1.getAuthor()),
                    () -> assertEquals(book1.getPublisher(), dto1.getPublisher()),
                    () -> assertEquals(book1.getLink(), dto1.getLink())
            );

            // dto2 검증
            assertAll("dto2",
                    () -> assertEquals(book2.getId(), dto2.getId()),
                    () -> assertEquals(book2.getIsbn(), dto2.getIsbn()),
                    () -> assertEquals(book2.getTitle(), dto2.getTitle()),
                    () -> assertEquals(book2.getImage(), dto2.getImage()),
                    () -> assertEquals(book2.getDescription(), dto2.getDescription()),
                    () -> assertEquals(book2.getAuthor(), dto2.getAuthor()),
                    () -> assertEquals(book2.getPublisher(), dto2.getPublisher()),
                    () -> assertEquals(book2.getLink(), dto2.getLink())
            );
        }

        @Test
        @DisplayName("즐겨찾기 목록 조회 - 즐겨찾기가 없는 경우")
        void getFavoriteBooksEmpty() {
            // Given
            Member member = Member.builder().id(1L).build();
            when(favoriteBookRepository.findFavoritesWithBookByMember(member))
                    .thenReturn(List.of());

            // When
            List<BookDto> favoriteBooks = favoriteBookService.getFavoriteBooks(member);

            // Then: 빈 리스트 반환 검증
            assertTrue(favoriteBooks.isEmpty());
        }
    }

    private SearchBookDetailResponseDto createDummyDetailResponse(String isbn) {
        SearchBookDetailResponseDto detailResponse = new SearchBookDetailResponseDto();
        SearchBookDetailResponseDto.Items item = SearchBookDetailResponseDto.Items.builder()
                .title("Test Book")
                .image("imageUrl")
                .author("Test Author")
                .isbn(isbn)
                .description("Test Description")
                .publisher("Test Publisher")
                .link("http://test.com")
                .build();
        detailResponse.getItems().add(item);
        return detailResponse;
    }
}