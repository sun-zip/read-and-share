package com.flab.readnshare.domain.review.facade;

import com.flab.readnshare.domain.book.domain.Book;
import com.flab.readnshare.domain.book.dto.BookDto;
import com.flab.readnshare.domain.book.service.BookService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.domain.review.domain.Review;
import com.flab.readnshare.domain.review.dto.SaveReviewRequestDto;
import com.flab.readnshare.domain.review.event.ReviewCreateEvent;
import com.flab.readnshare.domain.review.event.ReviewDeleteEvent;
import com.flab.readnshare.domain.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewFacade {
    private final BookService bookService;
    private final ReviewService reviewService;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long save(SaveReviewRequestDto dto, Member signInMember) {
        BookDto bookDto = dto.getBook();
        Book book = bookService.save(bookDto);

        Review review = dto.toEntity(signInMember, book);
        Long reviewId = reviewService.save(review);

        eventPublisher.publishEvent(new ReviewCreateEvent(signInMember, review));

        return reviewId;
    }

    @Transactional
    public void delete(Long reviewId, Member signInMember) {
        reviewService.delete(reviewId, signInMember);

        eventPublisher.publishEvent(new ReviewDeleteEvent(signInMember, reviewId));
    }
}
