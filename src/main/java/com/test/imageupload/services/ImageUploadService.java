package com.test.imageupload.services;

import static com.test.imageupload.services.ApiErrorCode.INCORRECT_API_PARAMS;
import static com.test.imageupload.services.ApiErrorCode.URL_MAX_FILES;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.imageupload.config.AppProperties;
import com.test.imageupload.data.Base64Image;
import com.test.imageupload.data.ImageStoreError;
import com.test.imageupload.data.ImageStoreResult;
import com.test.imageupload.data.ImageUploadResult;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Upload image service
 *
 * @author Vladimir Moiseev
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private final MessageSource messageSource;
    private final ImageStorageService storageService;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    /**
     * Upload images by types
     *
     * @param files From multipart files
     * @param urlImages From URL
     * @param base64Images From base64
     * @return Upload result
     */
    public ImageUploadResult upload(MultipartFile[] files, URL[] urlImages, String base64Images) {
        ImageUploadResult uploadResult = new ImageUploadResult();

        Stream<CompletableFuture<ImageStoreResult>> stream = getFutureStream(files, urlImages, base64Images, uploadResult);

        stream.forEach(future -> {
            try {
                uploadResult.addStoreResult(future.join());
            } catch (Exception e) {
                uploadResult.addError(getError(e));
            }
        });

        if (uploadResult.getStoreResultList().isEmpty() && uploadResult.getErrors().isEmpty()) {
            uploadResult.addError(getApiParamsError());
        }

        return uploadResult;
    }

    private Stream<CompletableFuture<ImageStoreResult>> getFutureStream(MultipartFile[] files, URL[] urlImages, String base64Images,
        ImageUploadResult uploadResult) {
        Stream<CompletableFuture<ImageStoreResult>> stream = Stream.empty();

        if (files != null) {
            stream = Stream.concat(Arrays.stream(files).map(storageService::store), stream);
        }

        if (urlImages != null) {
            int maxUrlFiles = appProperties.getMaxUrlFiles();
            if (urlImages.length > maxUrlFiles) {
                String message = messageSource.getMessage("imageStore.error.urlMaxFiles", new Object[]{maxUrlFiles}, Locale.getDefault());
                ImageStoreError error = new ImageStoreError(message, URL_MAX_FILES);
                uploadResult.addError(error);
            } else {
                stream = Stream.concat(Arrays.stream(urlImages).map(storageService::store), stream);
            }
        }

        if (StringUtils.isNotEmpty(base64Images)) {
            Base64Image[] images = convertStringToBase64Images(base64Images);
            Stream<CompletableFuture<ImageStoreResult>> base64Stream = IntStream.range(0, images.length)
                .mapToObj(id -> storageService.store(images[id], String.format("image %s", id + 1)));

            stream = Stream.concat(base64Stream, stream);
        }

        return stream;
    }

    private ImageStoreError getError(Exception e) {
        log.error(e.getMessage(), e);

        Throwable rootCause = ExceptionUtils.getRootCause(e);

        String message;
        ApiErrorCode errorCode = ApiErrorCode.SYSTEM_ERROR;
        if (rootCause instanceof ImageStoreException) {
            message = rootCause.getMessage();
            errorCode = ((ImageStoreException) rootCause).getErrorCode();
        } else {
            message = messageSource.getMessage("imageStore.error.system.no.file", null, Locale.getDefault());
        }

        return new ImageStoreError(message, errorCode);
    }

    private ImageStoreError getApiParamsError() {
        String message = messageSource.getMessage("imageStore.error.incorrectParams", null, Locale.getDefault());
        return new ImageStoreError(message, INCORRECT_API_PARAMS);
    }

    @SneakyThrows
    private Base64Image[] convertStringToBase64Images(String base64Images) {
        return objectMapper.readValue(base64Images, Base64Image[].class);
    }
}
