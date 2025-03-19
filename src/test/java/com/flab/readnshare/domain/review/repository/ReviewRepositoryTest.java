package com.flab.readnshare.domain.review.repository;

import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.book.repository.BookRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.ReviewTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Nested
public class ReviewRepositoryTest {
	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private BookRepository bookRepository;

	private Member member;
	private Book book;
	private Review review;

	// 검색 테스트

	@BeforeEach
	void setUp(){
		member = memberRepository.save(ReviewTestFixture.createMember("tester"));
		book = bookRepository.save(ReviewTestFixture.createBook("Java Basics", "Author", "TechPress", "1234567890"));
		review = reviewRepository.save(ReviewTestFixture.createReview(member, book, "Good book!"));
	}

	@Nested
	@DisplayName("책 제목으로 리뷰 검색 테스트")
	class findByBook_TitleContaining{
		@Test
		@DisplayName("책 제목으로 리뷰 검색 성공")
		void success() {
			// given - setUp에서 처리

			// when
			List<Review> result = reviewRepository.findByBook_TitleContaining("Java");

			// then
			assertThat(result).hasSize(1);
			assertThat(result.get(0).getBook().getTitle()).contains("Java");
			assertThat(result.get(0).getContent()).isEqualTo("Good book!");
		}

		@Test
		@DisplayName("책 제목으로 리뷰 검색 실패 - 일치하는 제목 없음")
		void fail_no_match(){
			List<Review> result = reviewRepository.findByBook_TitleContaining("SMDIOFMXCLKS");
			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("책 저자로 리뷰 검색 테스트")
	class findByBook_AuthorContaining{
		@Test
		@DisplayName("책 저자로 리뷰 검색 성공")
		void success() {
			// given - setUp에서 처리

			// when
			List<Review> result = reviewRepository.findByBook_AuthorContaining("Author");

			// then
			assertThat(result).hasSize(1);
			assertThat(result.get(0).getBook().getAuthor()).contains("Author");
			assertThat(result.get(0).getContent()).isEqualTo("Good book!");
		}
	}

	@Nested
	@DisplayName("책 출판사로 리뷰 검색 테스트")
	class findByBook_PublisherContaining{
		@Test
		@DisplayName("책 출판사로 리뷰 검색 성공")
		void success() {
			// given - setUp에서 처리

			// when
			List<Review> result = reviewRepository.findByBook_PublisherContaining("TechPress");

			// then
			assertThat(result).hasSize(1);
			assertThat(result.get(0).getBook().getPublisher()).contains("TechPress");
			assertThat(result.get(0).getContent()).isEqualTo("Good book!");
		}
	}

	@Nested
	@DisplayName("작성자 이름으로 리뷰 검색 테스트")
	class findByMember_NameContaining{
		@Test
		@DisplayName("작성자 이름으로 리뷰 검색 성공")
		void success() {
			// given - setUp에서 처리

			// when
			List<Review> result = reviewRepository.findByMember_NickNameContaining("tester");

			// then
			assertThat(result).hasSize(1);
			assertThat(result.get(0).getMember().getNickName()).contains("tester");
		}
	}

	@Nested
	@DisplayName("키워드 통합 검색 테스트")
	class searchByKeywordTest{
		@Test
		@DisplayName("책 제목으로 검색 성공")
		void success_searchByTitle() {
			List<Review> result = reviewRepository.searchByKeyword("Jav");

			assertThat(result).hasSize(1);
			assertThat(result.get(0).getBook().getTitle()).contains("Jav");
		}

		@Test
		@DisplayName("저자명으로 검색 성공")
		void success_searchByAuthor() {
			List<Review> result = reviewRepository.searchByKeyword("thor");

			assertThat(result).hasSize(1);
			assertThat(result.get(0).getBook().getAuthor()).contains("thor");
		}

		@Test
		@DisplayName("출판사로 검색 성공")
		void success_searchByPublisher() {
			List<Review> result = reviewRepository.searchByKeyword("chPre");

			assertThat(result).hasSize(1);
			assertThat(result.get(0).getBook().getPublisher()).contains("chPre");
		}

		@Test
		@DisplayName("작성자 닉네임으로 검색 성공")
		void success_searchByNickName() {
			List<Review> result = reviewRepository.searchByKeyword("ester");

			assertThat(result).hasSize(1);
			assertThat(result.get(0).getMember().getNickName()).contains("ester");
		}

		@Test
		@DisplayName("검색 결과 없음")
		void fail_searchNoMatch() {
			List<Review> result = reviewRepository.searchByKeyword("NoMatch");

			assertThat(result).isEmpty();
		}
	}




	//CRUD 테스트

	@Nested
	@DisplayName("저장 테스트")
	class saveTest {
		@Test
		@DisplayName("리뷰 저장 성공")
		void success() {
			Review reviewToSave = ReviewTestFixture.createReview(member, book, "My First Review!");

			Review saved = reviewRepository.save(reviewToSave);

			assertThat(saved.getId()).isNotNull();
			assertThat(saved.getContent()).isEqualTo("My First Review!");
		}
	}

	@Nested
	@DisplayName("ID로 리뷰 찾기 테스트")
	class findByIdTest {
		@Test
		@DisplayName("ID로 리뷰 조회 성공")
		void success() {
			Optional<Review> result = reviewRepository.findById(review.getId());

			assertThat(result).isPresent();
			assertThat(result.get().getContent()).isEqualTo("Good book!");
		}

		@Test
		@DisplayName("리뷰 조회 실패 - 없는 ID")
		void fail_return_empty() {
			Optional<Review> result = reviewRepository.findById(-1L);

			assertThat(result).isEmpty();
		}
	}

	@Nested
	@DisplayName("모든 리뷰 조회 테스트")
	class findAllTest {
		@Test
		@DisplayName("모든 리뷰 조회 성공")
		void success() {
			Review review1 = reviewRepository.save(ReviewTestFixture.createReview(member, book, "Good book! 1"));
			Review review2 = reviewRepository.save(ReviewTestFixture.createReview(member, book, "Good book! 2"));
			Review review3 = reviewRepository.save(ReviewTestFixture.createReview(member, book, "Good book! 3"));

			List<Review> result = reviewRepository.findAll();

			assertThat(result).hasSize(4);
			assertThat(result).extracting(Review::getContent)
					.containsExactlyInAnyOrder(
							"Good book!", "Good book! 1", "Good book! 2", "Good book! 3"
					);
		}
	}

	@Nested
	@DisplayName("ID로 리뷰 삭제 테스트")
	class deleteByIdTest {
		@Test
		@DisplayName("리뷰 삭제 성공")
		void success() {
			reviewRepository.deleteById(review.getId());

			Optional<Review> result = reviewRepository.findById(review.getId());
			assertThat(result).isEmpty();
		}
	}

}
