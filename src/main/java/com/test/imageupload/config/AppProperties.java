package com.test.imageupload.config;

import java.nio.file.Path;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties("app")
public class AppProperties {

    private Path photoDir;

    private List<String> allowedMimeTypes;

    private int connectTimeoutMs;

    private int readTimeoutMs;

    private int maxFileSizeInMB;

    private int maxRequestSizeInMB;

    private int maxUrlFiles;

    private int thumbnailWidth;

    private int thumbnailHeight;

    private String thumbnailSuffix;

    public int getMaxFileSizeInBytes() {
        return maxFileSizeInMB * 1024 * 1024;
    }
}
