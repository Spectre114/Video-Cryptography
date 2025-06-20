package com.major.project.crypto.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import com.major.project.crypto.module.VideoPaths;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {BatchTestConfig.class, TestConfig.class, VideoPaths.class})
@Slf4j
public class BatchApplicationTest {

    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;

    VideoPaths videoPaths;

    private final String inputFile;
    private final String outputVideoFile;
    private final String metadataFile;
    private final String encryptedVid;

    @Autowired
    public BatchApplicationTest(@Value("${video.input-file}") String inputFile,
                                @Value("${video.decrypted-video}") String outputVideoFile,
                                @Value("${video.output-encrypted}") String encryptedVid,
                                @Value("${video.output-encrypted}") String metadataFile,
                                VideoPaths videoPaths) {
        this.inputFile = inputFile;
        this.outputVideoFile = outputVideoFile;
        this.metadataFile = metadataFile + ".json";
        this.videoPaths = videoPaths;
        this.encryptedVid = encryptedVid;
    }

    @Test
    public void testBatchApplication() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        assertTrue(new File(outputVideoFile).exists());
        assertTrue(new File(metadataFile).exists());
    }

    @BeforeEach
    public void setUp() {
        videoPaths.setInputFilePath(inputFile);
    }

    @AfterEach
    @SneakyThrows
    public void cleanUpFiles() {
        deleteIfExists(outputVideoFile);
        deleteIfExists(metadataFile);
        deleteIfExists(encryptedVid);
    }

    private void deleteIfExists(String filePath) {

        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                LOGGER.error("Failed to delete: {}", filePath);
            }
        }
    }

    @SneakyThrows
    public void deleteDirectory(String directoryPath) {
        FileUtils.deleteDirectory(new File(directoryPath));
    }
}
