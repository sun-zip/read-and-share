package com.flab.readnshare.global.common.exception;

import lombok.Getter;

@Getter
public class ImageException extends RuntimeException {
    private final ErrorCode errorCode;

    public ImageException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public static class NotFoundImageException extends ImageException {
        public NotFoundImageException() {
            super(ErrorCode.IMAGE_NOT_FOUND);
        }
    }


}
