package com.test.imageupload.services;

import lombok.Getter;

public class ImageStoreException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;

    public ImageStoreException(String message, ErrorCode errorCode) {
        super(message);

        this.errorCode = errorCode;
    }
}
