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
        LOGGER.info("Encryption Started");

        // Load OpenCV native library
        OpenCV.loadLocally();

        // Get the input video file path (should be set by BatchController correctly)
        String inputVideoPath = videoPaths.getInputFilePath();
        LOGGER.info("Reading video from path: {}", inputVideoPath);

        // Open the video file
        VideoCapture videoCapture = new VideoCapture(inputVideoPath);
        if (!videoCapture.isOpened()) {
            throw new RuntimeException("Cannot open video file at: " + inputVideoPath);
        }

        Mat frame = new Mat();
        String keyHex;
        byte[] keyHexBytes;
        int frameCount = 0;

        // Read frames and encrypt
        while (videoCapture.read(frame)) {
            copiedFrames.add(frame.clone()); // Store original frame

            // Generate key and encrypt frame
            keyHex = encrypt.generateHexKey(encrypt.frameToByte(frame), sha256);
            keyHexBytes = keyHex.getBytes(StandardCharsets.UTF_8);
            Mat encrypted = encrypt.encrypt(frame.clone(), keyHexBytes);

            // Add encrypted frame and metadata
            framesToDecrypt.add(encrypted.clone());
            metadata.put(frameCount, keyHex);
            frameCount++;
        }
        videoCapture.release();

        // Store the encrypted frames and original frames
        frames.setFramesToDecrypt(framesToDecrypt);
        frames.setCopiedFrames(copiedFrames);
        LOGGER.info("Total frames encrypted: {}", framesToDecrypt.size());

        // Write metadata to JSON file
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonFilePath = encryptedFile.replaceAll("\\..*$", "") + ".json";
        objectMapper.writeValue(new File(jsonFilePath), metadata);
        LOGGER.info("Metadata JSON written to: {}", jsonFilePath);

        // Optionally create encrypted video file
        if (showEncryptedVideo && !framesToDecrypt.isEmpty()) {
            int width = framesToDecrypt.getFirst().cols();
            int height = framesToDecrypt.getFirst().rows();
            Size frameSize = new Size(width, height);

            int fourcc = VideoWriter.fourcc('f', 'f', 'v', '1');
            VideoWriter writer = new VideoWriter(encryptedVid, fourcc, 30.0, frameSize, true);

            for (Mat mat : framesToDecrypt) {
                writer.write(mat.clone());
            }
            writer.release();
            LOGGER.info("Encrypted video created at: {}", encryptedVid);
        } else {
            LOGGER.warn("Encrypted video not created. Either disabled or no frames to encrypt.");
        }

        LOGGER.info("Encryption Task Completed Successfully.");
        return RepeatStatus.FINISHED;
    }

}
