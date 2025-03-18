package com.flab.readnshare.domain.review.repository;

import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.book.repository.BookRepository;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.member.repository.MemberRepository;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.ReviewTestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
public class ReviewRepositoryTest {
	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private BookRepository bookRepository;

	@Test
	@DisplayName("책 제목으로 리뷰 검색 성공")
	void findByBookTitleContaining() {
		// given
		Member member = memberRepository.save(ReviewTestFixture.createMember("tester"));
		Book book = bookRepository.save(ReviewTestFixture.createBook("Java Basics", "Author", "TechPress", "1234567890"));
		Review review = reviewRepository.save(ReviewTestFixture.createReview(member, book, "Good book!"));

		// when
		List<Review> result = reviewRepository.findByBook_TitleContaining("Java");

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getBook().getTitle()).contains("Java");
		assertThat(result.get(0).getContent()).isEqualTo("Good book!");
	}

	@Test
	@DisplayName("책 저자로 리뷰 검색 성공")
	void findByBookAuthorContaining() {
		// given
		Member member = memberRepository.save(ReviewTestFixture.createMember("tester"));
		Book book = bookRepository.save(ReviewTestFixture.createBook("Java Basics", "Author", "TechPress", "1234567890"));
		Review review = reviewRepository.save(ReviewTestFixture.createReview(member, book, "Good book!"));

		// when
		List<Review> result = reviewRepository.findByBook_AuthorContaining("Author");

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getBook().getAuthor()).contains("Author");
		assertThat(result.get(0).getContent()).isEqualTo("Good book!");
	}

	@Test
	@DisplayName("책 출판사로 리뷰 검색 성공")
	void findByBookPublisherContaining() {
		// given
		Member member = memberRepository.save(ReviewTestFixture.createMember("tester"));
		Book book = bookRepository.save(ReviewTestFixture.createBook("Java Basics", "Author", "TechPress", "1234567890"));
		Review review = reviewRepository.save(ReviewTestFixture.createReview(member, book, "Good book!"));

		// when
		List<Review> result = reviewRepository.findByBook_PublisherContaining("TechPress");

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getBook().getPublisher()).contains("TechPress");
		assertThat(result.get(0).getContent()).isEqualTo("Good book!");
	}


	@Test
	@DisplayName("작성자 이름으로 리뷰 검색 성공")
	void findByMemberNameContaining() {
		// given
		Member member = memberRepository.save(ReviewTestFixture.createMember("tester123"));
		Book book = bookRepository.save(ReviewTestFixture.createBook("Spring", "John", "Publisher", "1234567890"));
		Review review = reviewRepository.save(ReviewTestFixture.createReview(member, book, "Nice review"));

		// when
		List<Review> result = reviewRepository.findByMember_NickNameContaining("tester");

		// then
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getMember().getNickName()).contains("tester");
	}


}
