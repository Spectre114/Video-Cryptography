package com.major.project.crypto.module;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.opencv.core.Mat;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
public class Frames {
    List<Mat> copiedFrames = new ArrayList<>();
    List<Mat> decryptedFrames = new ArrayList<>();
}
