package com.test.imageupload.services;

import com.test.imageupload.config.AppProperties;
import com.test.imageupload.data.Base64Image;
import com.test.imageupload.data.ImageStoreResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.util.Base64;
import lombok.Cleanup;
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
        commonAssert(result, bytes, successImageJpg.getFilename());
    }

    @Test
    public void storeUrlFileTest() throws Exception {
        URL successJpgUrl = new URL("http://techslides.com/demos/samples/sample.jpg");

        ImageStoreResult imageStoreResult = service.store(successJpgUrl).join();

        URLConnection conn = successJpgUrl.openConnection();
        conn.connect();

        @Cleanup InputStream inputStream = conn.getInputStream();
        byte[] bytes = IOUtils.toByteArray(inputStream);

        commonAssert(imageStoreResult, bytes, successJpgUrl.toString());
    }

    @Test
    public void storeBase64FileTest() throws Exception {
        byte[] bytes = IOUtils.toByteArray(successImageJpg.getInputStream());
        String encoded = Base64.getEncoder().encodeToString(bytes);

        Base64Image image = new Base64Image(encoded);

        ImageStoreResult imageStoreResult = service.store(image, "image 1").join();
        commonAssert(imageStoreResult, bytes, "image 1");
    }

    private void commonAssert(ImageStoreResult result, byte[] bytes, String fileName) throws Exception {
        Assert.assertEquals(fileName, result.getFileName());

        File internalFile = Paths.get(appProperties.getPhotoDir().toString(), result.getInternalFileName()).toFile();
        Assert.assertTrue(internalFile.exists());

        File internalFileThumbnail = Paths.get(appProperties.getPhotoDir().toString(), result.getThumbnailName()).toFile();
        Assert.assertTrue(internalFileThumbnail.exists());

        byte[] bytesOfInternal = IOUtils.toByteArray(new FileInputStream(internalFile));
        Assert.assertArrayEquals(bytes, bytesOfInternal);

        Assert.assertTrue(result.getInternalFileName().endsWith(".jpg"));
        Assert.assertTrue(result.getThumbnailName().endsWith(".jpg"));
    }
}
