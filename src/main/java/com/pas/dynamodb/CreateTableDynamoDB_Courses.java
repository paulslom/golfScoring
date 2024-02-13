package com.pas.dynamodb;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.pas.beans.Course;
import com.pas.beans.Hole;

import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class CreateTableDynamoDB_Courses
{	 
	private static Logger logger = LogManager.getLogger(CreateTableDynamoDB_Courses.class);
	private static String AWS_TABLE_NAME = "courses";
	
	public void loadTable(DynamoClients dynamoClients, InputStream inputStream) throws Exception  
	{
		//Delete the table in DynamoDB Local if it exists.  If not, just catch the exception and move on
        try
        {
        	deleteTable(dynamoClients.getDynamoDbEnhancedClient());
        }
        catch (Exception e)
        {
        	logger.info(e.getMessage());
        }
        
        // Create a table in DynamoDB Local
        DynamoDbTable<DynamoCourse> table = createTable(dynamoClients.getDynamoDbEnhancedClient(), dynamoClients.getDdbClient());           

        loadTableData(table, inputStream);  
		
	}
   
    private static void deleteTable(DynamoDbEnhancedClient ddbEnhancedClient) throws Exception
    {
    	DynamoDbTable<DynamoCourse> coursesTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoCourse.class));
       	coursesTable.deleteTable();		
	}
   
    private static void loadTableData(DynamoDbTable<DynamoCourse> courseTable, InputStream inputStream) throws Exception
    {   
        // Insert data into the table
    	logger.info("Inserting data into the table:" + AWS_TABLE_NAME);   
        
        List<Course> courseList = readFromFileAndConvert(inputStream);
        
        if (courseList == null)
        {
        	logger.error("list from json file is Empty - can't do anything more so exiting");
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
            	
            	courseTable.putItem(dc);
                
    		}             
        }
        
	}
    
    private static List<Course> readFromFileAndConvert(InputStream inputStream) throws Exception
    {
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
       	Course[] courseArray = new Gson().fromJson(reader, Course[].class);
        List<Course> courseList = Arrays.asList(courseArray);
       	return courseList;       
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
            
            response.response().orElseThrow(
                    () -> new RuntimeException(AWS_TABLE_NAME + " was not created."));
            
            // The actual error can be inspected in response.exception()
            System.out.println(AWS_TABLE_NAME + " table was created.");
        }        
        
        return coursesTable;
    }
   
}