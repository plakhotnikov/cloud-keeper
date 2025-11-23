package com.plakhotnikov.cloudkeeper.storage;

import com.plakhotnikov.cloudkeeper.storage.model.Breadcrumb;
import com.plakhotnikov.cloudkeeper.storage.model.dto.BaseReqDto;
import com.plakhotnikov.cloudkeeper.storage.service.MinioService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.plakhotnikov.cloudkeeper.storage.MultipartFilesUtil.generateMockMultipartFiles;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
@DisplayName("Testing objects manipulations requests")
public class ObjectAndSearchControllersTest extends CompleteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MinioService minioService;

    private final static long USER_ID = 1L;
    private static final String NEW_OBJECT_NAME = "newObjectName";
    private static final String FOLDER_NAME = "folderName";
    private static final String SEARCH_QUERY = "jectN";
    private final MockMultipartFile mockSingleFile = (MockMultipartFile) generateMockMultipartFiles(1).get(0);
    private final String newObjNameWithExt = NEW_OBJECT_NAME + "." + mockSingleFile.getOriginalFilename().split("\\.")[1];

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Testing correct requests performing")
    class ProperRequestsTest {
        @Test
        @Order(1)
        @WithUserDetails()
        @DisplayName("Uploading file -> 302 redirect and object exists")
        public void uploadFile() throws Exception {

            mockMvc.perform(multipart("/files")
                            .file(mockSingleFile)
                            .param("path", "")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/"));

            assertTrue(minioService.isObjectExist(new BaseReqDto(USER_ID, "", mockSingleFile.getOriginalFilename())));
        }

        @Test
        @Order(2)
        @WithUserDetails()
        @DisplayName("Downloading  file-> 200 ok and content type == octet stream")
        public void downloadFile() throws Exception {

            mockMvc.perform(get("/files")
                            .param("path", "")
                            .param("objName", mockSingleFile.getOriginalFilename())
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
        }

        @Test
        @Order(3)
        @WithUserDetails()
        @DisplayName("Renaming file -> 302 redirect, file with new name exists, with old - does not")
        public void renameFile() throws Exception {

            mockMvc.perform(patch("/files")
                            .param("path", "")
                            .param("objName", mockSingleFile.getOriginalFilename())
                            .param("newObjName", NEW_OBJECT_NAME)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection());

            assertFalse(minioService.isObjectExist(new BaseReqDto(USER_ID, "", mockSingleFile.getOriginalFilename())));
            assertTrue(minioService.isObjectExist(new BaseReqDto(USER_ID, "", NEW_OBJECT_NAME + ".txt")));
        }

        @Test
        @Order(4)
        @WithUserDetails()
        @DisplayName("Searching for file by part of its name -> 200 ok and previously renamed file is listed")
        public void searchFiles() throws Exception {
            MvcResult mvcResult = mockMvc.perform(get("/search")
                            .param("query", SEARCH_QUERY)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(view().name("storage/search"))
                    .andExpect(model().attributeExists("breadcrumbs"))
                    .andReturn();

            List<Breadcrumb> foundObjects = extractFoundBreadcrumbs(mvcResult);
            assertFalse(foundObjects.isEmpty());
            assertEquals(newObjNameWithExt, foundObjects.get(0).getLastPart());
        }


        @Test
        @Order(5)
        @WithUserDetails()
        @DisplayName("Deleting file -> 302 redirection and file does not exist")
        public void deleteFile() throws Exception {

            mockMvc.perform(delete("/files")
                            .param("path", "")
                            .param("objName", newObjNameWithExt)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection());

            assertFalse(minioService.isObjectExist(new BaseReqDto(USER_ID, "", newObjNameWithExt)));
        }

        @Test
        @Order(6)
        @WithUserDetails()
        @DisplayName("Creating a folder -> 302 redirection and folder exists")
        public void createFolder() throws Exception {

            mockMvc.perform(post("/files/create")
                            .param("path", "")
                            .param("objName", FOLDER_NAME)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection());

            assertTrue(minioService.isDir(new BaseReqDto(USER_ID, "", FOLDER_NAME)));
        }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("Testing unacceptable requests performing")
    class UnacceptableRequestsTest {
        @Test
        @Order(1)
        @WithUserDetails()
        @DisplayName("Downloading non-existent file -> 400 bad request")
        public void downloadNonExistentFile() throws Exception {

            mockMvc.perform(get("/files")
                            .param("path", "")
                            .param("objName", "nonExistentFileName")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @Order(2)
        @WithUserDetails()
        @DisplayName("Renaming with invalid params non-existent file -> 400 bad request")
        public void renameFile_withInvalidParams() throws Exception {

            // request is missing objName parameter
            mockMvc.perform(patch("/files")
                            .param("path", "")
                            .param("newObjName", NEW_OBJECT_NAME)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

        }


        @Test
        @Order(3)
        @DisplayName("Trying to upload when user is anonymous -> 302 redirect to login")
        public void uploadFile_whenAnonymousUSer() throws Exception {

            mockMvc.perform(multipart("/files")
                            .file(mockSingleFile)
                            .param("path", "")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlPattern("**/welcome"));

        }

        @Test
        @Order(4)
        @WithUserDetails()
        @DisplayName("Duplicating folder -> 409 conflict")
        public void createFolder_withNameOfExistingFolder() throws Exception {

            mockMvc.perform(post("/files/create")
                            .param("path", "")
                            .param("objName", FOLDER_NAME)
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }


    @SuppressWarnings("unchecked")
    private static List<Breadcrumb> extractFoundBreadcrumbs(MvcResult mvcResult) {
        ModelAndView modelAndView = mvcResult.getModelAndView();
        if (modelAndView != null) {
            Map<String, Object> model = modelAndView.getModel();
            try {
                return (List<Breadcrumb>) model.get("breadcrumbs");
            } catch (ClassCastException e) {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }


}
