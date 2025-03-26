package com.flab.readnshare.domain.review.domain;

import com.flab.readnshare.global.common.exception.ReviewException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReviewTest {

	private Review testReview;

	@Nested
	@DisplayName("별점 검증 테스트")
	class validateAndDefaultScoreTest {
		@Test
		@DisplayName("성공 - 입력된 별점이 유요할 때 해당 값을 반환한다")
		void success_input_score() {
			Integer inputScore = 7;
			testReview = Review.builder().id(1L).score(inputScore).build();
			assertThat(testReview.getScore()).isEqualTo(inputScore);
		}

		@Test
		@DisplayName("성공 - 별점을 기입하지 않았을 때 기본값 10점을 반환한다")
		void success_default_score() {
			testReview = Review.builder().id(1L).build();
			assertThat(testReview.getScore()).isEqualTo(10);
		}

		@Test
		@DisplayName("실패 - 0보다 작은 별점은 예외가 발생")
		void fail_score_less_than_zero() {
			assertThrows( ReviewException.InvalidScoreException.class,
					() -> Review.builder().id(1L).score(-1).build());
		}

		@Test
		@DisplayName("실패 - 10보다 큰 별점은 예외가 발생")
		void fail_score_larger_than_ten() {
			assertThrows( ReviewException.InvalidScoreException.class,
					() -> Review.builder().id(1L).score(11).build());
		}
	}
}
