package com.test.imageupload.data;

import com.test.imageupload.services.ApiErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents api error
 *
 * @author Vladimir Moiseev
 */
@Data
@AllArgsConstructor
public class ImageStoreError {

    /**
     * Default error message
     */
    private String errorMessage;

    /**
     * Api error code
     */
    private ApiErrorCode errorCode;
}
