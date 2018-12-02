package com.test.imageupload.data;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents image store result
 *
 * @author Vladimir Moiseev
 */
@Data
@AllArgsConstructor
public class ImageStoreResult {

    /**
     * Original file name
     */
    private String fileName;

    /**
     * Internal file name
     */
    private String internalFileName;

    /**
     * Thumbnail
     */
    private String thumbnailName;
}
