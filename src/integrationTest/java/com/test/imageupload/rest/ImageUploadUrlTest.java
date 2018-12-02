package com.test.imageupload.rest;

import static com.test.imageupload.services.ErrorCode.FILE_NOT_EXIST;
import static com.test.imageupload.services.ErrorCode.URL_MAX_FILES;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.test.imageupload.config.AppProperties;
import java.net.URL;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

@RunWith(SpringRunner.class)
public class ImageUploadUrlTest extends ImageUploadCommonTest {

    @Autowired
    private AppProperties appProperties;

    @Test
    public void shouldUploadFiles() throws Exception {
        URL successJpgUrl = new URL("http://techslides.com/demos/samples/sample.jpg");
        URL successPngUrl = new URL("http://techslides.com/demos/samples/sample.png");
        URL successGifUrl = new URL("http://techslides.com/demos/samples/sample.gif");

        ResultActions perform = this.mockMvc.perform(multipart("/image")
            .param("urls", successJpgUrl.toString())
            .param("urls", successPngUrl.toString())
            .param("urls", successGifUrl.toString()));

        shouldUploadFilesExpect(perform, Arrays.asList(successJpgUrl.toString(), successPngUrl.toString(), successGifUrl.toString()));
    }

    @Test
    public void shouldNotUploadFilesWithIncorrectName() throws Exception {
        URL successJpgUrl = new URL("http://techslides.com/demos/samples/sample.jpg");

        ResultActions perform = this.mockMvc.perform(multipart("/image")
            .param("fake", successJpgUrl.toString()));

        shouldNotUploadWithIncorrectNameExpect(perform);
    }

    @Test
    public void shouldNotUpload404Files() throws Exception {
        URL url404 = new URL("http://techslides.com/demos/samples/sample404.jpg");

        this.mockMvc.perform(multipart("/image")
            .param("urls", url404.toString()))
            .andExpect(jsonPath("$.storeResultList", hasSize(0)))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].errorMessage").value(String.format("File %s not exists", url404.toString())))
            .andExpect(jsonPath("$.errors[0].errorCode").value(FILE_NOT_EXIST.name()));
    }

    @Test
    public void shouldNotUploadTooMuchFiles() throws Exception {
        URL successJpgUrl = new URL("http://techslides.com/demos/samples/sample.jpg");

        MockMultipartHttpServletRequestBuilder multipart = multipart("/image");

        for (int i = 1; i <= appProperties.getMaxUrlFiles() + 1; i++) {
            multipart.param("urls", successJpgUrl.toString());
        }

        this.mockMvc.perform(multipart)
            .andExpect(jsonPath("$.storeResultList", hasSize(0)))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].errorMessage").value(String.format("Maximum url files is %s", appProperties.getMaxUrlFiles())))
            .andExpect(jsonPath("$.errors[0].errorCode").value(URL_MAX_FILES.name()));
    }

    @Test
    public void shouldNotUploadFilesWithInvalidMediaType() throws Exception {
        URL videoUrl = new URL("http://techslides.com/demos/samples/sample.mov");

        ResultActions perform = this.mockMvc.perform(multipart("/image")
            .param("urls", videoUrl.toString()));

        shouldNotUploadWithInvalidMediaTypeExpect(perform, videoUrl.toString());
    }

    @Test
    public void shouldNotUploadLargeFile() throws Exception {
        URL largeJpgUrl = new URL("http://mirrors.standaloneinstaller.com/video-sample/page18-movie-4.3gp");

        ResultActions perform = this.mockMvc.perform(multipart("/image").param("urls", largeJpgUrl.toString()));
        shouldNotUploadLargeFileExpect(perform, largeJpgUrl.toString());
    }
}
