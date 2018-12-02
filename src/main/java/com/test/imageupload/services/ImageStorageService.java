package com.test.imageupload.services;

import static com.test.imageupload.services.ApiErrorCode.FILE_TOO_LARGE;

import com.test.imageupload.config.AppProperties;
import com.test.imageupload.data.Base64Image;
import com.test.imageupload.data.ImageStoreResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Image store service
 *
 * @author Vladimir Moiseev
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageStorageService {

    private final AppProperties appProperties;
    private final MessageSource messageSource;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size}")
    private String maxRequestSize;

    @PostConstruct
    @SneakyThrows
    public void init() {
        Files.createDirectories(appProperties.getPhotoDir());
    }

    /**
     * Store multipart file
     *
     * @param file File
     * @return Async result
     * @throws ImageStoreException e
     */
    @Async
    public CompletableFuture<ImageStoreResult> store(MultipartFile file) {
        try {
            return storeImage(file.getBytes(), file.getOriginalFilename());
        } catch (ImageStoreException e) {
            throw e;
        } catch (Exception e) {
            throw getSystemError(file.getOriginalFilename(), e);
        }
    }

    /**
     * Store file from url
     *
     * @param url URL
     * @return Async result
     * @throws ImageStoreException e
     */
    @Async
    public CompletableFuture<ImageStoreResult> store(URL url) {
        try {
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(appProperties.getConnectTimeoutMs());
            conn.setReadTimeout(appProperties.getReadTimeoutMs());
            conn.connect();

            @Cleanup InputStream inputStream = conn.getInputStream();
            byte[] bytes = IOUtils.toByteArray(inputStream);

            return storeImage(bytes, url.toString());
        } catch (ImageStoreException e) {
            throw e;
        } catch (SocketTimeoutException e) {
            String message = messageSource.getMessage("imageStore.error.urlReadTimeout", new Object[]{url.toString()}, Locale.getDefault());
            throw new ImageStoreException(message, ApiErrorCode.URL_READ_TIMEOUT);
        } catch (FileNotFoundException e) {
            String message = messageSource.getMessage("imageStore.error.urlFileNotExist", new Object[]{url.toString()}, Locale.getDefault());
            throw new ImageStoreException(message, ApiErrorCode.FILE_NOT_EXIST);
        } catch (Exception e) {
            throw getSystemError(url.toString(), e);
        }
    }

    /**
     * Store file from base64 image
     *
     * @param image Base64 image
     * @param fileName image file name
     * @return Async result
     * @throws ImageStoreException e
     */
    @Async
    public CompletableFuture<ImageStoreResult> store(Base64Image image, String fileName) {
        try {
            if (image.getBase64Image() == null) {
                String message = messageSource.getMessage("imageStore.error.base64FormatJson", new Object[]{fileName}, Locale.getDefault());
                throw new ImageStoreException(message, ApiErrorCode.BASE64_JSON_FORMAT);
            }

            byte[] bytes = Base64.getDecoder().decode(image.getBase64Image());

            return storeImage(bytes, fileName);
        } catch (ImageStoreException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            String message = messageSource.getMessage("imageStore.error.base64SchemeInvalid", new Object[]{fileName}, Locale.getDefault());
            throw new ImageStoreException(message, ApiErrorCode.BASE64_SCHEME_INVALID);
        } catch (Exception e) {
            throw getSystemError(fileName, e);
        }
    }

    @SneakyThrows
    private String getPhotoExtension(byte[] bytes, String fileName) {
        Tika tika = new Tika();
        String mediaType = tika.detect(bytes);

        log.info("detect mediaType {} for file {}", mediaType, fileName);

        if (!appProperties.getAllowedMimeTypes().contains(mediaType)) {
            String allowedMimes = String.join(", ", appProperties.getAllowedMimeTypes());
            String message = messageSource.getMessage("imageStore.error.fileFormat", new Object[]{mediaType, fileName, allowedMimes}, Locale.getDefault());

            throw new ImageStoreException(message, ApiErrorCode.INVALID_FILE_FORMAT);
        }

        MimeType mimeType = MimeTypes.getDefaultMimeTypes().forName(mediaType);

        return mimeType.getExtension();
    }

    @SneakyThrows
    private CompletableFuture<ImageStoreResult> storeImage(byte[] bytes, String originalFilename) {
        if (bytes.length == 0) {
            String message = messageSource.getMessage("imageStore.error.fileEmpty", new Object[]{originalFilename}, Locale.getDefault());
            throw new ImageStoreException(message, ApiErrorCode.FILE_EMPTY);
        }

        if (bytes.length > appProperties.getMaxFileSizeInBytes()) {
            String message = messageSource.getMessage("imageStore.error.fileIsTooLarge", new Object[]{maxFileSize, maxRequestSize}, Locale.getDefault());
            throw new ImageStoreException(message, FILE_TOO_LARGE);
        }

        String ext = getPhotoExtension(bytes, originalFilename);
        String internalFileName = UUID.randomUUID().toString() + ext;

        Path path = Paths.get(appProperties.getPhotoDir().toString(), internalFileName);
        Files.write(path, bytes, StandardOpenOption.CREATE_NEW);

        log.info("new image upload file name = {}, stored file name = {}", originalFilename, internalFileName);

        String thumbName = makeThumbnail(path);

        ImageStoreResult result = new ImageStoreResult(originalFilename, internalFileName, thumbName);

        return CompletableFuture.completedFuture(result);
    }

    @SneakyThrows
    private String makeThumbnail(Path path) {
        File file = path.toFile();

        String name = file.getName();
        String id = StringUtils.substringBefore(name, ".");

        String thumbName = name.replaceFirst(id, id + appProperties.getThumbnailSuffix());
        Path thumbPath = Paths.get(appProperties.getPhotoDir().toString(), name.replaceFirst(id, thumbName));

        Thumbnails.of(file).size(appProperties.getThumbnailWidth(), appProperties.getThumbnailHeight()).toFile(thumbPath.toFile());

        return thumbName;
    }

    private ImageStoreException getSystemError(String fileName, Exception e) {
        log.error(e.getMessage(), e);

        String message = messageSource.getMessage("imageStore.error.system", new Object[]{fileName}, Locale.getDefault());
        return new ImageStoreException(message, ApiErrorCode.SYSTEM_ERROR);
    }
}
