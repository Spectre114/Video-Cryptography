package com.major.project.crypto.task;

import lombok.extern.slf4j.Slf4j;
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

        LOGGER.info("Original frames are {} to Decrypted Frames",
                frames.getCopiedFrames().equals(frames.getDecryptedFrames()) ? "equals" : "not equals");
        return RepeatStatus.FINISHED;
    }
}
