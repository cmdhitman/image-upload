package com.test.imageupload.data;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageStoreResult {

    private String fileName;

    private String internalFileName;

    private String thumbnailName;
}
