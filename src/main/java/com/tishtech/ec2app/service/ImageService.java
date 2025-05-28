package com.tishtech.ec2app.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.tishtech.ec2app.response.ImageMetadataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Random RANDOM = new Random();

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public byte[] downloadImage(String key) {
        S3Object s3Object = amazonS3.getObject(bucketName, key);
        try (S3ObjectInputStream inputStream = s3Object.getObjectContent()) {
            return inputStream.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file", e);
        }
    }

    public ImageMetadataResponse getMetadata(String key) {
        ObjectMetadata metadata = amazonS3.getObjectMetadata(bucketName, key);
        String extension = key.contains(".") ? key.substring(key.lastIndexOf(".") + 1) : "";
        return new ImageMetadataResponse(key, extension, metadata.getContentLength(), metadata.getLastModified());
    }

    public ImageMetadataResponse getRandomMetadata() {
        ListObjectsV2Result result = amazonS3.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        if (objects.isEmpty()) {
            throw new RuntimeException("No images in bucket");
        }
        String randomKey = objects.get(RANDOM.nextInt(objects.size())).getKey();
        return getMetadata(randomKey);
    }

    public void uploadImage(MultipartFile file) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        try {
            amazonS3.putObject(bucketName, file.getOriginalFilename(), file.getInputStream(), metadata);
        } catch (IOException e) {
            throw new RuntimeException("Upload failed", e);
        }
    }

    public void deleteImage(String fileName) {
        amazonS3.deleteObject(bucketName, fileName);
    }
}

