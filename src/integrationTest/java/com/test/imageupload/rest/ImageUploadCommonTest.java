package com.test.imageupload.rest;

import static com.test.imageupload.services.ApiErrorCode.FILE_TOO_LARGE;
import static com.test.imageupload.services.ApiErrorCode.INCORRECT_API_PARAMS;
import static com.test.imageupload.services.ApiErrorCode.INVALID_FILE_FORMAT;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.test.imageupload.Application;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class ImageUploadCommonTest {

    @Autowired
    protected MockMvc mockMvc;

    void shouldUploadFilesExpect(ResultActions actions, List<String> names) throws Exception {
        ResultActions resultActions = actions.andExpect(jsonPath("$.storeResultList", hasSize(3)))
            .andExpect(jsonPath("$.errors", hasSize(0)));

        for (int i = 0; i < names.size(); i++) {
            resultActions.andExpect(jsonPath("$.storeResultList[" + i + "].fileName").value(names.get(i)))
                .andExpect(jsonPath("$.storeResultList[" + i + "].internalFileName").exists())
                .andExpect(jsonPath("$.storeResultList[" + i + "].thumbnailName").exists());
        }
    }

    void shouldNotUploadWithIncorrectNameExpect(ResultActions resultActions) throws Exception {
        resultActions
            .andExpect(jsonPath("$.storeResultList", hasSize(0)))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].errorMessage").value("Request files is empty. Check api parameters are valid"))
            .andExpect(jsonPath("$.errors[0].errorCode").value(INCORRECT_API_PARAMS.name()));
    }

    void shouldNotUploadWithInvalidMediaTypeExpect(ResultActions resultActions, String name) throws Exception {
        resultActions.andExpect(jsonPath("$.storeResultList", hasSize(0)))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].errorMessage")
                .value(String.format("Media type video/quicktime incorrect for file %s. Allowed types are image/gif, image/jpeg, image/png", name)))
            .andExpect(jsonPath("$.errors[0].errorCode").value(INVALID_FILE_FORMAT.name()));
    }

    void shouldNotUploadLargeFileExpect(ResultActions resultActions, String name) throws Exception {
        resultActions
            .andExpect(jsonPath("$.storeResultList", hasSize(0)))
            .andExpect(jsonPath("$.errors", hasSize(1)))
            .andExpect(jsonPath("$.errors[0].errorMessage").value("Request file is too large. Max file size is 5MB, max request size is 20MB"))
            .andExpect(jsonPath("$.errors[0].errorCode").value(FILE_TOO_LARGE.name()));
    }
}
