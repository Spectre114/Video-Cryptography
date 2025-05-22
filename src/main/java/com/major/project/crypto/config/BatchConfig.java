package com.major.project.crypto.config;

import com.major.project.crypto.task.CompareFramesTask;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import com.major.project.crypto.task.DecryptionTask;
import com.major.project.crypto.task.EncryptionTask;

@Configuration
public class BatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchConfig(JobRepository jobRepository,
                       PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;

    }

    @Bean
    public Job videoCryptographyJob(@Qualifier("encryption") Step encryptStep,
                                    @Qualifier("decryption") Step decryptStep,
                                    @Qualifier("compare") Step comparison) {
        return new JobBuilder("videoCryptographyJob", jobRepository)
                .start(encryptStep)
                .next(decryptStep)
                .next(comparison)
                .build();
    }

    @Bean
    @Qualifier("encryption")
    public Step encryption(EncryptionTask encryptionTask) {
        return new StepBuilder("encryption", jobRepository)
                .tasklet(encryptionTask, transactionManager).build();
    }

    @Bean
    @Qualifier("decryption")
    public Step decryption(DecryptionTask decryptionTask) {
        return new StepBuilder("decryption", jobRepository)
                .tasklet(decryptionTask, transactionManager).build();
    }

    @Bean
    @Qualifier("compare")
    public Step comparison(CompareFramesTask compareFramesTask) {
        return  new StepBuilder("compare", jobRepository)
                .tasklet(compareFramesTask, transactionManager).build();
    }

}
