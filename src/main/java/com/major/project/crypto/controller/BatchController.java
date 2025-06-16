package com.major.project.crypto.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.SneakyThrows;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.major.project.crypto.module.VideoPaths;

@Controller
public class BatchController {

    private final String decryptedVideo;

    private final VideoPaths videoPaths;

    private final JobLauncher jobLauncher;

    private final Job videoCryptographyJob;

    @Autowired
    public BatchController(@Value("${video.decrypted-video}") String decryptedVideo,
                           VideoPaths videoPaths,
                           JobLauncher jobLauncher,
                           Job videoCryptographyJob) {
        this.decryptedVideo = decryptedVideo;
        this.videoPaths = videoPaths;
        this.jobLauncher = jobLauncher;
        this.videoCryptographyJob = videoCryptographyJob;

    }

    /**
     * Home page.
     *
     * @return index.html
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }

    /**
     * Upload the desired video.
     *
     * @param videoFile video file to process
     * @param model handle values related to data
     * @return upload page
     */
    @PostMapping("/upload")
    public String uploadVideo(@RequestParam("videoFile") MultipartFile videoFile, Model model) {
        try {
            String uploadBasePath = System.getProperty("user.home") + File.separator + "uploaded-videos";
            Path uploadDir = Paths.get(uploadBasePath);
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(videoFile.getOriginalFilename());
            videoFile.transferTo(filePath.toFile());
            videoPaths.setInputFilePath(filePath.toString());

            model.addAttribute("showInputVideo", true);
            model.addAttribute("status", "Video uploaded successfully.");
        } catch (IOException e) {
            model.addAttribute("status", "Upload failed: " + e.getMessage());
        }
        return "index";
    }

    /**
     * Run the batch job to encrypt and decrypt the video.
     *
     * @param model handle values related to data
     * @return batch job runner page
     */
    @SneakyThrows
    @PostMapping("/run")
    public String runBatch(Model model) {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();
        JobExecution execution = jobLauncher.run(videoCryptographyJob, jobParameters);
        model.addAttribute("showInputVideo", true);
        model.addAttribute("status", execution.getStatus());
        return "index";

    }

    /**
     * Display the decrypted video.
     *
     * @return video to display
     * @throws IOException if there is any
     */
    @GetMapping("/video")
    public ResponseEntity<Resource> streamVideo() throws IOException {
        FileSystemResource resource = new FileSystemResource(decryptedVideo);
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("video/mp4"));
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    /**
     * Display the input video.
     *
     * @param model handle values related to data
     * @return video to display
     * @throws IOException if there is any
     */
    @GetMapping("/videoInput")
    public ResponseEntity<Resource> streamVideoInput(Model model) throws IOException {
        FileSystemResource resource = new FileSystemResource(videoPaths.getInputFilePath());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("video/mp4"));
        model.addAttribute("showInputVideo", true);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
