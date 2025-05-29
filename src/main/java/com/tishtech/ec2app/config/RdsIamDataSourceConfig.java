package com.tishtech.ec2app.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsUtilities;

import javax.sql.DataSource;

@Configuration
public class RdsIamDataSourceConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${rds.endpoint}")
    private String dbEndpoint;

    @Value("${rds.port}")
    private int dbPort;

    @Value("${rds.dbName}")
    private String dbName;

    @Value("${rds.username}")
    private String dbUser;

    @Bean
    public DataSource dataSource() {
        AwsCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
        RdsUtilities rdsUtilities = RdsUtilities.builder()
                .region(Region.of(region))
                .build();
        String authToken = rdsUtilities.generateAuthenticationToken(builder -> builder
                .hostname(dbEndpoint)
                .port(dbPort)
                .username(dbUser)
                .credentialsProvider(credentialsProvider));
        String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s?useSSL=true&verifyServerCertificate=false", dbEndpoint, dbPort, dbName);
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(dbUser);
        config.setPassword(authToken);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return new HikariDataSource(config);
    }
}
