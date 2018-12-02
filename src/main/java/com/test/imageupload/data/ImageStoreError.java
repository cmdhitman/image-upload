package com.test.imageupload.data;

import com.test.imageupload.services.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageStoreError {

    private String errorMessage;

    private ErrorCode errorCode;
}
