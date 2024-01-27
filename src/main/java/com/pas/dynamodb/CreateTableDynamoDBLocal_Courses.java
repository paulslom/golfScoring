package com.pas.dynamodb;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.google.gson.Gson;
import com.pas.beans.Course;
import com.pas.beans.Hole;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class CreateTableDynamoDBLocal_Courses
{	 
	private static String AWS_DYNAMODB_LOCAL_PORT = "8000";
	private static String AWS_REGION = "us-east-1";
	private static String AWS_JSON_FILE_NAME = "CoursesData.json";
	private static String AWS_TABLE_NAME = "courses";
	private static String AWS_DYNAMODB_LOCAL_DB_LOCATION = "C:/Paul/DynamoDB";
	
    public static void main(String[] args) 
    {
        try 
        {
            System.setProperty("sqlite4java.library.path", "C:\\Paul\\DynamoDB\\DynamoDBLocal_lib");
            String uri = "http://localhost:" + AWS_DYNAMODB_LOCAL_PORT;
            
            // Create an in-memory and in-process instance of DynamoDB Local that runs over HTTP
            final String[] localArgs = {"-port", AWS_DYNAMODB_LOCAL_PORT, "-sharedDb", "-dbPath", AWS_DYNAMODB_LOCAL_DB_LOCATION};
            System.out.println("Starting DynamoDB Local...");
            DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(localArgs);
            server.start();
            
            DynamoDbClient ddbClient =  DynamoDbClient.builder()
            		.endpointOverride(URI.create(uri))
                    .region(Region.of(AWS_REGION))
                    .credentialsProvider(ProfileCredentialsProvider.create("default"))
                    .build();
            
            //  Create a client and connect to DynamoDB Local, using an instance of the standard client.
            DynamoDbEnhancedClient ddbEnhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(ddbClient)                           
                    .build();
            
            //Delete the table in DynamoDB Local if it exists
            deleteTable(ddbEnhancedClient);
            
            // Create a table in DynamoDB Local
            DynamoDbTable<DynamoCourse> courseTable = createTable(ddbEnhancedClient, ddbClient);

            loadTableData(courseTable);
            
            scan(courseTable);
            
        } 
        catch (Exception e) 
        {
        	e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        System.exit(1);
    }
    
    private static void deleteTable(DynamoDbEnhancedClient ddbEnhancedClient)
    {
    	DynamoDbTable<DynamoCourse> coursesTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoCourse.class));
        
        try
        {
        	coursesTable.deleteTable();
        }
        catch (Exception e)
        {
        	System.out.println("Table did not already exist, so no delete table executed! " + e.getMessage());
        }
		
	}

	private static void scan(DynamoDbTable<DynamoCourse> courseTable) 
    {
        try 
        {
            Iterator<DynamoCourse> results = courseTable.scan().items().iterator();
            
            while (results.hasNext()) 
            {
                DynamoCourse rec = results.next();
                System.out.println("ID = " + rec.getCourseID() + " .. courseName = " + rec.getCourseName());
            }
        } 
        catch (DynamoDbException e) 
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done with dynamo scan");
    }
   
    private static void loadTableData(DynamoDbTable<DynamoCourse> courseTable) throws Exception
    {   
        // Insert data into the table
        System.out.println();
        System.out.println("Inserting data into the table:" + AWS_TABLE_NAME);
        System.out.println();        
        
        List<Course> courseList = readFromFileAndConvert();
        
        if (courseList == null)
        {
        	System.err.println("list from json file is Empty - can't do anything more so exiting");
            System.exit(1);
        }
        else
        {
        	for (int i = 0; i < courseList.size(); i++) 
    		{
            	Course obj = courseList.get(i);
            	
            	DynamoCourse dc = new DynamoCourse();             	
            	 	
            	dc.setOldCourseID(Integer.parseInt(obj.getCourseID()));
            	dc.setCourseID(UUID.randomUUID().toString());
            	dc.setCourseName(obj.getCourseName());
            	dc.setFront9Par(obj.getFront9Par());
            	dc.setBack9Par(obj.getBack9Par());
            	dc.setCoursePar(obj.getCoursePar());
            	dc.setGroupID("1");
            	
            	for (int j = 0; j < obj.getHolesList().size(); j++) 
            	{
            		Hole hole = obj.getHolesList().get(j);
            	
            		switch (j) 
            		{
	            		case 0: 
	            			
	            			dc.setHole1Par(hole.getPar());
	            			break;
	            			
	            		case 1: 
	            			
	            			dc.setHole2Par(hole.getPar());
	            			break;
	            			
	            		case 2: 
	            			
	            			dc.setHole3Par(hole.getPar());
	            			break;
	            			
	            		case 3: 
	            			
	            			dc.setHole4Par(hole.getPar());
	            			break;
	            			
	            		case 4: 
	            			
	            			dc.setHole5Par(hole.getPar());
	            			break;
	            			
	            		case 5: 
	            			
	            			dc.setHole6Par(hole.getPar());
	            			break;
	            			
	            		case 6: 
	            			
	            			dc.setHole7Par(hole.getPar());
	            			break;
	            			
	            		case 7: 
	            			
	            			dc.setHole8Par(hole.getPar());
	            			break;
	            			
	            		case 8: 
	            			
	            			dc.setHole9Par(hole.getPar());
	            			break;
	            			
	            		case 9: 
	            			
	            			dc.setHole10Par(hole.getPar());
	            			break;
	            			
	            		case 10: 
	            			
	            			dc.setHole11Par(hole.getPar());
	            			break;
	            			
	            		case 11: 
	            			
	            			dc.setHole12Par(hole.getPar());
	            			break;
	            			
	            		case 12: 
	            			
	            			dc.setHole13Par(hole.getPar());
	            			break;
	            			
	            		case 13: 
	            			
	            			dc.setHole14Par(hole.getPar());
	            			break;
	            			
	            		case 14: 
	            			
	            			dc.setHole15Par(hole.getPar());
	            			break;
	            			
	            		case 15: 
	            			
	            			dc.setHole16Par(hole.getPar());
	            			break;
	            			
	            		case 16: 
	            			
	            			dc.setHole17Par(hole.getPar());
	            			break;
	            			
	            		case 17: 
	            			
	            			dc.setHole18Par(hole.getPar());
	            			break;
            		}
            			
				}
            	
                try 
                {
                    courseTable.putItem(dc);
                } 
                catch (ResourceNotFoundException e) 
                {
                    System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", AWS_TABLE_NAME);
                    System.err.println("Be sure that it exists and that you've typed its name correctly!");
                    System.exit(1);
                } 
                catch (DynamoDbException e) 
                {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
    		}             
        }
        
	}
    
    private static List<Course> readFromFileAndConvert() 
    {
    	String jsonFile = "C:\\Paul\\GitHub\\golfScoring\\src\\main\\resources\\data\\" + AWS_JSON_FILE_NAME;
    	
        try (InputStream inputStream = new FileInputStream(new File(jsonFile));
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) 
        {
        	Course[] courseArray = new Gson().fromJson(reader, Course[].class);
        	List<Course> courseList = Arrays.asList(courseArray);
        	return courseList;
        } 
        catch (final Exception exception) 
        {
        	System.out.println("Got an exception while reading the json file " + AWS_JSON_FILE_NAME + exception.getMessage());
        }
        return null;
    }
    
    private static DynamoDbTable<DynamoCourse> createTable(DynamoDbEnhancedClient ddbEnhancedClient, DynamoDbClient ddbClient) 
    {
        DynamoDbTable<DynamoCourse> coursesTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoCourse.class));
        
        // Create the DynamoDB table.  If it exists, it'll throw an exception
        
        try
        {
        	coursesTable.createTable(r -> r.provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
                    .globalSecondaryIndices(
                        EnhancedGlobalSecondaryIndex.builder()
                                                    .indexName("gsi_OldCourseID")
                                                    .projection(p -> p.projectionType(ProjectionType.ALL))
                                                    .provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
                                                    .build()));
	        
        }
        catch (ResourceInUseException riue)
        {
        	System.out.println("Table already exists! " + riue.getMessage());
        	throw riue;
        }
        // The 'dynamoDbClient' instance that's passed to the builder for the DynamoDbWaiter is the same instance
        // that was passed to the builder of the DynamoDbEnhancedClient instance used to create the 'customerDynamoDbTable'.
        // This means that the same Region that was configured on the standard 'dynamoDbClient' instance is used for all service clients.
        
        try (DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(ddbClient).build()) // DynamoDbWaiter is Autocloseable
        { 
            ResponseOrException<DescribeTableResponse> response = waiter
                    .waitUntilTableExists(builder -> builder.tableName(AWS_TABLE_NAME).build())
                    .matched();
            
            DescribeTableResponse tableDescription = response.response().orElseThrow(
                    () -> new RuntimeException(AWS_TABLE_NAME + " was not created."));
            
            // The actual error can be inspected in response.exception()
            System.out.println(AWS_TABLE_NAME + " table was created.");
        }        
        
        return coursesTable;
    }
   
}