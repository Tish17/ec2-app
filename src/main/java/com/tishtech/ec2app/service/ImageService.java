package com.tishtech.ec2app.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.tishtech.ec2app.entity.Image;
import com.tishtech.ec2app.repository.ImageRepository;
import com.tishtech.ec2app.response.ImageMetadataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private final AmazonS3 amazonS3;
    private final ImageRepository imageRepository;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public byte[] downloadImage(String key) {
        log.info("downloadImage() - started with key = {}", key);
        S3Object s3Object = amazonS3.getObject(bucketName, key);
        try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
            byte[] data = inputStream.readAllBytes();
            log.info("downloadImage() - ended with key = {}", key);
            return data;
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }

    public ImageMetadataResponse getMetadata(String name) {
        log.info("getMetadata() - started with name = {}", name);
        Image image = imageRepository.findByName(name).orElseThrow(() -> new RuntimeException("Image not found"));
        ImageMetadataResponse response = getImageMetadata(image);
        log.info("getMetadata() - ended with response = {}", response);
        return response;
    }

    public ImageMetadataResponse getRandomMetadata() {
        log.info("getRandomMetadata() - started");
        Image image = imageRepository.findRandom().orElseThrow(() -> new RuntimeException("Image not found"));
        ImageMetadataResponse response = getImageMetadata(image);
        log.info("getRandomMetadata() - ended with response = {}", response);
        return response;
    }

    @Transactional
    public void uploadImage(MultipartFile file) {
        log.info("uploadImage() - started with fileName = {}", file.getOriginalFilename());
        saveImage(file);
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            amazonS3.putObject(bucketName, file.getOriginalFilename(), file.getInputStream(), metadata);
            log.info("uploadImage() - ended with fileName = {}", file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    @Transactional
    public void deleteImage(String fileName) {
        log.info("deleteImage() - started with fileName = {}", fileName);
        imageRepository.findByName(fileName).ifPresent(imageRepository::delete);
        amazonS3.deleteObject(bucketName, fileName);
        log.info("deleteImage() - ended with fileName = {}", fileName);
    }

    private void saveImage(MultipartFile file) {
        log.info("saveImage() - started with fileName = {}", file.getOriginalFilename());
        imageRepository.save(Image.builder()
                .name(file.getOriginalFilename())
                .extension(getExtension(Objects.requireNonNull(file.getOriginalFilename())))
                .size(file.getSize())
                .lastModified(new Date())
                .build());
        log.info("saveImage() - ended with fileName = {}", file.getOriginalFilename());
    }

    private String getExtension(String fileName) {
        return fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".") + 1) : "";
    }

    private ImageMetadataResponse getImageMetadata(Image image) {
        return new ImageMetadataResponse(image.getName(), image.getExtension(), image.getSize(), image.getLastModified());
    }
}

