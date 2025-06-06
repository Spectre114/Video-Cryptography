package com.major.project.crypto.task;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.major.project.crypto.module.VideoPaths;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.major.project.crypto.module.Frames;
import com.major.project.crypto.service.EncryptService;

@Component
@Getter
@Slf4j
public class EncryptionTask implements Tasklet {

    VideoPaths videoPaths;
    private final String encryptedFile;
    private final boolean sha256;
    private final String encryptedImageDir;
    EncryptService encrypt;
    Frames frames;
    HashMap<Integer, String> metadata = new HashMap<>();
    List<Mat> copiedFrames = new ArrayList<>();

    @Autowired
    public EncryptionTask(@Value("${video.output-encrypted}") String encryptedFile,
                          @Value("${encrypt.mode:true}") boolean sha256,
                          @Value("${video.encryptedImageDir}") String encryptedImageDir,
                          EncryptService encrypt,
                          Frames frames,
                          VideoPaths videoPaths) {
        this.encryptedFile = encryptedFile;
        this.sha256 = sha256;
        this.encrypt = encrypt;
        this.encryptedImageDir = encryptedImageDir;
        this.frames = frames;
        this.videoPaths = videoPaths;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        LOGGER.info("Encryption Started");
        OpenCV.loadLocally();
        new File(encryptedImageDir).mkdirs();

        VideoCapture videoCapture = new VideoCapture(videoPaths.getInputFilePath());
        Mat frame = new Mat();

        String keyHex;
        byte[] keyHexBytes;
        int frameCount = 0;
        while(videoCapture.read(frame)) {
            copiedFrames.add(frame.clone());
            keyHex = encrypt.generateHexKey(encrypt.frameToByte(frame), sha256);

            keyHexBytes =keyHex.getBytes(StandardCharsets.UTF_8);
            Mat encrypted = encrypt.encrypt(frame.clone(), keyHexBytes);
            String encryptedPath = encryptedImageDir + String.format("frame_%04d.png", frameCount);
            Imgcodecs.imwrite(encryptedPath, encrypted);
            metadata.put(frameCount, keyHex);
            frameCount++;
        }
        videoCapture.release();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(encryptedFile + ".json"), metadata);
        frames.setCopiedFrames(copiedFrames);
        LOGGER.info("Original Frames Copied");
        LOGGER.info("File is encrypted");
        return RepeatStatus.FINISHED;
    }
}
