package com.tishtech.ec2app.controller;

import com.tishtech.ec2app.response.ImageMetadataResponse;
import com.tishtech.ec2app.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/{name}")
    public ResponseEntity<byte[]> downloadImage(@PathVariable String name) {
        byte[] data = imageService.downloadImage(name);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(data);
    }

    @GetMapping("/metadata/{name}")
    public ResponseEntity<ImageMetadataResponse> getMetadata(@PathVariable String name) {
        return ResponseEntity.ok(imageService.getMetadata(name));
    }

    @GetMapping("/metadata/random")
    public ResponseEntity<ImageMetadataResponse> getRandomMetadata() {
        return ResponseEntity.ok(imageService.getRandomMetadata());
    }

    @PostMapping
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        imageService.uploadImage(file);
        return ResponseEntity.ok("Uploaded");
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<String> deleteImage(@PathVariable String name) {
        imageService.deleteImage(name);
        return ResponseEntity.ok("Deleted");
    }
}

