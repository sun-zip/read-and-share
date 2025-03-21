package com.flab.readnshare.domain.favorite.controller;

import com.flab.readnshare.domain.book.dto.BookDto;
import com.flab.readnshare.domain.favorite.service.FavoriteBookService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.global.common.resolver.SignInMember;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteBookService favoriteBookService;

    /**
     * 즐겨찾기 추가
     */
    @PostMapping
    public ResponseEntity<Void> addFavorite(@SignInMember Member member,
                                            @RequestParam String isbn) {
        favoriteBookService.addFavorite(member, isbn);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 즐겨찾기 삭제
     */
    @DeleteMapping ResponseEntity<Void> deleteFavorite(@SignInMember Member member,
                                                       @RequestParam String isbn) {
        favoriteBookService.deleteFavorite(member, isbn);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping ResponseEntity<List<BookDto>> getFavoriteBook(@SignInMember Member member) {

        List<BookDto> favoriteBooks = favoriteBookService.getFavoriteBooks(member);


        return ResponseEntity.ok(favoriteBooks);
    }
}
