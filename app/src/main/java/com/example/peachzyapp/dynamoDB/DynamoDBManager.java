package com.example.peachzyapp.dynamoDB;

import android.content.Context;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;

public class DynamoDBManager {
    private Context context;
    private AmazonDynamoDBClient ddbClient;

    public DynamoDBManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;

    }

    private void initializeDynamoDB() {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context.getApplicationContext(),
                "thanhnhan16.2.2002@gmail.com", // Replace with your AWS account email or phone number
                "ap-southeast-1", // Replace with the region your identity pool resides in
                "AKIAZI2LEH5QMWTQ3VEH", // Replace with your AWS Access Key ID
                "cI8hmArANH7FOh5AwlC3sqPnV88CZQSw7ndTtS/F", // Replace with your AWS Secret Access Key
                Regions.valueOf("ap-southeast-1") // Replace with the AWS region you are using
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);

        // Check connection status
//        checkDynamoDBConnection();
    }
//
//    public void checkDynamoDBConnection() {
//        try {
//            ListTablesResult tables = ddbClient.listTables();
//            // If there's no error, connection is successful
//            Toast.makeText(context, "DynamoDB connection successful.", Toast.LENGTH_SHORT).show();
//        } catch (AmazonServiceException e) {
//            // If there's an error, connection failed
//            Toast.makeText(context, "DynamoDB connection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
}
