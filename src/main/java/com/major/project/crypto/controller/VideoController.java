package com.major.project.crypto.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.major.project.crypto.module.VideoUtils;
import com.major.project.crypto.task.DecryptionTask;
import com.major.project.crypto.task.EncryptionTask;

@Controller
public class VideoController {

    private final EncryptionTask encryptionTask;

    private final DecryptionTask decryptionTask;

    private final String decryptedVideo;

    private final String encryptedVideo;

    private final VideoUtils videoUtils;

    @Autowired
    public VideoController(@Value("${video.decrypted-video}") String decryptedVideo,
                           @Value("${video.output-encrypted}") String encryptedVideo,
                           VideoUtils videoUtils,
                           EncryptionTask encryptionTask,
                           DecryptionTask decryptionTask) {
        this.decryptedVideo = decryptedVideo;
        this.videoUtils = videoUtils;
        this.encryptedVideo = encryptedVideo;
        this.encryptionTask = encryptionTask;
        this.decryptionTask = decryptionTask;

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
            String uploadBasePath = System.getProperty("user.dir") + File.separator + "uploaded-videos";
            Path uploadDir = Paths.get(uploadBasePath);
            Files.createDirectories(uploadDir);
            Path filePath = uploadDir.resolve(Objects.requireNonNull(videoFile.getOriginalFilename()));
            videoFile.transferTo(filePath.toFile());
            videoUtils.setInputFilePath(filePath.toString());

            model.addAttribute("showInputVideo", true);
            model.addAttribute("status", "Video uploaded successfully.");
        } catch (IOException e) {
            model.addAttribute("status", "Upload failed: " + e.getMessage());
        }
        return "index";
    }
    
    /**
     * Display the decrypted video.
     *
     * @return video to display
     * @throws IOException if there is any
     */
    @GetMapping("/encryptVideo")
    public ResponseEntity<Resource> streamEncryptVideo() throws IOException {
        FileSystemResource resource = new FileSystemResource(encryptedVideo);
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
        FileSystemResource resource = new FileSystemResource(videoUtils.getInputFilePath());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("video/mp4"));
        model.addAttribute("showInputVideo", true);
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }
}
