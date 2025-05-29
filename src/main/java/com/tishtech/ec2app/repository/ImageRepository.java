package com.tishtech.ec2app.repository;

import com.tishtech.ec2app.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<Image, UUID> {

    Optional<Image> findByName(String name);

    @Query(value = "SELECT * FROM images ORDER BY RAND() LIMIT 1", nativeQuery = true)
    Optional<Image> findRandom();
}
