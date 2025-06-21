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

import com.major.project.crypto.module.VideoPaths;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@Slf4j
class BatchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VideoPaths videoPaths;

    @Mock
    private JobLauncher jobLauncher;

    @Mock
    private Job videoCryptographyJob;

    @InjectMocks
    private BatchController batchController;

    private final String decryptedVideoPath = "decrypted.mp4";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        batchController = new BatchController(decryptedVideoPath, videoPaths, jobLauncher, videoCryptographyJob);
        mockMvc = MockMvcBuilders.standaloneSetup(batchController).build();
    }

    @Test
    void testHomeReturnsIndex() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void testUploadVideo_Success() throws Exception {
        // Prepare a dummy video file (mock)
        MockMultipartFile mockFile = new MockMultipartFile(
                "videoFile",
                "testvideo.mp4",
                "video/mp4",
                "dummy content".getBytes()
        );

        // Spy on VideoPaths to verify setInputFilePath is called
        doNothing().when(videoPaths).setInputFilePath(anyString());

        mockMvc.perform(multipart("/upload").file(mockFile))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("showInputVideo", true))
                .andExpect(model().attribute("status", "Video uploaded successfully."));

        // Verify videoPaths.setInputFilePath was called with the saved path
        ArgumentCaptor<String> pathCaptor = ArgumentCaptor.forClass(String.class);
        verify(videoPaths).setInputFilePath(pathCaptor.capture());

        String savedPath = pathCaptor.getValue();
        assertTrue(savedPath.endsWith("testvideo.mp4"));

        // Clean up uploaded file
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
        // Mock job execution
        when(videoPaths.getInputFilePath()).thenReturn("/tmp/dummy.mp4");
        JobExecution jobExecution = mock(JobExecution.class);
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);

        when(jobLauncher.run(any(Job.class), any(JobParameters.class))).thenReturn(jobExecution);

        mockMvc.perform(post("/run"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("showInputVideo", true))
                .andExpect(model().attribute("status", BatchStatus.COMPLETED));
    }

    @Test
    void testStreamVideo_FileExists() throws Exception {
        // Create a dummy decrypted video file
        String tempDir = System.getProperty("java.io.tmpdir");
        Path path = Paths.get(tempDir, decryptedVideoPath);
        LOGGER.info("Creating dummy video file at: {}", path);
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

        when(videoPaths.getInputFilePath()).thenReturn(inputFilePath);

        mockMvc.perform(get("/videoInput"))
                .andExpect(status().isOk());

        Files.deleteIfExists(Paths.get(inputFilePath));
    }

    @Test
    void testStreamVideoInput_FileNotExists() throws Exception {
        when(videoPaths.getInputFilePath()).thenReturn("/non/existent/path.mp4");

        mockMvc.perform(get("/videoInput"))
                .andExpect(status().isNotFound());
    }
}
