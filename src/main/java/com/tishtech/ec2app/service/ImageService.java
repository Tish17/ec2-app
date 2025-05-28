package com.tishtech.ec2app.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.tishtech.ec2app.response.ImageMetadataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Random RANDOM = new Random();

    private final AmazonS3 amazonS3;

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

    public ImageMetadataResponse getMetadata(String key) {
        log.info("getMetadata() - started with key = {}", key);
        ObjectMetadata metadata = amazonS3.getObjectMetadata(bucketName, key);
        String extension = key.contains(".") ? key.substring(key.lastIndexOf(".") + 1) : "";
        ImageMetadataResponse response = new ImageMetadataResponse(key, extension, metadata.getContentLength(), metadata.getLastModified());
        log.info("getMetadata() - ended with response = {}", response);
        return response;
    }

    public ImageMetadataResponse getRandomMetadata() {
        log.info("getRandomMetadata() - started");
        ListObjectsV2Result result = amazonS3.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        if (objects.isEmpty()) {
            throw new RuntimeException("No images in bucket");
        }
        String randomKey = objects.get(RANDOM.nextInt(objects.size())).getKey();
        ImageMetadataResponse response = getMetadata(randomKey);
        log.info("getRandomMetadata() - ended with response = {}", response);
        return response;
    }

    public void uploadImage(MultipartFile file) {
        log.info("uploadImage() - started with fileName = {}", file.getOriginalFilename());
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        try {
            amazonS3.putObject(bucketName, file.getOriginalFilename(), file.getInputStream(), metadata);
            log.info("uploadImage() - ended with fileName = {}", file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    public void deleteImage(String fileName) {
        log.info("deleteImage() - started with fileName = {}", fileName);
        amazonS3.deleteObject(bucketName, fileName);
        log.info("deleteImage() - ended with fileName = {}", fileName);
    }
}

