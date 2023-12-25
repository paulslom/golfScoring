package com.pas.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.pas.beans.GolfUser;

import jakarta.validation.constraints.NotBlank;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch.Builder;

/**
 * Class to load data from JSON file.
 */
@Component
public class FileDataLoader 
{
	private static Logger logger = LogManager.getLogger(FileDataLoader.class);
    private final boolean loadData;

    /**
     * Public constructor.
     *
     * @param callSummaryRepository - CallSummaryRepository
     */
    public FileDataLoader(@NotBlank @Value("${app.load-data}") final boolean loadData) 
    {
  		this.loadData = loadData;
    }

    public boolean load() throws Exception 
    {
        if (!loadData) 
        {
            logger.info("Load data property is set to false.");
            return true;
        }
        DynamoDbEnhancedClient dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder().build();
        DynamoDbTable<GolfUser> table = dynamoDbEnhancedClient.table("GolfUsers", TableSchema.fromBean(GolfUser.class));
        table.createTable();
        
        List<GolfUser> golfUserList = loadFile();
        loadDynamoDb(golfUserList);
        
        return true;
    }

    private void loadDynamoDb(List<GolfUser> datalist) 
    {
        Builder<GolfUser> writeBatchBuilder = WriteBatch.builder(GolfUser.class);
        datalist.forEach(data -> writeBatchBuilder.addPutItem(builder -> builder.item(data)));
        BatchWriteItemEnhancedRequest batchWriteItemEnhancedRequest = BatchWriteItemEnhancedRequest.builder().writeBatches(writeBatchBuilder.build()).build();
        DynamoDbEnhancedClient dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder().build();
        dynamoDbEnhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
        
        logger.info("Completed loading data to DB.");
    }


    private List<GolfUser> loadFile() throws Exception 
    {
        List<GolfUser> golfUserList = (List<GolfUser>) readFromFileAndConvert(GolfUser.class);

        if (golfUserList == null || golfUserList.isEmpty()) 
        {
            logger.info("Failed to load GolfUsersData.json in memory.");
            throw new Exception("Failed to load GolfUsersData.json in memory.");
        } 
        else 
        {
            logger.info("Loaded GolfUsersData.json in memory. Found {} items.", golfUserList.size());
            return golfUserList;
        }
    }


    /**
     * Read the data from JSON file and @return List.
     */
    private List<GolfUser> readFromFileAndConvert(Class tableClass) 
    {
        try (InputStream inputStream = FileDataLoader.class.getResourceAsStream("/data/GolfUsersData.json");
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) 
        {
            return (List<GolfUser>) new Gson().fromJson(reader, tableClass);
        } 
        catch (final Exception exception) 
        {
            logger.error("Got an exception while reading the json file /data/golfusersdata.json", exception);
        }
        return null;
    }
}

