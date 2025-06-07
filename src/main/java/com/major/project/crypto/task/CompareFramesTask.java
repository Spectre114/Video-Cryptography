package com.major.project.crypto.task;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.major.project.crypto.module.Frames;

@Slf4j
@Component
public class CompareFramesTask implements Tasklet {

    Frames frames;

    @Autowired
    public CompareFramesTask(Frames frames) {
        this.frames = frames;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        boolean framesEqual = compareFrames(frames.getCopiedFrames(), frames.getDecryptedFrames());
        LOGGER.info("Original frames are {} to Decrypted Frames", framesEqual ? "equals" : "not equals");

        if (framesEqual) {
            LOGGER.info("SUCCESS: Encryption and Decryption working correctly!");
        } else {
            LOGGER.warn("WARNING: There are differences between original and decrypted frames");
        }

        return RepeatStatus.FINISHED;
    }

    private boolean compareFrames(List<Mat> originalFrames, List<Mat> decryptedFrames) {
        if (originalFrames.size() != decryptedFrames.size()) {
            LOGGER.warn("Frame count mismatch: {} vs {}", originalFrames.size(), decryptedFrames.size());
            return false;
        }

        for (int i = 0; i < originalFrames.size(); i++) {
            Mat original = originalFrames.get(i);
            Mat decrypted = decryptedFrames.get(i);
            
            if (!compareMatrices(original, decrypted)) {
                LOGGER.warn("Frame {} differs between original and decrypted", i);
                return false;
            }
        }
        
        return true;
    }
    
    private boolean compareMatrices(Mat mat1, Mat mat2) {
        if (mat1.rows() != mat2.rows() || mat1.cols() != mat2.cols() || mat1.channels() != mat2.channels()) {
            return false;
        }
        
        int totalPixels = mat1.rows() * mat1.cols();
        for (int idxPixel = 0; idxPixel < totalPixels; idxPixel++) {
            int i = idxPixel / mat1.cols();
            int j = idxPixel % mat1.cols();
            
            double[] pixel1 = mat1.get(i, j);
            double[] pixel2 = mat2.get(i, j);
            
            for (int k = 0; k < pixel1.length; k++) {
                if (Math.abs(pixel1[k] - pixel2[k]) > 1) { // Allow small differences due to rounding
                    return false;
                }
            }
        }
        
        return true;
    }
}
