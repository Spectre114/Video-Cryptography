package com.major.project.crypto.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@Slf4j
public class BatchApplicationTest {

    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;

    private final String outputVideoFile;
    private final String metadataFile;
    private final String encryptedImageDir;
    private final String decryptedFrameDir;

    public BatchApplicationTest(@Value("${video.decrypted-video}") String outputVideoFile,
                                @Value("${video.output-encrypted}") String metadataFile,
                                @Value("${video.ecryptedImageDir}") String encryptedImageDir,
                                @Value("${video.decryptedFrameDir}") String decryptedFrameDir) {
        this.outputVideoFile = outputVideoFile;
        this.metadataFile = metadataFile + ".json";
        this.encryptedImageDir = encryptedImageDir;
        this.decryptedFrameDir = decryptedFrameDir;
    }

    @Test
    public void testBatchApplication() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        assertNotNull(new File(outputVideoFile));
        assertNotNull(new File(metadataFile));

    }

    @AfterEach
    @SneakyThrows
    public void cleanUpFiles() {
        deleteIfExists(outputVideoFile);
        deleteIfExists(metadataFile);
        deleteDirectory(encryptedImageDir);
        deleteDirectory(decryptedFrameDir);
    }

    @AfterAll
    public static void cleanUp() {}
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
