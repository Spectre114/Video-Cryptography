package com.major.project.crypto.controller;

import java.time.Duration;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.major.project.crypto.module.VideoUtils;
import com.major.project.crypto.task.DecryptionTask;
import com.major.project.crypto.task.EncryptionTask;

@Slf4j
@Controller
public class KafkaController {

    private static final String VID_CRYPTO = "vid-crypto";

    private final KafkaTemplate<String, VideoUtils> kafkaTemplate;

    private final KafkaConsumer<String, VideoUtils> kafkaConsumer;

    private final VideoUtils videoUtils;

    private final EncryptionTask encryptionTask;

    private final DecryptionTask decryptionTask;

    private final String decryptedVideo;

    public KafkaController(KafkaTemplate<String, VideoUtils> kafkaTemplate,
                           KafkaConsumer<String, VideoUtils> kafkaConsumer,
                           VideoUtils videoUtils,
                           EncryptionTask encryptionTask,
                           DecryptionTask decryptionTask,
                           @Value("${video.decrypted-video:}") String decryptedVideo) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaConsumer = kafkaConsumer;
        this.videoUtils = videoUtils;
        this.encryptionTask = encryptionTask;
        this.decryptionTask = decryptionTask;
        this.decryptedVideo = decryptedVideo;
    }

    @PostMapping(value = "/send-message")
    @SneakyThrows
    public String sendMessage(Model model) {
        encryptionTask.encrypt();
        kafkaTemplate.send(VID_CRYPTO, videoUtils);
        model.addAttribute("showInputVideo", true);
        return "index";
    }

    @GetMapping("/receive")
    public ResponseEntity<Resource> receive() {

        ConsumerRecords<String, VideoUtils> records = kafkaConsumer.poll(Duration.ofSeconds(5));

        if (records.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        for (ConsumerRecord<String, VideoUtils> record : records) {

            String inputFilePath = record.value().getInputFilePath();
            byte[] encryptedVideo = record.value().getEncryptedBytes();
            byte[] metadata = record.value().getMetadata();
            videoUtils.setInputFilePath(inputFilePath);
            videoUtils.setMetadata(metadata);
            videoUtils.setEncryptedBytes(encryptedVideo);

            LOGGER.info("Bytes Received: {}", encryptedVideo.length);
            decryptionTask.decrypt();
        }
        kafkaConsumer.commitSync();
        FileSystemResource resource = new FileSystemResource(decryptedVideo);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("video/mp4"));
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
