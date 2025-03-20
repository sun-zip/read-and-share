package com.flab.readnshare.domain.book.dto;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class SearchBookResponseDto {
    public Integer total;
    public Integer start;
    public Integer display;
    private Integer totalPage;
    private Integer currentPage;
    List<Items> items = new ArrayList<>();

    @Getter
    final static class Items {
        private String title;
        private String image;
        private String author;
        private String isbn;
        private String description;
    }
}
