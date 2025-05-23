package com.major.project.crypto.config;

import com.major.project.crypto.VideoCryptographyApplication;
import com.major.project.crypto.task.CompareFramesTask;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

import com.major.project.crypto.task.DecryptionTask;
import com.major.project.crypto.task.EncryptionTask;

@TestConfiguration
@Import(VideoCryptographyApplication.class)
@Profile("test")
public class BatchTestConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    public BatchTestConfig(JobRepository jobRepository,
                       PlatformTransactionManager transactionManager) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;

    }

    @Bean
    public Job videoCryptographyJob(@Qualifier("encryptionTest") Step encryptStep,
                                    @Qualifier("decryptionTest") Step decryptStep,
                                    @Qualifier("compareTest") Step comparison) {
        return new JobBuilder("videoCryptographyJob", jobRepository)
                .start(encryptStep)
                .next(decryptStep)
                .next(comparison)
                .build();
    }

    @Bean
    @Qualifier("encryptionTest")
    public Step encryption(EncryptionTask encryptionTask) {
        return new StepBuilder("encryptionTest", jobRepository)
                .tasklet(encryptionTask, transactionManager).build();
    }

    @Bean
    @Qualifier("decryptionTest")
    public Step decryption(DecryptionTask decryptionTask) {
        return new StepBuilder("decryptionTest", jobRepository)
                .tasklet(decryptionTask, transactionManager).build();
    }

    @Bean
    @Qualifier("compareTest")
    public Step comparison(CompareFramesTask compareFramesTask) {
        return  new StepBuilder("compareTest", jobRepository)
                .tasklet(compareFramesTask, transactionManager).build();
    }

}
