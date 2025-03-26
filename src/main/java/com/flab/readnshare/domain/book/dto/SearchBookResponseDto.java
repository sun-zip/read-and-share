package com.flab.readnshare.domain.book.dto;


import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class SearchBookResponseDto {
    private Integer total;
    private Integer start;
    private Integer display;
    private Integer totalPage;
    private Integer currentPage;
    @Builder.Default
    List<Items> items = new ArrayList<>();

    @Getter
    public static class Items {
        private String title;
        private String image;
        private String author;
        private String isbn;
        private String description;
    }
}
