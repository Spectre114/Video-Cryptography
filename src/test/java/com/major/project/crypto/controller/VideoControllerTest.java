package com.major.project.crypto.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.major.project.crypto.module.VideoUtils;
import com.major.project.crypto.task.DecryptionTask;
import com.major.project.crypto.task.EncryptionTask;

@Slf4j
class VideoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VideoUtils videoUtils;

    @Mock
    private EncryptionTask encryptionTask;

    @Mock
    private DecryptionTask decryptionTask;

    @InjectMocks
    private VideoController videoController;

    private final String decryptedVideoPath = System.getProperty("java.io.tmpdir") + "/decrypted.mp4";
    private final String enryptedVideoPath = System.getProperty("java.io.tmpdir") + "/encrypted.mp4";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        videoController = new VideoController(decryptedVideoPath, enryptedVideoPath, videoUtils,  encryptionTask, decryptionTask);
        mockMvc = MockMvcBuilders.standaloneSetup(videoController).build();
    }

    @Test
    void testHomeReturnsIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void testUploadVideo_Success() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "videoFile",
                "testvideo.mp4",
                "video/mp4",
                "dummy content".getBytes()
        );

        doNothing().when(videoUtils).setInputFilePath(anyString());

        mockMvc.perform(multipart("/upload").file(mockFile))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("showInputVideo", true))
                .andExpect(model().attribute("status", "Video uploaded successfully."));

        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(videoUtils).setInputFilePath(pathCaptor.capture());

        String savedPath = pathCaptor.getValue();
        assertTrue(savedPath.endsWith("testvideo.mp4"));

        Files.deleteIfExists(Paths.get(savedPath));
    }

    @Test
    void testUploadVideo_Failure() throws Exception {
        // Mock MultipartFile that throws IOException on transferTo()
        MockMultipartFile mockFile = mock(MockMultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("failvideo.mp4");
        doThrow(new IOException("Simulated IO error")).when(mockFile).transferTo(any(File.class));

        mockMvc.perform(multipart("/upload").file(mockFile))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testRunBatch_Success() throws Exception {

        Path original = Files.createTempFile("original", ".mp4");
        Files.write(original, "dummy".getBytes());

        // Create decrypted file
        Path decrypted = Paths.get(decryptedVideoPath);
        Files.write(decrypted, "dummy".getBytes());

        when(videoUtils.getInputFilePath())
                .thenReturn(original.toString());

        mockMvc.perform(post("/run"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("showInputVideo", true))
                .andExpect(model().attribute("status", "COMPLETED"));

        Files.deleteIfExists(original);
        Files.deleteIfExists(decrypted);
    }

    @Test
    void testStreamVideo_FileExists() throws Exception {
        // Create a dummy decrypted video file
        Path path = Paths.get(decryptedVideoPath);
        Files.write(path, "dummy video content".getBytes());

        mockMvc.perform(get("/video"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "video/mp4"));

        Files.deleteIfExists(path);
    }

    @Test
    void testStreamVideo_FileNotExists() throws Exception {
        // Make sure file does not exist
        Files.deleteIfExists(Paths.get(decryptedVideoPath));

        mockMvc.perform(get("/video"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testStreamVideoInput_FileExists() throws Exception {
        // Setup mock input file path in VideoPaths
        String inputFilePath = System.getProperty("java.io.tmpdir") + "/inputvideo.mp4";
        Files.write(Paths.get(inputFilePath), "dummy input video".getBytes());

        when(videoUtils.getInputFilePath()).thenReturn(inputFilePath);

        mockMvc.perform(get("/videoInput"))
                .andExpect(status().isOk());

        Files.deleteIfExists(Paths.get(inputFilePath));
    }

    @Test
    void testStreamVideoInput_FileNotExists() throws Exception {
        when(videoUtils.getInputFilePath()).thenReturn("/non/existent/path.mp4");

        mockMvc.perform(get("/videoInput"))
                .andExpect(status().isNotFound());
    }
}
