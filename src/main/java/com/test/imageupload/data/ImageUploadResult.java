package com.test.imageupload.data;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ImageUploadResult {

    private List<ImageStoreResult> storeResultList = new ArrayList<>();

    private List<ImageStoreError> errors = new ArrayList<>();

    public void addStoreResult(ImageStoreResult result) {
        this.storeResultList.add(result);
    }

    public void addError(ImageStoreError error) {
        this.errors.add(error);
    }
}
