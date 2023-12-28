package com.pas.dynamodb;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Configuration
public class DynamoDbConfig 
{
    private final String dynamoDbEndPointUrl;

    public DynamoDbConfig(@Value("${aws.dynamodb.endpoint}") String dynamoDbEndPointUrl) 
    {
        this.dynamoDbEndPointUrl = dynamoDbEndPointUrl;
    }

    @Bean
    public DynamoDbAsyncClient getDynamoDbAsyncClient() 
    {
        return DynamoDbAsyncClient.builder()
                .endpointOverride(URI.create(dynamoDbEndPointUrl))
                .build();
    }

    @Bean
    public DynamoDbEnhancedAsyncClient getDynamoDbEnhancedAsyncClient() 
    {
        return DynamoDbEnhancedAsyncClient.builder()
                .dynamoDbClient(getDynamoDbAsyncClient())
                .build();
    }

}