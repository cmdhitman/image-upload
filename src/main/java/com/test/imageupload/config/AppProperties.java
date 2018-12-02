package com.test.imageupload.config;

import java.nio.file.Path;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Common image properties
 *
 * @author Vladimir Moiseev
 */
@Component
@Getter
@Setter
@ConfigurationProperties("app")
public class AppProperties {

    /**
     * Where to store files
     */
    private Path photoDir;

    /**
     * Mime types
     */
    private List<String> allowedMimeTypes;

    /**
     * URL connect timeout
     */
    private int connectTimeoutMs;

    /**
     * URL read timeout
     */
    private int readTimeoutMs;

    /**
     * Maximum file size
     */
    private int maxFileSizeInMB;

    /**
     * Maximum request size
     */
    private int maxRequestSizeInMB;

    /**
     * Maximum url files
     */
    private int maxUrlFiles;

    /**
     * Thumbnail with in px
     */
    private int thumbnailWidth;

    /**
     * Thumbnail height in px
     */
    private int thumbnailHeight;

    /**
     * Thumbnail suffix
     */
    private String thumbnailSuffix;

    public int getMaxFileSizeInBytes() {
        return maxFileSizeInMB * 1024 * 1024;
    }
}
