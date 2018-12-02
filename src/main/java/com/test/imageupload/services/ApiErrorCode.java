package com.test.imageupload.services;

/**
 * Upload image error codes
 *
 * @author Vladimir Moiseev
 */
public enum ApiErrorCode {
    INVALID_FILE_FORMAT,

    SYSTEM_ERROR,

    URL_READ_TIMEOUT,

    URL_MAX_FILES,

    INCORRECT_API_PARAMS,

    FILE_EMPTY,

    FILE_TOO_LARGE,

    FILE_NOT_EXIST,

    BASE64_JSON_FORMAT,

    BASE64_SCHEME_INVALID,
}
