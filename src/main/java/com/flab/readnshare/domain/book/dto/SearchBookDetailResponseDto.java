package com.flab.readnshare.domain.book.dto;

import com.flab.readnshare.domain.book.domain.Book;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SearchBookDetailResponseDto {
    List<Items> items = new ArrayList<>();

    @Getter
    @Builder
    static public class Items {
        private String title;
        private String image;
        private String author;
        private String isbn;
        private String description;
        private String publisher;
        private String link;
    }

    public Book toEntity() {
        Items item = items.get(0);
        return Book.builder()
                .title(item.getTitle())
                .image(item.getImage())
                .author(item.getAuthor())
                .isbn(item.getIsbn())
                .description(item.getDescription())
                .publisher(item.getPublisher())
                .link(item.link)
                .build();
    }
}
