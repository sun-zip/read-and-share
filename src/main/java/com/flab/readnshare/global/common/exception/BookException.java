package com.flab.readnshare.global.common.exception;

import lombok.Getter;

@Getter
public class BookException extends RuntimeException {
    private final ErrorCode errorCode;

    public BookException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public static class BookNotFound extends BookException {
        public BookNotFound() {
            super(ErrorCode.BOOK_NOT_FOUND);
        }
    }

}
