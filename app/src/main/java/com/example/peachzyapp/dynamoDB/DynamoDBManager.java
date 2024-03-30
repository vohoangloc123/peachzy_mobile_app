package com.example.peachzyapp.dynamoDB;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.regions.Region;
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
                "YOUR_AWS_ACCESS_KEY_ID", // Replace with your AWS Access Key ID
                "YOUR_AWS_SECRET_ACCESS_KEY", // Replace with your AWS Secret Access Key
                Regions.AP_SOUTHEAST_1 // Replace with the AWS region you are using
        );
        ddbClient = new AmazonDynamoDBClient(credentialsProvider);
        ddbClient.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1)); // Set the region
    }



    public boolean checkDynamoDBConnection() {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            ListTablesResult tables = ddbClient.listTables();
            for (String tableName : tables.getTableNames()) {
                Log.d("DynamoDBManager", "Table name: " + tableName);
            }
            // If there's no error, connection is successful
            return true;
        } catch (Exception e) {
            // Log exception for debugging
            Log.e("DynamoDBManager", "Error checking DynamoDB connection: " + e.getMessage());
            return false;
        }
    }
}
