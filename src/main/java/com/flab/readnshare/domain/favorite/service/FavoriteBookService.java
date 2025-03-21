package com.flab.readnshare.domain.favorite.service;


import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.book.dto.BookDto;
import com.flab.readnshare.domain.book.dto.SearchBookDetailResponseDto;
import com.flab.readnshare.domain.book.repository.BookRepository;
import com.flab.readnshare.domain.book.service.BookService;
import com.flab.readnshare.domain.favorite.domain.FavoriteBook;
import com.flab.readnshare.domain.favorite.repository.FavoriteBookRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.domain.member.service.MemberService;
import com.flab.readnshare.global.common.exception.FavoriteException;
import com.flab.readnshare.global.common.exception.MemberException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteBookService {
    private static final Logger log = LoggerFactory.getLogger(FavoriteBookService.class);
    private final FavoriteBookRepository favoriteBookRepository;
    private final BookRepository bookRepository;
    private final MemberService memberService;
    private final BookService bookService;

    /**
     * 즐겨찾기 추가
     * @param member
     * @param isbn
     */
    public void addFavorite(Member member, String isbn) {

        // 1. DB에서 책 조회 (없으면 도서 정보 저장)
        Book book = bookRepository.findByIsbn(isbn)
                .orElseGet(() -> bookRepository.save(bookService.searchBookDetail(isbn).toEntity()));

        // 이미 즐겨찾기에 등록되어 있는지 확인
        favoriteBookRepository.findByMemberAndBook(member,book)
                .ifPresent(favoriteBook -> {
                    throw new FavoriteException.FavoriteAlreadyExist();
                });

        // 2. 즐겨찾기에 등록
        FavoriteBook favoriteBook = FavoriteBook.builder()
                .member(member)
                .book(book)
                .build();
        favoriteBookRepository.save(favoriteBook);
    }

    /**
     * 즐겨찾기 삭제
     * @param member
     * @param isbn
     */
    public void deleteFavorite(Member member, String isbn) {
        // 1. DB에서 책 조회
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(FavoriteException.FavoriteNotFound::new);

        // 2. 즐겨찾기에서 삭제
        favoriteBookRepository.findByMemberAndBook(member, book)
                .ifPresent(favoriteBookRepository::delete);
    }

    /**
     * 즐겨찾기 목록 조회
     * @param member
     * @return
     */
    public List<BookDto> getFavoriteBooks(Member member) {
        List<FavoriteBook> favorites = favoriteBookRepository.findFavoritesWithBookByMember(member);

         return favorites.stream()
                .map(fb -> convertToSearchBookResponseDto(fb.getBook()))
                .collect(Collectors.toList());
    }


    /**
     * Book 엔티티를 BookDto로 변환
     * @param book
     * @return
     */
    private BookDto convertToSearchBookResponseDto(Book book) {
        return BookDto.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .image(book.getImage())
                .description(book.getDescription())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .link(book.getLink())
                .build();
    }
}
