package com.tishtech.ec2app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "images")
public class Image {

    @Id
    @GeneratedValue
    private UUID id;

    private String extension;
    private long size;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "last_modified")
    private Date lastModified;
}
