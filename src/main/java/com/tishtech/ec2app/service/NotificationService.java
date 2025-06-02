package com.tishtech.ec2app.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tishtech.ec2app.response.ImageMetadataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    @Value("${download.link}")
    private String link;

    @Value("${aws.sqs.queueUrl}")
    private String queueUrl;

    @Value("${aws.sns.topicArn}")
    private String topicArn;

    private final SqsClient sqsClient;
    private final SnsClient snsClient;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String NOTIFICATION_TEMPLATE = """
             Image uploaded.
             Metadata:
             -size: %d
             -name: %s
             -extension: %s
             Download: %s
            """;
    private static final String FILTER_POLICY = """
                {
                  "extension": ["jpg"]
                }
            """;

    public void subscribe(String email) {
        log.info("subscribe() - started with email = {}", email);
        SubscribeRequest request = SubscribeRequest.builder()
                .topicArn(topicArn)
                .protocol("email")
                .endpoint(email)
                .attributes(Map.of("FilterPolicy", FILTER_POLICY))
                .build();
        snsClient.subscribe(request);
        log.info("subscribe() - ended with email = {}", email);
    }

    public void unsubscribe(String email) {
        log.info("unsubscribe() - started with email = {}", email);
        snsClient.listSubscriptionsByTopic(ListSubscriptionsByTopicRequest.builder().topicArn(topicArn).build()).subscriptions().stream()
                .filter(s -> s.endpoint().equals(email))
                .findFirst()
                .ifPresent(s -> snsClient.unsubscribe(UnsubscribeRequest.builder().subscriptionArn(s.subscriptionArn()).build()));
        log.info("unsubscribe() - ended with email = {}", email);
    }

    public void publish(ImageMetadataResponse metadata) {
        log.info("publish() - started with metadata = {}", metadata);
        try {
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(OBJECT_MAPPER.writeValueAsString(metadata))
                    .messageAttributes(Map.of(
                            "extension", MessageAttributeValue.builder()
                                    .dataType("String")
                                    .stringValue(metadata.extension())
                                    .build()
                    ))
                    .build());
        } catch (Exception e) {
            log.error("publish() - exception with message: ", e);
        }
        log.info("publish() - ended with metadata = {}", metadata);
    }

    @Scheduled(cron = "0 * * * * ?")
    public void processQueue() throws JsonProcessingException {
        log.info("processQueue() - started");
        ReceiveMessageResponse response = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .messageAttributeNames("All")
                .build());
        for (Message message : response.messages()) {
            ImageMetadataResponse metadata = OBJECT_MAPPER.readValue(message.body(), ImageMetadataResponse.class);
            snsClient.publish(PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(String.format(NOTIFICATION_TEMPLATE, metadata.size(), metadata.name(), metadata.extension(), link + metadata.name()))
                    .messageAttributes(Map.of(
                            "extension", software.amazon.awssdk.services.sns.model.MessageAttributeValue.builder()
                                    .dataType("String")
                                    .stringValue(metadata.extension())
                                    .build()
                    ))
                    .build());
            sqsClient.deleteMessage(DeleteMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .receiptHandle(message.receiptHandle())
                    .build());
        }
        log.info("processQueue() - ended");
    }
}
