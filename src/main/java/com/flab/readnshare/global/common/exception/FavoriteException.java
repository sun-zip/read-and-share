package com.flab.readnshare.global.common.exception;

import lombok.Getter;

@Getter
public class FavoriteException extends RuntimeException{
    private final ErrorCode errorCode;

    public FavoriteException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public static class FavoriteNotFound extends FavoriteException {
        public FavoriteNotFound() {
            super(ErrorCode.FAVORITE_NOT_FOUND);
        }
    }

    public static class FavoriteAlreadyExist extends FavoriteException {
        public FavoriteAlreadyExist() {
            super(ErrorCode.FAVORITE_ALREADY_EXIST);
        }
    }


}
