package com.major.project.crypto.controller;

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
            // Use system temp directory (safe for Render.com and cloud platforms)
            String uploadBasePath = System.getProperty("java.io.tmpdir"); // Typically "/tmp" in Linux
            Path uploadDir = Paths.get(uploadBasePath);
            Files.createDirectories(uploadDir); // Just to be safe, though /tmp always exists

            // Save the uploaded file in temp directory with original filename
            Path filePath = uploadDir.resolve(videoFile.getOriginalFilename());
            videoFile.transferTo(filePath.toFile());

            // Update your shared path holder with absolute path
            videoPaths.setInputFilePath(filePath.toString());

            // Inform the HTML template
            model.addAttribute("showInputVideo", true);
            model.addAttribute("status", "Video uploaded successfully.");
        } catch (IOException e) {
            e.printStackTrace();
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
        // Check if video file path is set
        String inputVideoPath = videoPaths.getInputFilePath();
        if (inputVideoPath == null || inputVideoPath.isEmpty()) {
            model.addAttribute("status", "Error: No video file uploaded. Please upload a video before running the job.");
            return "index";
        }

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
        if (decryptedVideo == null || decryptedVideo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        String uploadBasePath = System.getProperty("java.io.tmpdir"); // Typically "/tmp" in Linux
        Path filePath = Paths.get(uploadBasePath, decryptedVideo);
        FileSystemResource resource = new FileSystemResource(filePath.toFile());
        if (!resource.exists() || !resource.isFile()) {
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
        String inputVideoPath = videoPaths.getInputFilePath();
        if (inputVideoPath == null || inputVideoPath.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        FileSystemResource resource = new FileSystemResource(inputVideoPath);
        if (!resource.exists() || !resource.isFile()) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("video/mp4"));
        model.addAttribute("showInputVideo", true);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
