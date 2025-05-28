package com.tishtech.ec2app.response;

import java.util.Date;

public record ImageMetadataResponse(String name, String extension, long size, Date lastModified) {
}
