package com.flab.readnshare.domain.favorite.repository;

import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.book.repository.BookRepository;
import com.flab.readnshare.domain.favorite.domain.FavoriteBook;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class FavoriteBookRepositoryTest {

    @Autowired
    FavoriteBookRepository favoriteBookRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BookRepository bookRepository;

    @Nested
    @DisplayName("findByMemberAndBook 테스트")
    class FindByMemberAndBookTest {
        @Test
        @DisplayName("성공")
        void success() {
            // Given: 테스트용 회원과 도서 생성
            Member member = createMember("user1@example.com", "User1");
            memberRepository.save(member);

            Book book = createBook("1234567890", "Test Book");
            bookRepository.save(book);

            // 즐겨찾기 등록
            FavoriteBook favoriteBook = FavoriteBook.builder()
                    .member(member)
                    .book(book)
                    .build();
            favoriteBookRepository.save(favoriteBook);

            // When: 회원과 도서에 해당하는 즐겨찾기 조회
            Optional<FavoriteBook> result = favoriteBookRepository.findByMemberAndBook(member, book);

            // Then
            assertThat(result).isPresent();
            FavoriteBook fb = result.get();
            assertThat(fb.getMember().getEmail()).isEqualTo(member.getEmail());
            assertThat(fb.getBook().getIsbn()).isEqualTo(book.getIsbn());
        }
    }

    @Nested
    @DisplayName("findFavoritesWithBookByMember 테스트")
    class FindFavoritesWithBookByMemberTest {
        @Test
        @DisplayName("성공")
        void success() {
            // Given: 테스트용 회원 생성
            Member member = createMember("user1@example.com", "User1");
            memberRepository.save(member);

            // 두 개의 도서 생성 및 저장
            Book book1 = createBook("1234567890", "Test Book 1");
            Book book2 = createBook("0987654321", "Test Book 2");
            bookRepository.saveAll(List.of(book1, book2));

            // 해당 회원에 대한 즐겨찾기 등록
            FavoriteBook favorite1 = FavoriteBook.builder()
                    .member(member)
                    .book(book1)
                    .build();
            FavoriteBook favorite2 = FavoriteBook.builder()
                    .member(member)
                    .book(book2)
                    .build();
            favoriteBookRepository.saveAll(List.of(favorite1, favorite2));

            // When: 회원의 즐겨찾기 목록 조회
            List<FavoriteBook> favorites = favoriteBookRepository.findFavoritesWithBookByMember(member);

            // Then: 두 건의 즐겨찾기가 조회되어야 함
            assertThat(favorites).hasSize(2)
                    .extracting(fb -> fb.getBook().getIsbn())
                    .containsExactlyInAnyOrder("1234567890", "0987654321");
        }
    }

    // 테스트용 엔티티 생성 헬퍼 메서드

    private Member createMember(String email, String nickName) {
        return Member.builder()
                .email(email)
                .nickName(nickName)
                .password("password")  // 더미 값
                .build();
    }

    private Book createBook(String isbn, String title) {
        return Book.builder()
                .isbn(isbn)
                .title(title)
                .image("dummyImage")
                .description("dummyDescription")
                .author("dummyAuthor")
                .publisher("dummyPublisher")
                .link("dummyLink")
                .build();
    }
}
