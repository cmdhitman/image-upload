package com.test.imageupload.rest;

import static com.test.imageupload.services.ApiErrorCode.FILE_EMPTY;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.Arrays;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;

@RunWith(SpringRunner.class)
public class ImageUploadFileTest extends ImageUploadCommonTest {

    @Value("classpath:test_success.jpg")
    private Resource successImageJpg;

    @Value("classpath:test_success.png")
    private Resource successImagePng;

    @Value("classpath:test_success.gif")
    private Resource successImageGif;

    @Value("classpath:test_success_no_ext")
    private Resource successImageEmptyExt;

    @Value("classpath:test_incorrect_media.jpg")
    private Resource incorrectMedia;

    @Value("classpath:test_empty.jpg")
    private Resource emptyImage;

    @Value("classpath:test_large.jpg")
    private Resource largeImage;

    @Test
    public void shouldUploadFiles() throws Exception {
        MockMultipartFile jpgFile = getMockFile(successImageJpg);
        MockMultipartFile pngFile = getMockFile(successImagePng);
        MockMultipartFile gifFile = getMockFile(successImageGif);

        ResultActions perform = this.mockMvc.perform(multipart("/image").file(jpgFile).file(pngFile).file(gifFile));
        shouldUploadFilesExpect(perform, Arrays.asList(jpgFile.getOriginalFilename(), pngFile.getOriginalFilename(), gifFile.getOriginalFilename()));
    }

    @Test
    public void shouldUploadWithEmptyExtension() throws Exception {
        MockMultipartFile jpg = getMockFile(successImageEmptyExt);

        this.mockMvc.perform(multipart("/image").file(jpg))
            .andExpect(jsonPath("$.storeResultList", hasSize(1)))
            .andExpect(jsonPath("$.storeResultList[0].fileName").value(jpg.getOriginalFilename()))
            .andExpect(jsonPath("$.errors", hasSize(0)));
    }

    @Test
    public void shouldNotUploadFilesWithIncorrectName() throws Exception {
        MockMultipartFile jpgFile = getMockFile(successImageJpg, "notValidName");

        ResultActions perform = this.mockMvc.perform(multipart("/image").file(jpgFile));
        shouldNotUploadWithIncorrectNameExpect(perform);
    }

    @Test
    public void shouldNotUploadFilesWithInvalidMediaType() throws Exception {
        MockMultipartFile incorrectFile = getMockFile(incorrectMedia);

        ResultActions perform = this.mockMvc.perform(multipart("/image").file(incorrectFile));
        shouldNotUploadWithInvalidMediaTypeExpect(perform, incorrectFile.getOriginalFilename());
    }

    @Test
    public void shouldNotUploadEmptyFile() throws Exception {
        MockMultipartFile empty = getMockFile(emptyImage);

        this.mockMvc.perform(multipart("/image").file(empty))
            .andExpect(jsonPath("$.storeResultList", hasSize(0)))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].errorMessage").value(String.format("File %s is empty", empty.getOriginalFilename())))
            .andExpect(jsonPath("$.errors[0].errorCode").value(FILE_EMPTY.name()));
    }

    @Test
    public void shouldNotUploadLargeFile() throws Exception {
        MockMultipartFile largeFile = getMockFile(largeImage);

        ResultActions perform = this.mockMvc.perform(multipart("/image").file(largeFile));
        shouldNotUploadLargeFileExpect(perform, largeFile.getOriginalFilename());
    }

    private MockMultipartFile getMockFile(Resource imageResource) {
        return getMockFile(imageResource, "files");
    }

    @SneakyThrows
    private MockMultipartFile getMockFile(Resource imageResource, String name) {
        byte[] imageBytes = IOUtils.toByteArray(imageResource.getInputStream());
        String filename = imageResource.getFilename();
        return new MockMultipartFile(name, filename, null, imageBytes);
    }
}
