package com.major.project.crypto.task;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.major.project.crypto.module.Frames;
import com.major.project.crypto.module.VideoPaths;
import com.major.project.crypto.service.EncryptService;

@Component
@Getter
@Slf4j
public class EncryptionTask implements Tasklet {

    private final VideoPaths videoPaths;
    private final String encryptedFile;
    private final boolean sha256;
    private final EncryptService encrypt;
    private final Frames frames;
    private final Boolean showEncryptedVideo;
    private final String encryptedVid;
    HashMap<Integer, String> metadata = new HashMap<>();
    List<Mat> copiedFrames = new ArrayList<>();
    ArrayList<Mat> framesToDecrypt = new ArrayList<>();

    @Autowired
    public EncryptionTask(@Value("${video.output-encrypted}") String encryptedFile,
                          @Value("${encrypt.mode:true}") boolean sha256,
                          @Value("${video.output-encrypted}") String encryptedVid,
                          @Value("${video.show-encrypted:false}") Boolean showEncryptedVideo,
                          EncryptService encrypt,
                          Frames frames,
                          VideoPaths videoPaths) {
        this.encryptedFile = encryptedFile;
        this.sha256 = sha256;
        this.showEncryptedVideo = showEncryptedVideo;
        this.encryptedVid = encryptedVid;
        this.encrypt = encrypt;
        this.frames = frames;
        this.videoPaths = videoPaths;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        //TODO Either optimise the encryption logic
        // or use a different approach to handle large videos
        // or compress the videos before processing
        LOGGER.info("Encryption Started");
        OpenCV.loadLocally();

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
            framesToDecrypt.add(encrypted.clone());
            metadata.put(frameCount, keyHex);
            frameCount++;
        }
        videoCapture.release();
        frames.setFramesToDecrypt(framesToDecrypt);
        LOGGER.info("Frames queue size {}", frames.getFramesToDecrypt().size());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(encryptedFile.replaceAll("\\..*$", "") + ".json"), metadata);
        frames.setCopiedFrames(copiedFrames);
        LOGGER.info("Original Frames Copied");
        LOGGER.info("File is encrypted");

        if (showEncryptedVideo) {
            int width = framesToDecrypt.getFirst().cols();
            int height = framesToDecrypt.getFirst().rows();
            Size frameSize = new Size(width, height);

            int fourcc = VideoWriter.fourcc('f', 'f', 'v', '1');
            VideoWriter writer = new VideoWriter(encryptedVid, fourcc, 30.0, frameSize, true);
            for (Mat mat : framesToDecrypt) {
                writer.write(mat.clone());
            }
            writer.release();
            LOGGER.info("Encrypted Video created and stored as: {}", encryptedVid);
        }
        return RepeatStatus.FINISHED;
    }
}
