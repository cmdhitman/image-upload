package com.test.imageupload.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents base64 image
 *
 * @author Vladimir Moiseev
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Base64Image {

    /**
     * Encoded image string
     */
    private String base64Image;
}


