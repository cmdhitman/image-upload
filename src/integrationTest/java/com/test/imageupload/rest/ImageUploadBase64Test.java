package com.test.imageupload.rest;

import static com.test.imageupload.services.ErrorCode.BASE64_JSON_FORMAT;
import static com.test.imageupload.services.ErrorCode.BASE64_SCHEME_INVALID;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.imageupload.data.Base64Image;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;

@RunWith(SpringRunner.class)
public class ImageUploadBase64Test extends ImageUploadCommonTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Value("classpath:test_success.jpg")
    private Resource successImageJpg;

    @Value("classpath:test_success.png")
    private Resource successImagePng;

    @Value("classpath:test_success.gif")
    private Resource successImageGif;

    @Value("classpath:test_incorrect_media.jpg")
    private Resource incorrectMedia;

    @Value("classpath:test_large.jpg")
    private Resource largeImage;

    @Test
    public void shouldUploadFiles() throws Exception {
        List<Base64Image> images = Arrays.asList(getBase64Image(successImageJpg), getBase64Image(successImagePng), getBase64Image(successImageGif));

        ResultActions perform = this.mockMvc.perform(post("/image").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(images)));

        shouldUploadFilesExpect(perform, Arrays.asList("image 1", "image 2", "image 3"));
    }

    @Test
    public void shouldNotUploadFilesWithInvalidMediaType() throws Exception {
        List<Base64Image> images = Collections.singletonList(getBase64Image(incorrectMedia));

        ResultActions perform = this.mockMvc.perform(post("/image").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(images)));

        shouldNotUploadWithInvalidMediaTypeExpect(perform, "image 1");
    }

    @Test
    public void shouldNotUploadEmpty() throws Exception {
        List<Base64Image> images = Collections.emptyList();

        ResultActions perform = this.mockMvc.perform(post("/image").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(images)));

        shouldNotUploadWithIncorrectNameExpect(perform);
    }

    @Test
    public void shouldNotUploadLargeFile() throws Exception {
        List<Base64Image> images = Collections.singletonList(getBase64Image(largeImage));

        ResultActions perform = this.mockMvc.perform(post("/image").contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(images)));

        shouldNotUploadLargeFileExpect(perform, "image 1");
    }

    @Test
    public void shouldNotUploadWithObjectNameError() throws Exception {
        List<Base64Image> images = Collections.singletonList(getBase64Image(successImageJpg));

        String content = objectMapper.writeValueAsString(images);

        content = content.replaceFirst("base64Image", "base64");

        this.mockMvc.perform(post("/image").contentType(MediaType.APPLICATION_JSON)
            .content(content)).andExpect(jsonPath("$.storeResultList", hasSize(0)))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].errorMessage").value(String.format("Please check json structure syntax for file %s", "image 1")))
            .andExpect(jsonPath("$.errors[0].errorCode").value(BASE64_JSON_FORMAT.name()));
    }

    @Test
    public void shouldNotUploadWithBase64Invalid() throws Exception {
        List<Base64Image> images = Collections.singletonList(getBase64Image(successImageJpg));

        String content = objectMapper.writeValueAsString(images);

        content = content.replaceFirst("4AAQS", "****");

        this.mockMvc.perform(post("/image").contentType(MediaType.APPLICATION_JSON)
            .content(content)).andExpect(jsonPath("$.storeResultList", hasSize(0)))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].errorMessage").value(String.format("Please check file %s for base64 valid", "image 1")))
            .andExpect(jsonPath("$.errors[0].errorCode").value(BASE64_SCHEME_INVALID.name()));
    }

    private Base64Image getBase64Image(Resource image) throws IOException {
        byte[] bytes = IOUtils.toByteArray(image.getInputStream());
        String encoded = Base64.getEncoder().encodeToString(bytes);

        return new Base64Image(encoded);
    }
}
