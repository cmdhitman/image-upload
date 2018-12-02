package com.test.imageupload.services;

import lombok.Getter;

/**
 * Common storage exception
 *
 * @author Vladimir Moiseev
 */
public class ImageStoreException extends RuntimeException {

    @Getter
    private final ApiErrorCode errorCode;

    public ImageStoreException(String message, ApiErrorCode errorCode) {
        super(message);

        this.errorCode = errorCode;
    }
}
