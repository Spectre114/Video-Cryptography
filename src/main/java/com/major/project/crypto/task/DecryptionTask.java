package com.major.project.crypto.task;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoWriter;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.major.project.crypto.module.Frames;
import com.major.project.crypto.service.DecryptService;

@Slf4j
@Component
public class DecryptionTask implements Tasklet {
    HashMap<Integer, String> metadataMap = new HashMap<>();

    private final String encryptedFrameDir;
    private final String decryptedFrameDir;
    private final String encryptedFile;
    private final String decryptedVideo;
    Frames frames;
    DecryptService decrypt;
    List<Mat> decryptedFrames = new ArrayList<>();

    @Autowired
    public DecryptionTask(DecryptService decrypt,
                          @Value("${video.output-encrypted}") String encryptedFile,
                          @Value("${video.decrypted-video}") String decryptedVideo,
                          @Value("${video.ecryptedImageDir}") String encryptedFrameDir,
                          @Value("${video.decryptedFrameDir}") String decryptedFrameDir,
                          Frames frames) {
        this.decrypt = decrypt;
        this.encryptedFrameDir = encryptedFrameDir;
        this.encryptedFile = encryptedFile;
        this.decryptedVideo = decryptedVideo;
        this.decryptedFrameDir = decryptedFrameDir;
        this.frames = frames;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        LOGGER.info("Decryption Started");
        OpenCV.loadLocally();
        try {
            ObjectMapper mapper = new ObjectMapper();
            metadataMap = mapper.readValue(new File(encryptedFile + ".json"),
                    mapper.getTypeFactory().constructMapType(HashMap.class, Integer.class, String.class));
        } catch (IOException e) {
            e.printStackTrace();
            return RepeatStatus.FINISHED;
        }

        new File(decryptedFrameDir).mkdirs();

        File dir = new File(encryptedFrameDir);
        File[] imageFiles = dir.listFiles((d, name) -> name.endsWith(".png") || name.endsWith(".bmp") || name.endsWith(".jpg"));

        Arrays.sort(imageFiles, Comparator.comparing(File::getName));

        Mat firstImage = Imgcodecs.imread(imageFiles[0].getAbsolutePath());
        int width = firstImage.cols();
        int height = firstImage.rows();
        Size frameSize = new Size(width, height);

        int fourcc = VideoWriter.fourcc('f', 'f', 'v', '1');
        VideoWriter writer = new VideoWriter(decryptedVideo, fourcc, 30.0, frameSize, true);

        for (int i = 0; metadataMap.containsKey(i); i++) {
            String path = encryptedFrameDir + String.format("frame_%04d.png", i);
            Mat encryptedFrame = Imgcodecs.imread(path);
            byte[] keyHexBytes = metadataMap.get(i).getBytes(StandardCharsets.UTF_8);

            Mat decrypted = decrypt.decryptFrame(encryptedFrame, keyHexBytes);
            decryptedFrames.add(decrypted.clone());

            String decryptedPath = decryptedFrameDir + String.format("frame_%04d.png", i);
            writer.write(decrypted);
            Imgcodecs.imwrite(decryptedPath, decrypted);
        }
        writer.release();
        frames.setDecryptedFrames(decryptedFrames);
        LOGGER.info("Decrypted Video stored at: {}", decryptedVideo);
        return RepeatStatus.FINISHED;
    }
}
