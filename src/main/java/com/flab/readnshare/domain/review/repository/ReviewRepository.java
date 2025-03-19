package com.flab.readnshare.domain.review.repository;

import com.flab.readnshare.domain.review.domain.Review;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT r FROM Review r WHERE r.id = :id")
    Optional<Review> findByIdForUpdate(Long id);

    @Query("SELECT r FROM Review r JOIN FETCH r.book JOIN FETCH r.member WHERE r.id IN :reviewIds")
    List<Review> findByIdIn(List<Long> reviewIds);

    // 책 제목으로 검색
    List<Review> findByBook_TitleContaining(String title);

    // 책 저자명으로 검색
    List<Review> findByBook_AuthorContaining(String author);

    // 책 출판사로 검색
    List<Review> findByBook_PublisherContaining(String publisher);

    // 작성자 이름으로 검색
    List<Review> findByMember_NickNameContaining(String memberName);

    // OR 조건을 활용해 통합 검색도 가능 (Optional)
    @Query("SELECT r FROM Review r WHERE " +
            "r.book.title LIKE %:keyword% OR " +
            "r.book.author LIKE %:keyword% OR " +
            "r.book.publisher LIKE %:keyword% OR " +
            "r.member.nickName LIKE %:keyword%")
    List<Review> searchByKeyword(@Param("keyword") String keyword);
}
