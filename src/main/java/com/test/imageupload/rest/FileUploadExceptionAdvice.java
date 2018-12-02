package com.test.imageupload.rest;

import static com.test.imageupload.services.ErrorCode.FILE_TOO_LARGE;

import com.test.imageupload.data.ImageStoreError;
import com.test.imageupload.data.ImageUploadResult;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class FileUploadExceptionAdvice extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxSize;

    @Value("${spring.servlet.multipart.max-request-size}")
    private String maxRequestSize;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    protected ResponseEntity<ImageUploadResult> handleMaxUploadSizeException() {
        String message = messageSource.getMessage("imageStore.error.fileIsTooLarge", new Object[]{maxSize, maxRequestSize}, Locale.getDefault());
        ImageStoreError error= new ImageStoreError(message, FILE_TOO_LARGE);

        ImageUploadResult uploadResult = new ImageUploadResult();
        uploadResult.addError(error);

        return new ResponseEntity<>(uploadResult, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
