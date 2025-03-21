package com.flab.readnshare.domain.favorite.repository;

import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.favorite.domain.FavoriteBook;
import com.flab.readnshare.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteBookRepository extends JpaRepository<FavoriteBook, Long> {

    Optional<FavoriteBook> findByMemberAndBook(Member member, Book book);

    // 회원의 즐겨찾기 항목과 연관된 도서 정보를 함께 조회
    @Query("select fb from FavoriteBook fb join fetch fb.book where fb.member = :member")
    List<FavoriteBook> findFavoritesWithBookByMember(Member member);
}
