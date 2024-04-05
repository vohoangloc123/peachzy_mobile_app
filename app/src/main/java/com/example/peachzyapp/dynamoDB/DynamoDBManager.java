package com.example.peachzyapp.dynamoDB;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.example.peachzyapp.entities.FriendItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamoDBManager {
    private Context context;
    private AmazonDynamoDBClient ddbClient;

    public DynamoDBManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;
    }
    private static AWSCredentials getAWSCredentials() {
        // Đây có thể là nơi bạn truy xuất thông tin xác thực từ nơi nào đó, chẳng hạn như SharedPreferences hoặc cơ sở dữ liệu
        // Trong ví dụ này, tôi sẽ trả về thông tin xác thực cố định, bạn có thể điều chỉnh theo nhu cầu của mình
        return new BasicAWSCredentials("AKIAZI2LEH5QNBAXEUHP", "krI7P46llTA2kLj+AZQGSr9lEviTlS4bwQzBXSSi");
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
    public void createAccountWithFirebaseUID(String firebaseUID, String firstName, String lastName, String email,String dateOfBirth, Boolean sex) {

        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAZI2LEH5QNBAXEUHP", "krI7P46llTA2kLj+AZQGSr9lEviTlS4bwQzBXSSi");

                        ddbClient = new AmazonDynamoDBClient(credentials);
                        ddbClient.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1)); // Set the region
                        // Tạo một mục mới
                        Map<String, AttributeValue> item = new HashMap<>();
                        item.put("_id", new AttributeValue().withS(firebaseUID));
                        item.put("name", new AttributeValue().withS(firstName+" "+lastName));
                        item.put("email", new AttributeValue().withS(email));
                        item.put("avatar", new AttributeValue().withS("https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/avatar.jpg"));
                        item.put("dateOfBirth", new AttributeValue().withS(dateOfBirth));
                        item.put("sex", new AttributeValue().withBOOL(sex));
                        // Tạo yêu cầu chèn mục vào bảng
                        PutItemRequest putItemRequest = new PutItemRequest()
                                .withTableName("Users")
                                .withItem(item);

                        // Thực hiện yêu cầu chèn mục và nhận kết quả
                        PutItemResult result = ddbClient.putItem(putItemRequest);
                        Log.d("CheckResult", String.valueOf(result));
                    } catch (Exception e) {
                        Log.e("Error", "Exception occurred: ", e);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Log exception for debugging
            Log.e("DynamoDBManager", "Error checking DynamoDB connection: " + e.getMessage());
        }
    }
    public void findFriend(String email, FriendFoundListener listener) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            Log.d("email", email);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Tạo một yêu cầu truy vấn
                        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
                        Condition condition = new Condition()
                                .withComparisonOperator(ComparisonOperator.EQ.toString())
                                .withAttributeValueList(new AttributeValue().withS(email));
                        scanFilter.put("email", condition);

                        ScanRequest scanRequest = new ScanRequest("Users").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);

                        // Xử lý kết quả
                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            String id=item.get("_id").getS();
                            String name = item.get("name").getS();
                            String avatar = item.get("avatar").getS();

                            // Tạo một chuỗi để hiển thị trong ListView, ví dụ: "Name: [Tên], Avatar: [Avatar]"
                            String friendResult = "Id"+ id+ "Name: " + name + ", Avatar: " + avatar;

                            // Log dữ liệu
                            Log.d("friendResult", friendResult);
                            listener.onFriendFound(id, name, avatar);
                            return; // Đảm bảo chỉ hiển thị một kết quả nếu tìm thấy
                        }
                        // Gọi callback nếu không tìm thấy bạn bè
                        listener.onFriendNotFound();
                    } catch (Exception e) {
                        listener.onError(e);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            listener.onError(e);
        }
    }

    // Định nghĩa interface để truyền kết quả tìm kiếm bạn bè
    public interface FriendFoundListener {
        void onFriendFound(String id, String friendResult, String avatar);
        void onFriendNotFound();
        void onError(Exception e);
    }
    public void addFriend(final String userId, final String friendId, final String status) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Lấy danh sách bạn bè hiện có từ cơ sở dữ liệu
                        GetItemRequest getRequest = new GetItemRequest().withTableName("Users").withKey(Collections.singletonMap("_id", new AttributeValue(userId)));
                        GetItemResult getResult = ddbClient.getItem(getRequest);
                        Map<String, AttributeValue> item = getResult.getItem();

                        // Kiểm tra xem danh sách "friends" đã được tạo chưa
                        if (item.containsKey("friends")) {
                            List<AttributeValue> friendsList = item.get("friends").getL();
                            boolean found = false;
                            // Duyệt qua danh sách bạn bè để kiểm tra xem bạn bè đã tồn tại chưa
                            for (AttributeValue friend : friendsList) {
                                String friendIdExisting = friend.getM().get("_idFriend").getS();
                                if (friendIdExisting.equals(friendId)) {
                                    // Nếu bạn bè đã tồn tại, cập nhật trạng thái của họ
                                    friend.getM().put("status", new AttributeValue(status));
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                // Nếu bạn bè không tồn tại, thêm mới vào danh sách
                                Map<String, AttributeValue> friendItem = new HashMap<>();
                                friendItem.put("_idFriend", new AttributeValue(friendId)); // ID của người bạn
                                friendItem.put("status", new AttributeValue(status)); // Trạng thái của mối quan hệ
                                friendsList.add(new AttributeValue().withM(friendItem));
                            }
                        } else {
                            // Nếu danh sách "friends" chưa tồn tại, tạo mới danh sách và thêm bạn bè vào
                            List<AttributeValue> friendsList = new ArrayList<>();
                            Map<String, AttributeValue> friendItem = new HashMap<>();
                            friendItem.put("_idFriend", new AttributeValue(friendId)); // ID của người bạn
                            friendItem.put("status", new AttributeValue(status)); // Trạng thái của mối quan hệ
                            friendsList.add(new AttributeValue().withM(friendItem));
                            item.put("friends", new AttributeValue().withL(friendsList)); // Thêm danh sách vào item
                        }

                        // Tạo yêu cầu put item để cập nhật danh sách "friends" của người dùng trong cơ sở dữ liệu
                        PutItemRequest putItemRequest = new PutItemRequest()
                                .withTableName("Users")
                                .withItem(item);

                        // Thực hiện cập nhật danh sách "friends" của người dùng trong cơ sở dữ liệu
                        ddbClient.putItem(putItemRequest);

                        // Debug
                        Log.d("AddFriend", "Successfully added friend with ID: " + friendId + " to user with ID: " + userId + " with status: " + status);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getProfileByUID(String uid, FriendFoundForGetUIDByEmailListener listener) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            Log.d("getProfileByUID",uid);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Tạo một yêu cầu truy vấn
                        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
                        Condition condition = new Condition()
                                .withComparisonOperator(ComparisonOperator.EQ)
                                .withAttributeValueList(new AttributeValue().withS(uid));
                        scanFilter.put("_id", condition);

                        ScanRequest scanRequest = new ScanRequest("Users").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);
                        Log.d("getProfileByUID1",uid);
                        // Xử lý kết quả
                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            String name=item.get("name").getS();
                            String email = item.get("email").getS();
                            String avatar=item.get("avatar").getS();
                            Boolean sex = Boolean.valueOf(item.get("sex").getBOOL()); // Lấy giá trị sex từ item
                            String dateOfBirth=item.get("dateOfBirth").getS();
                            // Tạo một chuỗi để hiển thị trong ListView, ví dụ: "Name: [Tên], Avatar: [Avatar]"
                            String userResult = "name đã nhận: " + name+ "email đã nhận: "+ email;

                            // Log dữ liệu
                            Log.d("userResult", userResult);
                            listener.onFriendFound(uid, name, email, avatar, sex, dateOfBirth);
                            return; // Đảm bảo chỉ hiển thị một kết quả nếu tìm thấy
                        }
                        // Gọi callback nếu không tìm thấy bạn bè
                        listener.onFriendNotFound();
                    } catch (Exception e) {
                        listener.onError(e);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            listener.onError(e);
        }
    }

    public void getIDFriend(String id,String status, AlreadyFriendListener listener) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Tạo một yêu cầu truy vấn
                        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
                        Condition condition = new Condition()
                                .withComparisonOperator(ComparisonOperator.EQ.toString())
                                .withAttributeValueList(new AttributeValue().withS(id));
                        scanFilter.put("_id", condition);

                        ScanRequest scanRequest = new ScanRequest("Users").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);
                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            List<AttributeValue> friendsList = item.get("friends").getL();
                            for (AttributeValue friend : friendsList) {
                                String status_friend = friend.getM().get("status").getS();
                                if (status.equals(status_friend)) {
                                    String friendId = friend.getM().get("_idFriend").getS(); // Lấy giá trị của thuộc tính "_idFriend"
                                    Log.d("friendID", friendId);
                                    findFriendByID(friendId,listener);
                                }
                            }
                        }
                    } catch (Exception e) {

                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            Log.e("", "Error checking DynamoDB connection: " + e.getMessage());
        }
    }

    public void findFriendByID(String id, AlreadyFriendListener listener) {

        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            Log.d("id", id);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Tạo một yêu cầu truy vấn
                        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
                        Condition condition = new Condition()
                                .withComparisonOperator(ComparisonOperator.EQ.toString())
                                .withAttributeValueList(new AttributeValue().withS(id));
                        scanFilter.put("_id", condition);

                        ScanRequest scanRequest = new ScanRequest("Users").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);

                        // Xử lý kết quả
                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            String friendResult = item.toString(); // Kết quả tìm thấy
                            Log.d("findbyid", friendResult);
                            String id=item.get("_id").getS();
                            String name = item.get("name").getS();
                            String avatar = item.get("avatar").getS();
                            FriendItem friend = new FriendItem(avatar,name);
                            listener.onFriendAlreadyFound(friend);
                            listener.onFriendAcceptRequestFound(id, name, avatar);
                        }
                    } catch (Exception e) {

                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {

        }
    }
    public interface FriendFoundForGetUIDByEmailListener {
        void onFriendFound(String uid, String name, String email, String avatar, Boolean sex, String dateOfBirth);
        void onFriendNotFound();
        void onError(Exception e);
    }

    public interface AlreadyFriendListener {
        void onFriendAlreadyFound(FriendItem data);
        void onFriendAcceptRequestFound(String id, String name, String avatar);
    }
    public void updateUser(String userId, String name, String dateOfBirth, String avatarUrl, Boolean sex, UpdateUserListener listener) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Tạo một yêu cầu truy vấn để lấy thông tin người dùng cần cập nhật
                        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
                        Condition condition = new Condition()
                                .withComparisonOperator(ComparisonOperator.EQ.toString())
                                .withAttributeValueList(new AttributeValue().withS(userId));
                        scanFilter.put("_id", condition);

                        ScanRequest scanRequest = new ScanRequest("Users").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);

                        // Xử lý kết quả
                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            // Cập nhật thông tin người dùng
                            item.put("name", new AttributeValue().withS(name));
                            item.put("dateOfBirth", new AttributeValue().withS(dateOfBirth));
                            item.put("avatar", new AttributeValue().withS(avatarUrl));
                            item.put("sex", new AttributeValue().withBOOL(sex));
                            // Tạo yêu cầu put item để cập nhật thông tin người dùng trong cơ sở dữ liệu
                            PutItemRequest putItemRequest = new PutItemRequest()
                                    .withTableName("Users")
                                    .withItem(item);

                            // Thực hiện cập nhật thông tin người dùng trong cơ sở dữ liệu
                            ddbClient.putItem(putItemRequest);

                            // Gọi callback để thông báo cập nhật thành công
                            listener.onUpdateSuccess();
                            return;
                        }

                        // Gọi callback nếu không tìm thấy người dùng cần cập nhật
                        listener.onUserNotFound();
                    } catch (Exception e) {
                        // Gọi callback nếu xảy ra lỗi
                        listener.onError(e);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Gọi callback nếu xảy ra lỗi
            listener.onError(e);
        }
    }

    public interface UpdateUserListener {
        void onUpdateSuccess();
        void onUserNotFound();
        void onError(Exception e);
    }
}
