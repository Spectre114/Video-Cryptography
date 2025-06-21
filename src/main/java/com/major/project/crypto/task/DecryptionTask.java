package com.major.project.crypto.task;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.Size;
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

    private final String encryptedFile;
    private final String decryptedVideo;
    private final Frames frames;
    private final DecryptService decrypt;
    HashMap<Integer, String> metadataMap = new HashMap<>();
    List<Mat> decryptedFrames = new ArrayList<>();
    ArrayList<Mat> framesToDecrypt = new ArrayList<>();

    @Autowired
    public DecryptionTask(DecryptService decrypt,
                          @Value("${video.output-encrypted}") String encryptedFile,
                          @Value("${video.decrypted-video}") String decryptedVideo,
                          Frames frames) {
        this.decrypt = decrypt;
        this.encryptedFile = encryptedFile;
        this.decryptedVideo = decryptedVideo;
        this.frames = frames;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        //TODO Add handshake logic to ensure that the
        // decryption is done only if the secret key matches with that generated during encryption
        LOGGER.info("Decryption Started");
        OpenCV.loadLocally();
        framesToDecrypt = frames.getFramesToDecrypt();
        try {
            ObjectMapper mapper = new ObjectMapper();
            metadataMap = mapper.readValue(new File(encryptedFile.replaceAll("\\..*$", "") + ".json"),
                    mapper.getTypeFactory().constructMapType(HashMap.class, Integer.class, String.class));
        } catch (IOException e) {
            e.printStackTrace();
            return RepeatStatus.FINISHED;
        }

        int width = framesToDecrypt.getFirst().cols();
        int height = framesToDecrypt.getFirst().rows();
        Size frameSize = new Size(width, height);

        int fourcc = VideoWriter.fourcc('f', 'f', 'v', '1');
        VideoWriter writer = new VideoWriter(decryptedVideo, fourcc, 30.0, frameSize, true);

        for (int i = 0; metadataMap.containsKey(i); i++) {
            Mat encryptedFrame = framesToDecrypt.get(i).clone();
            byte[] keyHexBytes = metadataMap.get(i).getBytes(StandardCharsets.UTF_8);

            Mat decrypted = decrypt.decrypt(encryptedFrame.clone(), keyHexBytes);
            decryptedFrames.add(decrypted.clone());

            writer.write(decrypted);
        }
        writer.release();
        frames.setDecryptedFrames(decryptedFrames);
        frames.setFramesToDecrypt(framesToDecrypt);
        LOGGER.info("Decrypted Video stored at: {}", decryptedVideo);
        return RepeatStatus.FINISHED;
    }
}
