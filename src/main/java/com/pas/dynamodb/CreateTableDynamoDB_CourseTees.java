package com.pas.dynamodb;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.pas.beans.CourseTee;

import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class CreateTableDynamoDB_CourseTees
{	 
	private static Logger logger = LogManager.getLogger(CreateTableDynamoDB_Courses.class);
	private static String AWS_TABLE_NAME = "coursetees";
	
	private static DynamoDbTable<DynamoCourse> coursesTable;
	private static final String AWS_TABLE_NAME_COURSES = "courses";
	
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
        DynamoDbTable<DynamoCourseTee> table = createTable(dynamoClients.getDynamoDbEnhancedClient(), dynamoClients.getDdbClient());           

        //need the courses table to look up course ids
        coursesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME_COURSES, TableSchema.fromBean(DynamoCourse.class));
        
        loadTableData(table, inputStream);		
	}
	
    private static void deleteTable(DynamoDbEnhancedClient ddbEnhancedClient)
    {
    	DynamoDbTable<DynamoCourseTee> table = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoCourseTee.class));
       	table.deleteTable();        
	}
   
    private static void loadTableData(DynamoDbTable<DynamoCourseTee> table, InputStream inputStream) throws Exception
    {   
        // Insert data into the table
        logger.info("Inserting data into the table:" + AWS_TABLE_NAME);
       
        List<CourseTee> courseTeeList = readFromFileAndConvert(inputStream);
        
     	DynamoCourseTee dct = new DynamoCourseTee();            	
    	
    	//look up the new course id in the courses table, using the oldCourseID int field obj.getOldCourseID()
    	
    	DynamoDbIndex<DynamoCourse> coursesGSI = coursesTable.index("gsi_OldCourseID");

        if (courseTeeList == null)
        {
        	logger.error("list from json file is Empty - can't do anything more so exiting");
        }
        else
        {
        	for (int i = 0; i < courseTeeList.size(); i++) 
    		{
            	CourseTee obj = courseTeeList.get(i);            	
                    
            	Key key = Key.builder().partitionValue(obj.getOldCourseID()).build();
            	QueryConditional qc = QueryConditional.keyEqualTo(key);
            	
            	QueryEnhancedRequest qer = QueryEnhancedRequest.builder()
                        .queryConditional(qc)
                        .build();
            	SdkIterable<Page<DynamoCourse>> coursesByOldCourseID = coursesGSI.query(qer);
            	     
            	PageIterable<DynamoCourse> pages = PageIterable.create(coursesByOldCourseID);
            	
            	List<DynamoCourse> dcList = pages.items().stream().toList();
            	String courseID = "";
            	if (dcList != null && dcList.size() > 0)
            	{
            		DynamoCourse dc = dcList.get(0);
            		courseID = dc.getCourseID();
            	}
            	dct.setCourseID(courseID);
                dct.setOldCourseTeeID(obj.getOldCourseTeeID());                
            	dct.setCourseTeeID(UUID.randomUUID().toString());
            	dct.setOldCourseID(obj.getOldCourseID());
            	dct.setTeeColor(obj.getTeeColor());
            	dct.setCourseRating(obj.getCourseRating());
            	dct.setCoursePar(obj.getCoursePar());
            	dct.setSlopeRating(obj.getSlopeRating());
            	dct.setTotalYardage(obj.getTotalYardage());
            	
                table.putItem(dct);                
    		}             
        }
        
	}
    
    private static List<CourseTee> readFromFileAndConvert(InputStream inputStream) throws Exception
    {
       Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
       CourseTee[] courseTeeArray = new Gson().fromJson(reader, CourseTee[].class);
       List<CourseTee> courseTeeList = Arrays.asList(courseTeeArray);
       return courseTeeList;       
    }
    
    private static DynamoDbTable<DynamoCourseTee> createTable(DynamoDbEnhancedClient ddbEnhancedClient, DynamoDbClient ddbClient) 
    {
        DynamoDbTable<DynamoCourseTee> courseTeesTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoCourseTee.class));
        
        // Create the DynamoDB table.  If it exists, it'll throw an exception
        
        try
        {
        	ArrayList<EnhancedGlobalSecondaryIndex> gsindices = new ArrayList<>();
        	
        	EnhancedGlobalSecondaryIndex gsi1 = EnhancedGlobalSecondaryIndex.builder()
        			.indexName("gsi_OldCourseTeeID")
        			.projection(p -> p.projectionType(ProjectionType.ALL))
        			.build();
        	gsindices.add(gsi1);
        	
        	EnhancedGlobalSecondaryIndex gsi2 = EnhancedGlobalSecondaryIndex.builder()
                    .indexName("gsi_courseID")
                    .projection(p -> p.projectionType(ProjectionType.ALL))
                    .build();
        	gsindices.add(gsi2);       	
        	  	
        	courseTeesTable.createTable(r -> r.globalSecondaryIndices(gsindices).build());
        	
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
        
        return courseTeesTable;
    }

	
   
}