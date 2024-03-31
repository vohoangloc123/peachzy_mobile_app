package com.example.peachzyapp.dynamoDB;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;

import java.util.List;

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
        try {
            BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAZI2LEH5QNBAXEUHP", "krI7P46llTA2kLj+AZQGSr9lEviTlS4bwQzBXSSi");

            ddbClient = new AmazonDynamoDBClient(credentials);
            ddbClient.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1)); // Set the region

            ListTablesResult tables = ddbClient.listTables();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public boolean checkDynamoDBConnection() {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            // Thực hiện công việc mạng trong một luồng mới
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        ListTablesResult tables = ddbClient.listTables();
                        for (String tableName : tables.getTableNames()) {
                            Log.d("DynamoDBManagerRun", "Table name: " + tableName);
                        }
                    } catch (Exception e) {
                        // Log exception for debugging
                        Log.e("DynamoDBManagerRun", "Error checking DynamoDB connection: " + e.getMessage());
                    }
                }
            }).start();

            // Trả về true vì việc khởi động công việc mạng đã được bắt đầu
            return true;
        } catch (Exception e) {
            // Log exception for debugging
            Log.e("DynamoDBManager", "Error checking DynamoDB connection: " + e.getMessage());
            return false;
        }
    }
}
