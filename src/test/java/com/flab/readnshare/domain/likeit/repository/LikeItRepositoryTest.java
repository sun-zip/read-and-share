package com.flab.readnshare.domain.likeit.repository;

import com.flab.readnshare.ReviewTestFixture;
import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.book.repository.BookRepository;
import com.flab.readnshare.domain.likeit.domain.LikeIt;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Nested
@DataJpaTest
public class LikeItRepositoryTest {

	@Autowired
	private LikeItRepository likeItRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private ReviewRepository reviewRepository;

	private Member fromMember;
	private Book reviewedBook;
	private Review toReview;
	@Autowired
	private BookRepository bookRepository;


	@BeforeEach
	void setup(){


		fromMember = memberRepository.save(	ReviewTestFixture.createMember("tester"));
		reviewedBook = bookRepository.save(	ReviewTestFixture.createBook("Java Basics", "Author", "TechPress", "1234567890"));
		toReview = reviewRepository.save( ReviewTestFixture.createReview(fromMember, reviewedBook, 7,"Test review content"));
	}

	@Nested
	@DisplayName("주체 회원과 대상 리뷰로 좋아요 검색 테스트")
	class findByFromMemberAndToReviewTest{

		@Test
		@DisplayName("주체 회원과 대상 리뷰로 좋아요 검색 성공")
		void success(){
			// given
			LikeIt likeIt = likeItRepository.save(LikeIt.builder().fromMember(fromMember).toReview(toReview).build());

			// when
			Optional<LikeIt> result = likeItRepository.findByFromMemberAndToReview(fromMember, toReview);

			// then
			assertThat(result).isPresent();
			assertThat(result.get()).isEqualTo(likeIt);
		}

		@Test
		@DisplayName("검색 실패 - 결과 없음")
		void fail_no_like(){

			// when
			Optional<LikeIt> result = likeItRepository.findByFromMemberAndToReview(fromMember, toReview);

			// then
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("좋아요 저장 테스트")
	class saveTest{

		@Test
		@DisplayName("좋아요 저장 성공")
		void success(){
			// given
			LikeIt toBeSaved = LikeIt.builder().fromMember(fromMember).toReview(toReview).build();

			// when
			LikeIt saved = likeItRepository.save(toBeSaved);

			// then
			assertThat(saved).isNotNull();
			assertThat(saved.getId()).isNotNull();
			assertThat(saved.getFromMember()).isEqualTo(fromMember);
			assertThat(saved.getToReview()).isEqualTo(toReview);
		}
	}

	@Nested
	@DisplayName("좋아요 삭제 테스트")
	class deleteTest{

		@Test
		@DisplayName("좋아요 삭제 성공")
		void success(){
			// given
			LikeIt toBeDeleted = likeItRepository.save(LikeIt.builder().fromMember(fromMember).toReview(toReview).build());

			// when
			likeItRepository.delete(toBeDeleted);

			// then
			assertThat(likeItRepository.findByFromMemberAndToReview(toBeDeleted.getFromMember(), toBeDeleted.getToReview())).isEmpty();
		}
	}
}
