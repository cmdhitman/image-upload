package com.test.imageupload.services;

import com.test.imageupload.config.AppProperties;
import com.test.imageupload.data.ImageStoreResult;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Paths;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ImageStorageServiceTest {

    @Value("classpath:test_success.jpg")
    private Resource successImageJpg;

    @Autowired
    private ImageStorageService service;

    @Autowired
    private AppProperties appProperties;

    @Test
    public void storeFileTest() throws Exception {
        byte[] bytes = IOUtils.toByteArray(successImageJpg.getInputStream());
        MockMultipartFile file = new MockMultipartFile("files", successImageJpg.getFilename(), null, bytes);

        ImageStoreResult result = service.store(file).join();

        Assert.assertEquals(successImageJpg.getFilename(), result.getFileName());

        File internalFile = Paths.get(appProperties.getPhotoDir().toString(), result.getInternalFileName()).toFile();
        Assert.assertTrue(internalFile.exists());

        File internalFileThumbnail = Paths.get(appProperties.getPhotoDir().toString(), result.getThumbnailName()).toFile();
        Assert.assertTrue(internalFileThumbnail.exists());

        byte[] bytesOfInternal = IOUtils.toByteArray(new FileInputStream(internalFile));

        Assert.assertTrue(result.getInternalFileName().endsWith(".jpg"));
        Assert.assertTrue(result.getThumbnailName().endsWith(".jpg"));


        Assert.assertArrayEquals(bytes, bytesOfInternal);
    }
}
