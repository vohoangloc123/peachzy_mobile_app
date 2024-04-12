package com.example.peachzyapp.dynamoDB;

import android.content.Context;
import android.util.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeAction;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.AttributeValueUpdate;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.example.peachzyapp.entities.Conversation;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.entities.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

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
        return new BasicAWSCredentials("AKIAZI2LEH5QOVBLO5IY", "qjAXFoZGxhu9u7IaDV818wbZNQeao2it1Wm8yEju");
    }

    private void initializeDynamoDB() {
        try {
            BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAZI2LEH5QOVBLO5IY", "qjAXFoZGxhu9u7IaDV818wbZNQeao2it1Wm8yEju");

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

    public void createAccountWithFirebaseUID(String firebaseUID, String firstName, String lastName, String email, String dateOfBirth, Boolean sex) {

        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAZI2LEH5QNBAXEUHP", "krI7P46llTA2kLj+AZQGSr9lEviTlS4bwQzBXSSi");
//
//                        ddbClient = new AmazonDynamoDBClient(credentials);
//                        ddbClient.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1)); // Set the region
                        // Tạo một mục mới
                        Map<String, AttributeValue> item = new HashMap<>();
                        item.put("_id", new AttributeValue().withS(firebaseUID));
                        item.put("name", new AttributeValue().withS(firstName + " " + lastName));
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
    }//update code 2

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
                            String id = item.get("_id").getS();
                            String name = item.get("name").getS();
                            String avatar = item.get("avatar").getS();

                            // Tạo một chuỗi để hiển thị trong ListView, ví dụ: "Name: [Tên], Avatar: [Avatar]"
                            String friendResult = "Id" + id + "Name: " + name + ", Avatar: " + avatar;

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

    public void addFriend(final String userId, final String friendId, final String status, final String channelID) {
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
                                friendItem.put("status", new AttributeValue(status));
                                friendItem.put("channel_id", new AttributeValue(channelID));// Trạng thái của mối quan hệ
                                friendsList.add(new AttributeValue().withM(friendItem));
                            }
                        } else {
                            // Nếu danh sách "friends" chưa tồn tại, tạo mới danh sách và thêm bạn bè vào
                            List<AttributeValue> friendsList = new ArrayList<>();
                            Map<String, AttributeValue> friendItem = new HashMap<>();
                            friendItem.put("_idFriend", new AttributeValue(friendId)); // ID của người bạn
                            friendItem.put("status", new AttributeValue(status));// Trạng thái của mối quan hệ
                            friendItem.put("channel_id", new AttributeValue(channelID));
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

    //.
    public void getProfileByUID(String uid, FriendFoundForGetUIDByEmailListener listener) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            Log.d("getProfileByUID", uid);
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
                        Log.d("getProfileByUID1", uid);
                        // Xử lý kết quả
                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            String name = item.get("name").getS();
                            String email = item.get("email").getS();
                            String avatar = item.get("avatar").getS();
                            Boolean sex = Boolean.valueOf(item.get("sex").getBOOL()); // Lấy giá trị sex từ item
                            String dateOfBirth = item.get("dateOfBirth").getS();
                            // Tạo một chuỗi để hiển thị trong ListView, ví dụ: "Name: [Tên], Avatar: [Avatar]"
                            String userResult = "name đã nhận: " + name + "email đã nhận: " + email;

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

    public void getIDFriend(String id, String status, AlreadyFriendListener listener) {
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
                                    findFriendByID(friendId, listener);
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
                            String id = item.get("_id").getS();
                            String name = item.get("name").getS();
                            String avatar = item.get("avatar").getS();
                            FriendItem friend = new FriendItem(avatar, name);
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
                            Log.d("putItemRequest", String.valueOf(putItemRequest));
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

    public void findAvatarByUID(String id, final AvatarCallback callback) {

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
                            String avatar = item.get("avatar").getS();
                            // Gọi callback để trả về avatar
                            callback.onSuccess(avatar);
                        }
                    } catch (Exception e) {
                        // Gọi callback nếu có lỗi xảy ra
                        callback.onError(e);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Gọi callback nếu có lỗi xảy ra
            callback.onError(e);
        }

    }

    public interface AvatarCallback {
        void onSuccess(String avatar);

        void onError(Exception e);
    }

    public void saveMessage(String channelID, String message, String time, Boolean sentByMe) {
        Log.d("SaveMessageInfo", channelID + message + time + sentByMe);
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    GetItemRequest getItemRequest = new GetItemRequest()
                            .withTableName("ChatHistory")
                            .withKey(Collections.singletonMap("_id", new AttributeValue().withS(channelID)));
                    GetItemResult getItemResult = ddbClient.getItem(getItemRequest);

                    if (getItemResult.getItem() == null) {
                        // Nếu cuộc trò chuyện chưa tồn tại, tạo mới và lưu vào DynamoDB
                        Map<String, AttributeValue> item = new HashMap<>();
                        item.put("_id", new AttributeValue().withS(channelID));
                        List<Map<String, AttributeValue>> messages = new ArrayList<>();
                        Map<String, AttributeValue> messageItem = new HashMap<>();
                        messageItem.put("time", new AttributeValue().withS(time));
                        messageItem.put("message", new AttributeValue().withS(message));
                        messageItem.put("sentByMe", new AttributeValue().withBOOL(sentByMe));
                        messages.add(messageItem);
                        item.put("messages", new AttributeValue().withL(messages.stream()
                                .map(msg -> new AttributeValue().withM(msg))
                                .collect(Collectors.toList())));

                        PutItemRequest putItemRequest = new PutItemRequest()
                                .withTableName("ChatHistory")
                                .withItem(item);

                        ddbClient.putItem(putItemRequest);
                        //đồng thời tạo lu

                    } else {
                        // Nếu cuộc trò chuyện đã tồn tại, thêm tin nhắn mới vào mảng messages
                        Map<String, AttributeValueUpdate> updates = new HashMap<>();
                        Map<String, AttributeValue> messageItem = new HashMap<>();
                        messageItem.put("time", new AttributeValue().withS(time));
                        messageItem.put("message", new AttributeValue().withS(message));
                        messageItem.put("sentByMe", new AttributeValue().withBOOL(sentByMe));
                        updates.put("messages", new AttributeValueUpdate()
                                .withAction(AttributeAction.ADD)
                                .withValue(new AttributeValue().withL(Collections.singletonList(new AttributeValue().withM(messageItem)))));

                        UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                                .withTableName("ChatHistory")
                                .withKey(Collections.singletonMap("_id", new AttributeValue().withS(channelID)))
                                .withAttributeUpdates(updates);

                        ddbClient.updateItem(updateItemRequest);
                    }

                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Gọi callback nếu có lỗi xảy ra

        }
    }
    public void saveConversation(String userUID, String conversationID, String friendID,String message, String time, String avatar, String name) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    GetItemRequest getItemRequest = new GetItemRequest()
                            .withTableName("Conversation")
                            .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userUID)));

                    GetItemResult getItemResult = ddbClient.getItem(getItemRequest);

                    if (getItemResult.getItem() == null) {
                        // Nếu cuộc trò chuyện chưa tồn tại, tạo mới và lưu vào DynamoDB
                        Map<String, AttributeValue> item = new HashMap<>();
                        item.put("_id", new AttributeValue().withS(userUID));
                        List<Map<String, AttributeValue>> conversations = new ArrayList<>();
                        Map<String, AttributeValue> conversationItem = new HashMap<>();
                        conversationItem.put("conversationID", new AttributeValue().withS(conversationID));
                        conversationItem.put("friendID", new AttributeValue().withS(friendID));
                        conversationItem.put("time", new AttributeValue().withS(time));
                        conversationItem.put("message", new AttributeValue().withS(message));
                        conversationItem.put("avatar", new AttributeValue().withS(avatar));
                        conversationItem.put("name", new AttributeValue().withS(name));
                        conversations.add(conversationItem);
                        item.put("conversations", new AttributeValue().withL(conversations.stream()
                                .map(msg -> new AttributeValue().withM(msg))
                                .collect(Collectors.toList())));

                        PutItemRequest putItemRequest = new PutItemRequest()
                                .withTableName("Conversation")
                                .withItem(item);

                        ddbClient.putItem(putItemRequest);
                    } else {
                        // Nếu cuộc trò chuyện đã tồn tại, kiểm tra và cập nhật hoặc thêm mới tin nhắn
                        List<AttributeValue> existingConversations = getItemResult.getItem().get("conversations").getL();
                        boolean conversationExists = false;

                        // Kiểm tra xem cuộc trò chuyện đã tồn tại hay chưa bằng cách so sánh conversationID
                        for (AttributeValue conversation : existingConversations) {
                            String existingConversationID = conversation.getM().get("conversationID").getS();
                            if (existingConversationID.equals(conversationID)) {
                                // Nếu cuộc trò chuyện đã tồn tại, cập nhật nội dung
                                conversation.getM().put("friendID", new AttributeValue().withS(friendID));
                                conversation.getM().put("time", new AttributeValue().withS(time));
                                conversation.getM().put("message", new AttributeValue().withS(message));
                                conversation.getM().put("avatar", new AttributeValue().withS(avatar));
                                conversation.getM().put("name", new AttributeValue().withS(name));
                                conversationExists = true;
                                break;
                            }
                        }

                        if (!conversationExists) {
                            // Nếu cuộc trò chuyện chưa tồn tại, thêm mới cuộc trò chuyện vào mảng
                            Map<String, AttributeValue> conversationItem = new HashMap<>();
                            conversationItem.put("conversationID", new AttributeValue().withS(conversationID));
                            conversationItem.put("friendID", new AttributeValue().withS(friendID));
                            conversationItem.put("time", new AttributeValue().withS(time));
                            conversationItem.put("message", new AttributeValue().withS(message));
                            conversationItem.put("avatar", new AttributeValue().withS(avatar));
                            conversationItem.put("name", new AttributeValue().withS(name));
                            existingConversations.add(new AttributeValue().withM(conversationItem));
                        }

                        // Cập nhật item trong bảng DynamoDB
                        Map<String, AttributeValueUpdate> updates = new HashMap<>();
                        updates.put("conversations", new AttributeValueUpdate()
                                .withAction(AttributeAction.PUT)
                                .withValue(new AttributeValue().withL(existingConversations)));

                        UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                                .withTableName("Conversation")
                                .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userUID)))
                                .withAttributeUpdates(updates);

                        ddbClient.updateItem(updateItemRequest);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Gọi callback nếu có lỗi xảy ra
        }
    }

    public void getChannelID(String id, String _idFriend, ChannelIDinterface listener) {
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
                                String friendID = friend.getM().get("_idFriend").getS();
                                if (friendID.equals(_idFriend)) {
                                    String channel_id = friend.getM().get("channel_id").getS(); // Lấy giá trị của thuộc tính "_idFriend"
                                    Log.d("channel_id", channel_id);
                                    listener.GetChannelId(channel_id);
//                                    findFriendByID(friendId,listener);
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

    public interface ChannelIDinterface {

        void GetChannelId(String channelID);

    }

    public List<Item> loadMessages(String messageId) {
        List<Item> messages = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1); // Khởi tạo CountDownLatch với giá trị ban đầu là 1
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ddbClient == null) {
                        initializeDynamoDB();
                    }
                    GetItemRequest getItemRequest = new GetItemRequest()
                            .withTableName("ChatHistory")
                            .withKey(Collections.singletonMap("_id", new AttributeValue().withS(messageId)));
                    GetItemResult getItemResult = ddbClient.getItem(getItemRequest);
                    if (getItemResult.getItem() != null) {
                        // Nếu cuộc trò chuyện đã tồn tại, trích xuất tin nhắn và load lên giao diện
                        Map<String, AttributeValue> item = getItemResult.getItem();
                        AttributeValue messagesAttributeValue = item.get("messages");
                        if (messagesAttributeValue != null) {
                            List<AttributeValue> messagesAttributeList = messagesAttributeValue.getL();
                            if (messagesAttributeList != null) {
                                for (AttributeValue messageAttribute : messagesAttributeList) {
                                    Map<String, AttributeValue> messageMap = messageAttribute.getM();
                                    String message = messageMap.get("message").getS();
                                    String time = messageMap.get("time").getS();
                                    Boolean sentByMe = Boolean.parseBoolean(messageMap.get("sentByMe").getBOOL().toString());
                                    Item newItem = new Item(time, message, sentByMe);
                                    messages.add(newItem);
                                }
                            }
                        }
                    } else {
                        // Nếu cuộc trò chuyện chưa tồn tại, tạo mới và lưu vào DynamoDB
                    }
                } catch (Exception e) {
                    // Gọi callback nếu có lỗi xảy ra
                } finally {
                    latch.countDown(); // Giảm số lượng của CountDownLatch về 0 khi luồng hoàn thành công việc
                }
            }
        }).start(); // Khởi chạy thread

        try {
            latch.await(); // Đợi cho đến khi CountDownLatch đếm về 0
        } catch (InterruptedException e) {
            // Xử lý ngoại lệ nếu có
        }

        return messages;
    }
    public interface ConversationLoadListener {
        void onConversationLoaded(Conversation conversation);
    }

    public void loadConversations(String conversationID, ConversationLoadListener listener) {
        Log.d("CheckIdOfLoadConversation", conversationID);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ddbClient == null) {
                        initializeDynamoDB();
                    }
                    GetItemRequest getItemRequest = new GetItemRequest()
                            .withTableName("Conversation")
                            .withKey(Collections.singletonMap("_id", new AttributeValue().withS(conversationID)));
                    GetItemResult getItemResult = ddbClient.getItem(getItemRequest);
                    if (getItemResult.getItem() != null) {
                        // Nếu cuộc trò chuyện đã tồn tại, trích xuất tin nhắn và load lên giao diện
                        Map<String, AttributeValue> item = getItemResult.getItem();
                        AttributeValue conversationsAttributeValue = item.get("conversations");
                        if (conversationsAttributeValue != null) {
                            List<AttributeValue> conversationsAttributeList = conversationsAttributeValue.getL();
                            if (conversationsAttributeList != null) {
                                for (AttributeValue messageAttribute : conversationsAttributeList) {
                                    Map<String, AttributeValue> messageMap = messageAttribute.getM();
                                    String conversationID = messageMap.get("conversionID").getS();
                                    String friendID=messageMap.get("friendID").getS();
                                    String message = messageMap.get("message").getS();
                                    String time = messageMap.get("time").getS();
                                    String avatar = messageMap.get("avatar").getS();
                                    String name = messageMap.get("name").getS();
                                    Conversation conversation = new Conversation(conversationID,friendID, message, time, avatar, name);

                                    // Thông báo về việc tìm thấy cuộc trò chuyện
                                    listener.onConversationLoaded(conversation);
                                }
                            }

                        }
                    } else {
                        // Nếu cuộc trò chuyện chưa tồn tại, tạo mới và lưu vào DynamoDB
                    }
                } catch (Exception e) {
                    // Xử lý ngoại lệ nếu có
                }
            }
        }).start(); // Khởi chạy thread
    }
    public void loadConversation1(String userUID, LoadConversationListener listener) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Tạo một yêu cầu truy vấn
                        if (ddbClient == null) {
                            initializeDynamoDB();
                        }
                        GetItemRequest getItemRequest = new GetItemRequest()
                                .withTableName("Conversation")
                                .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userUID)));
                        GetItemResult getItemResult = ddbClient.getItem(getItemRequest);
                        if (getItemResult.getItem() != null) {
                            // Nếu cuộc trò chuyện đã tồn tại, trích xuất tin nhắn và load lên giao diện
                            Map<String, AttributeValue> item = getItemResult.getItem();
                            AttributeValue conversationsAttributeValue = item.get("conversations");
                            if (conversationsAttributeValue != null) {
                                List<AttributeValue> conversationsAttributeList = conversationsAttributeValue.getL();
                                if (conversationsAttributeList != null) {
                                    for (AttributeValue messageAttribute : conversationsAttributeList) {
                                        Map<String, AttributeValue> messageMap = messageAttribute.getM();
                                        String conversationID = messageMap.get("conversationID").getS();
                                        String friendID= messageMap.get("friendID").getS();
                                        String message = messageMap.get("message").getS();
                                        String time = messageMap.get("time").getS();
                                        String avatar = messageMap.get("avatar").getS();
                                        String name = messageMap.get("name").getS();
                                        Conversation conversation = new Conversation(conversationID, friendID,message, time, avatar, name);

                                        // Thông báo về việc tìm thấy cuộc trò chuyện
                                        listener.onConversationFound(conversationID, friendID,message,time, avatar,name);
                                    }
                                }

                            }
                        } else {
                            // Nếu cuộc trò chuyện chưa tồn tại, tạo mới và lưu vào DynamoDB
                        }
                    } catch (Exception e) {
                        // Xử lý ngoại lệ và thông báo lỗi cho người nghe
                        Log.e("", "Error loading conversation: " + e.getMessage());
                        listener.onLoadConversationError(e);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Xử lý ngoại lệ và thông báo lỗi cho người nghe
            Log.e("", "Error checking DynamoDB connection: " + e.getMessage());
            listener.onLoadConversationError(e);
        }
    }

    public interface LoadConversationListener {
        void onConversationFound(String conversationID, String friendID ,String message, String time, String avatar, String name);
        void onLoadConversationError(Exception e);
    }
}