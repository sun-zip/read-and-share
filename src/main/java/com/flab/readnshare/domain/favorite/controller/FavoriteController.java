package com.flab.readnshare.domain.favorite.controller;

import com.flab.readnshare.domain.book.dto.BookDto;
import com.flab.readnshare.domain.favorite.service.FavoriteBookService;
import com.flab.readnshare.domain.member.domain.Member;
import com.flab.readnshare.global.common.resolver.SignInMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorite", description = "즐겨찾기")
public class FavoriteController {
    private final FavoriteBookService favoriteBookService;

    /**
     * 즐겨찾기 추가
     */
    @PostMapping
    @Operation(summary = "즐겨찾기 추가", description = "즐겨찾기 추가 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "즐겨찾기 추가 성공."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(파라미터 오류)", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "올바르지 않은 입력값", value = "{ \"code\": \"INVALID_INPUT_PARAMETER\", \"message\": \"입력값을 확인하세요.\" }"),
            })),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "이미 즐겨찾기에 추가되어 있음", value = "{ \"code\": \"FAVORITE_ALREADY_EXIST\", \"message\": \"이미 즐겨찾기에 등록되어 있는 책입니다.\" }"),
            })),
            @ApiResponse(responseCode = "401", description = "토큰이 없습니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "JWT_NULL", value = "{ \"code\": \"JWT_NULL\", \"message\": \"토큰이 없습니다.\" }")
                    })),
    })
    public ResponseEntity<Void> addFavorite(@SignInMember Member member,
                                            @RequestParam String isbn) {
        favoriteBookService.addFavorite(member, isbn);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * 즐겨찾기 삭제
     */
    @DeleteMapping
    @Operation(summary = "즐겨찾기 삭제", description = "즐겨찾기 삭제 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "즐겨찾기 삭제 성공."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청(파라미터 오류)", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "올바르지 않은 입력값", value = "{ \"code\": \"INVALID_INPUT_PARAMETER\", \"message\": \"입력값을 확인하세요.\" }"),
            })),
            @ApiResponse(responseCode = "401", description = "토큰이 없습니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "JWT_NULL", value = "{ \"code\": \"JWT_NULL\", \"message\": \"토큰이 없습니다.\" }")
                    })),
    })
    ResponseEntity<Void> deleteFavorite(@SignInMember Member member,
                                                       @RequestParam String isbn) {
        favoriteBookService.deleteFavorite(member, isbn);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    @Operation(summary = "즐겨찾기 조회", description = "즐겨찾기 조회 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "즐겨찾기 조회 성공."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "올바르지 않은 입력값", value = "{ \"code\": \"INVALID_INPUT_PARAMETER\", \"message\": \"입력값을 확인하세요.\" }"),
            })),
            @ApiResponse(responseCode = "401", description = "토큰이 없습니다.", content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "JWT_NULL", value = "{ \"code\": \"JWT_NULL\", \"message\": \"토큰이 없습니다.\" }")
                    })),
    })
    ResponseEntity<List<BookDto>> getFavoriteBook(@SignInMember Member member) {

        List<BookDto> favoriteBooks = favoriteBookService.getFavoriteBooks(member);


        return ResponseEntity.ok(favoriteBooks);
    }
}
