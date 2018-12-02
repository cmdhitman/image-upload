package com.test.imageupload.data;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Represents upload result
 *
 * @author Vladimir Moiseev
 */
@Data
public class ImageUploadResult {

    /**
     * Success stored images
     */
    private List<ImageStoreResult> storeResultList = new ArrayList<>();

    /**
     * Errors if any while storing images
     */
    private List<ImageStoreError> errors = new ArrayList<>();

    public void addStoreResult(ImageStoreResult result) {
        this.storeResultList.add(result);
    }

    public void addError(ImageStoreError error) {
        this.errors.add(error);
    }
}
