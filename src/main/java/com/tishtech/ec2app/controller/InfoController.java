package com.tishtech.ec2app.controller;

import com.tishtech.ec2app.response.InfoResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils;

@RestController
public class InfoController {

    @GetMapping("/info")
    public ResponseEntity<InfoResponse> getInfo() {
        String region = EC2MetadataUtils.getEC2InstanceRegion();
        String availabilityZone = EC2MetadataUtils.getAvailabilityZone();
        return ResponseEntity.ok(new InfoResponse(region, availabilityZone));
    }
}
