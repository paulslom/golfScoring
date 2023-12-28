package com.pas.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.google.gson.Gson;
import com.pas.beans.GolfUser;

import jakarta.validation.constraints.NotBlank;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch.Builder;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

/**
 * Class to load data from JSON file.
 */
@Component
public class FileDataLoader 
{
	private static Logger logger = LogManager.getLogger(FileDataLoader.class);
    private final boolean loadData;
    private DynamoDbEnhancedClient dynamoDbEnhancedClient;
    
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
        
        if (Utils.isLocalEnv())
        {
        	logger.info("We are operating in LOCAL env - connecting to DynamoDBLocal");
        	
        	Properties prop = new Properties();
			
	    	InputStream stream = new FileInputStream(new File("/dynamoDb.properties")); 
	    	prop.load(stream);   		
		 	
		    String AWS_REGION = prop.getProperty("region");
		    String AWS_DYNAMODB_LOCAL_PORT = prop.getProperty("local_port");
		    
		    System.setProperty("sqlite4java.library.path", "C:\\Paul\\DynamoDB\\DynamoDBLocal_lib");
            String uri = "http://localhost:" + AWS_DYNAMODB_LOCAL_PORT;
            
            // Create an in-memory and in-process instance of DynamoDB Local that runs over HTTP
            final String[] localArgs = {"-inMemory", "-port", AWS_DYNAMODB_LOCAL_PORT};
            logger.info("Starting DynamoDB Local...");
            
            DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(localArgs);
            server.start();
            
            //  Create a client and connect to DynamoDB Local
            DynamoDbClient ddbClient =  DynamoDbClient.builder()
            		.endpointOverride(URI.create(uri))
                    .region(Region.of(AWS_REGION))
                    .credentialsProvider(ProfileCredentialsProvider.create("default"))
                    .build();
            
            //  Create a client and connect to DynamoDB Local, using an instance of the standard client.
            dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(ddbClient)                           
                    .build();
        }
        else
        {
        	logger.info("We are operating in AWS env - connecting to DynamoDB on AWS");
        	dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder().build();
        }
        
        
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
        dynamoDbEnhancedClient.batchWriteItem(batchWriteItemEnhancedRequest);
        
        logger.info("Completed loading data to DB.");
    }


    private List<GolfUser> loadFile() throws Exception 
    {
        List<GolfUser> golfUserList = (List<GolfUser>) readFromFileAndConvert();

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

    private static List<GolfUser> readFromFileAndConvert() 
    {
    	try (InputStream inputStream = FileDataLoader.class.getResourceAsStream("/data/GolfUsersData.json");
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) 
        {
        	GolfUser[] golfUserArray = new Gson().fromJson(reader, GolfUser[].class);
        	List<GolfUser> golfUserList = Arrays.asList(golfUserArray);
        	return golfUserList;
        } 
        catch (final Exception exception) 
        {
        	logger.error("Got an exception while reading the json file /data/golfusersdata.json", exception);
        }
        return null;
    }
}

