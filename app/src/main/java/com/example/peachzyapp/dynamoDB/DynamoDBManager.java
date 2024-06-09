package com.example.peachzyapp.dynamoDB;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
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
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemResult;
import com.example.peachzyapp.Regexp.Regexp;
import com.example.peachzyapp.entities.Conversation;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.entities.GroupChat;
import com.example.peachzyapp.entities.GroupConversation;
import com.example.peachzyapp.entities.Item;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class DynamoDBManager {
    private Context context;
    private AmazonDynamoDBClient ddbClient;
    private Regexp regexp;

    public DynamoDBManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.context = context;
    }

    private static AWSCredentials getAWSCredentials() {
        return new BasicAWSCredentials("mykey", "mykey");
    }

    private void initializeDynamoDB() {
        try {
            BasicAWSCredentials credentials = new BasicAWSCredentials("mykey", "mykey");
            ddbClient = new AmazonDynamoDBClient(credentials);
            ddbClient.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1)); // Set the region
            ListTablesResult tables = ddbClient.listTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //I Đăng nhập, Đăng ký, Quên mật khẩu
    //1 check kết nối
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

    //2 tạo tài khoản
    public void createAccountWithFirebaseUID(String firebaseUID, String firstName, String lastName, String email, String dateOfBirth, Boolean sex) {

        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
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
    }

    //II Thông tin cá nhân
    //1 lấy thông tin cá nhân
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
                            String role = null;
                            // Log dữ liệu
                            Log.d("userResult", userResult);
                            listener.onFriendFound(uid, name, email, avatar, sex, dateOfBirth, role);
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

    //2 cập nhật thông tin cá nhân
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

    //3 lấy avatar hiện có để cập nhật nếu k đổi avatar
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

    //III Liên quan tới bạn bè
    //1 lấy trạng thái 1 là đã kết bạn rồi, 2 là đã gửi lời mời 3 nhận được lời mời kết bạn
    public void getStatusByFriendId(String myId, String friendId, StatusListener listener) {
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
                                .withAttributeValueList(new AttributeValue().withS(myId));
                        scanFilter.put("_id", condition);

                        Log.d("CheckgetStatusByFriendId", myId + friendId + "run");
                        ScanRequest scanRequest = new ScanRequest("Users").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);
                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            List<AttributeValue> friendsList = item.get("friends").getL();
                            for (AttributeValue friend : friendsList) {
                                String friendIdFromList = friend.getM().get("_idFriend").getS();
                                if (friendId.equals(friendIdFromList)) {
                                    String status = friend.getM().get("status").getS(); // Lấy giá trị của thuộc tính "status"
                                    // Gọi phương thức listener để trả về status
                                    listener.onStatusFetched(status);
                                    return;
                                }
                            }
                        }
                        // Nếu không tìm thấy friendId trong mảng friends
                        listener.onStatusFetched(null); // Trả về null nếu không tìm thấy status
                    } catch (Exception e) {
                        // Xử lý ngoại lệ
                        e.printStackTrace();
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Xử lý ngoại lệ
            Log.e("", "Error checking DynamoDB connection: " + e.getMessage());
        }
    }

    public interface StatusListener {
        void onStatusFetched(String status);
    }

    //2 Gửi lời mời kết bạn, 3 Chấp nhận lời mời
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

    //4 Hủy lời mời kết bạn đã gửi,   5 Xóa lời mời kết bại được nhận
    public void deleteAFriendFromUser(String userID, String friendID) {
        try {
            if (ddbClient == null) {
                // Khởi tạo DynamoDB client nếu nó chưa được khởi tạo
                initializeDynamoDB();
            }

            // Bắt đầu một thread mới để thực hiện công việc này
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Truy vấn người dùng từ bảng Users
                        GetItemRequest getItemRequest = new GetItemRequest()
                                .withTableName("Users")
                                .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userID)));

                        GetItemResult getItemResult = ddbClient.getItem(getItemRequest);

                        Map<String, AttributeValue> item = getItemResult.getItem();

                        // Kiểm tra nếu item không null và có chứa key "groups"
                        if (item != null && item.containsKey("friends")) {
                            AttributeValue groupsAttributeValue = item.get("friends");
                            List<Map<String, AttributeValue>> groups = new ArrayList<>();
                            if (groupsAttributeValue.getL() != null) {
                                for (AttributeValue av : groupsAttributeValue.getL()) {
                                    if (av.getM() != null) {
                                        groups.add(av.getM());
                                    }
                                }
                            }

                            // Duyệt qua từng phần tử trong mảng groups để tìm và xóa groupID cần xóa
                            for (Iterator<Map<String, AttributeValue>> iterator = groups.iterator(); iterator.hasNext(); ) {
                                Map<String, AttributeValue> group = iterator.next();
                                if (group.containsKey("_idFriend") && group.get("_idFriend").getS().equals(friendID)) {
                                    iterator.remove(); // Xóa phần tử khỏi mảng nếu tìm thấy groupID
                                }
                            }

                            // Cập nhật lại giá trị của mảng groups trong item
                            List<AttributeValue> updatedGroups = new ArrayList<>();
                            for (Map<String, AttributeValue> group : groups) {
                                updatedGroups.add(new AttributeValue().withM(group));
                            }

                            // Tạo yêu cầu cập nhật
                            UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                                    .withTableName("Users")
                                    .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userID)))
                                    .withAttributeUpdates(Collections.singletonMap(
                                            "friends",
                                            new AttributeValueUpdate().withValue(new AttributeValue().withL(updatedGroups)).withAction(AttributeAction.PUT)
                                    ));

                            // Thực hiện cập nhật
                            ddbClient.updateItem(updateItemRequest);
                        }

                    } catch (Exception e) {
                        // Ghi log cho lỗi để debug
                        System.err.println("Exception occurred: " + e);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Log exception for debugging
            System.err.println("Error checking DynamoDB connection: " + e.getMessage());
        }
    }

    //5.1 hủy kết bạn ver 2
    public void unFriend(final String userId, final String friendId) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        GetItemRequest getItemRequest = new GetItemRequest()
                                .withTableName("Users")
                                .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userId)));

                        GetItemResult getItemResult = ddbClient.getItem(getItemRequest);

                        Map<String, AttributeValue> item = getItemResult.getItem();


                        if (item != null && item.containsKey("friends")) {
                            AttributeValue friendsAttributeValue = item.get("friends");
                            List<Map<String, AttributeValue>> friends = new ArrayList<>();
                            if (friendsAttributeValue.getL() != null) {
                                for (AttributeValue av : friendsAttributeValue.getL()) {
                                    if (av.getM() != null) {
                                        friends.add(av.getM());
                                    }
                                }
                            }


                            for (Iterator<Map<String, AttributeValue>> iterator = friends.iterator(); iterator.hasNext(); ) {
                                Map<String, AttributeValue> friend = iterator.next();
                                if (friend.containsKey("_idFriend") && friend.get("_idFriend").getS().equals(friendId)) {
                                    iterator.remove();
                                }
                            }

                            // Cập nhật lại giá trị của mảng groups trong item
                            List<AttributeValue> updatedFriends = new ArrayList<>();
                            for (Map<String, AttributeValue> friend : friends) {
                                updatedFriends.add(new AttributeValue().withM(friend));
                            }

                            // Tạo yêu cầu cập nhật
                            UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                                    .withTableName("Users")
                                    .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userId)))
                                    .withAttributeUpdates(Collections.singletonMap(
                                            "friends",
                                            new AttributeValueUpdate().withValue(new AttributeValue().withL(updatedFriends)).withAction(AttributeAction.PUT)
                                    ));

                            // Thực hiện cập nhật
                            ddbClient.updateItem(updateItemRequest);
                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //6 tìm bạn để kết bạn
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

    //7 Kiểm tra xem đã là bạn chưa
    public void checkAlreadyFriend(String id, String friendId, CheckAlreadyFriendListener listener) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean isFriend = false; // Biến này sẽ được set thành true nếu tìm thấy idFriend trong danh sách bạn bè

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
                                String idFriend = friend.getM().get("_idFriend").getS();
                                if (idFriend.equals(friendId)) {
                                    isFriend = true;
                                    break;
                                }
                            }
                            if (isFriend) {
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.e("", "Error checking DynamoDB connection: " + e.getMessage());
                    }

                    // Trả về kết quả
                    if (listener != null) {
                        listener.onCheckComplete(isFriend);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            Log.e("", "Error checking DynamoDB connection: " + e.getMessage());
        }
    }

    public interface CheckAlreadyFriendListener {
        void onCheckComplete(boolean isFriend);
    }

    //8 load danh sách bạn bè với 1 là bạn bè, 2 là lời mời kết bạn user đã gửi đi và 3 là lời mời kết bạn đc nhận
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
    //9 xóa cuộc hội thoại của cả 2 người khi xóa kết bạn
    public void deleteConversation(String id,String friendID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ddbClient == null) {
                        initializeDynamoDB();
                    }
                    // Tạo yêu cầu xóa dựa trên _id của bản ghi và khóa phụ friendID
                    Map<String, AttributeValue> key = new HashMap<>();
                    key.put("_id", new AttributeValue().withS(id));
                    key.put("friendID", new AttributeValue().withS(friendID));
                    // Tạo yêu cầu xóa dựa trên _id của bản ghi
                    DeleteItemRequest deleteRequest = new DeleteItemRequest()
                            .withTableName("Conversation")
                            .withKey(key);
                    ddbClient.deleteItem(deleteRequest);
                } catch (Exception e) {
                    // Xử lý ngoại lệ và thông báo lỗi cho người nghe
                    Log.e("", "Error deleting group conversation: " + e.getMessage());
                }
            }
        }).start(); // Khởi chạy thread
    }
    //IV chat 1-1
    //1 lấy kênh chat websocket
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

    //2 Lưu cuộc trò chuyện
    public void saveConversation(String userUID, String friendID, String message, String time, String avatar, String name) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ddbClient == null) {
                            initializeDynamoDB();
                        }
                        Map<String, AttributeValue> item = new HashMap<>();
                        item.put("_id", new AttributeValue().withS(userUID));
                        item.put("friendID", new AttributeValue().withS(friendID));
                        item.put("avatar", new AttributeValue().withS(avatar));
                        item.put("message", new AttributeValue().withS(message));
                        item.put("name", new AttributeValue().withS(name));
                        item.put("time", new AttributeValue().withS(time));
                        // Tạo yêu cầu chèn mục vào bảng
                        PutItemRequest putItemRequest = new PutItemRequest()
                                .withTableName("Conversation")
                                .withItem(item);
                        // Thực hiện yêu cầu chèn mục và nhận kết quả
                        PutItemResult result = ddbClient.putItem(putItemRequest);
                    } catch (Exception e) {
                        // Xử lý ngoại lệ và thông báo lỗi cho người nghe
                        Log.e("", "Error loading conversation: " + e.getMessage());
                    }
                }

            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Gọi callback nếu có lỗi xảy ra
        }
    }

    //3 lưu tin nhắn giữa 2 người
    public void saveMessageOneToOne(String channelID, String message, String time, String type, String UID, String friendID) {
        Log.d("SaveMessageInfo824", channelID + ":" + message + ":" + time + ":" + type + ":" + UID + ":" + friendID + ":");
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
                        messageItem.put("text", new AttributeValue().withS(message));
                        messageItem.put("type", new AttributeValue().withS(type));
                        messageItem.put("from", new AttributeValue().withS(UID));
                        messageItem.put("to", new AttributeValue().withS(friendID));
                        messages.add(messageItem);
                        item.put("messages", new AttributeValue().withL(messages.stream()
                                .map(msg -> new AttributeValue().withM(msg))
                                .collect(Collectors.toList())));
                        List<AttributeValue> participantsList = new ArrayList<>();
                        participantsList.add(new AttributeValue().withS(UID));
                        participantsList.add(new AttributeValue().withS(friendID));
                        item.put("participants", new AttributeValue().withL(participantsList));
                        PutItemRequest putItemRequest = new PutItemRequest()
                                .withTableName("ChatHistory")
                                .withItem(item);
                        ddbClient.putItem(putItemRequest);
                    } else {
                        // Nếu cuộc trò chuyện đã tồn tại, thêm tin nhắn mới vào mảng messages
                        Map<String, AttributeValueUpdate> updates = new HashMap<>();
                        Map<String, AttributeValue> messageItem = new HashMap<>();
                        messageItem.put("time", new AttributeValue().withS(time));
                        messageItem.put("text", new AttributeValue().withS(message));
                        messageItem.put("type", new AttributeValue().withS(type));
                        messageItem.put("from", new AttributeValue().withS(UID));
                        messageItem.put("to", new AttributeValue().withS(friendID));
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
        }
    }

    //4 load danh sách cuộc hội thoại giữa 1-1
    public void loadConversation(String userUID, LoadConversationListener listener) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
                        Condition condition = new Condition()
                                .withComparisonOperator(ComparisonOperator.EQ.toString())
                                .withAttributeValueList(new AttributeValue().withS(userUID));
                        scanFilter.put("_id", condition);

                        ScanRequest scanRequest = new ScanRequest("Conversation").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);

                        // Xử lý kết quả
                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            String id = item.get("_id").getS();
                            String friendID = item.get("friendID").getS();
                            String name = item.get("name").getS();
                            String avatar = item.get("avatar").getS();
                            String message = item.get("message").getS();
                            String time = item.get("time").getS();
                            // Cập nhật giao diện
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onConversationFound(id, friendID, message, time, avatar, name);
                                }
                            });
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
        void onConversationFound(String conversationID, String friendID, String message, String time, String avatar, String name);

        void onLoadConversationError(Exception e);
    }

    //5 load tin nhắn 2 người đã gửi
    public List<Item> loadMessagesOneToOne(String channelId, String uid) {
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
                            .withKey(Collections.singletonMap("_id", new AttributeValue().withS(channelId)));
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
                                    String message = messageMap.get("text").getS();
                                    String time = messageMap.get("time").getS();
                                    String from = messageMap.get("from").getS();
                                    String type = messageMap.get("type").getS();
                                    Boolean sentByMe = false;
                                    if (from.equals(uid)) {
                                        sentByMe = true;
                                    }
                                    Item newItem = new Item(time, message, sentByMe, type);
                                    Log.d("loadMessagesOneToOne", message + ":" + time + ":" + sentByMe);
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

    public interface FriendFoundListener {
        void onFriendFound(String id, String friendResult, String avatar);

        void onFriendNotFound();

        void onError(Exception e);
    }

    //V chat nhóm
    //1 tạo nhóm chat
    //giải thích Tạo table group trong table Groups. Để quản lý thành viên qua memberID trong mảng members. Dùng để truy vấn memberID= _id(userID) thông qua User
    public void createGroup(String groupID, List<String> memberIDs) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ddbClient == null) {
                            initializeDynamoDB();
                        }
                        Map<String, AttributeValue> item = new HashMap<>();
                        item.put("_id", new AttributeValue().withS(groupID));

                        // Tạo danh sách thành viên
                        List<AttributeValue> memberList = new ArrayList<>();
                        for (String memberID : memberIDs) {
                            Map<String, AttributeValue> memberMap = new HashMap<>();
                            memberMap.put("memberID", new AttributeValue().withS(memberID));
                            memberList.add(new AttributeValue().withM(memberMap));
                        }
                        item.put("members", new AttributeValue().withL(memberList));

                        // Tạo yêu cầu chèn mục vào bảng
                        PutItemRequest putItemRequest = new PutItemRequest()
                                .withTableName("Groups")
                                .withItem(item);

                        // Thực hiện yêu cầu chèn mục và nhận kết quả
                        PutItemResult result = ddbClient.putItem(putItemRequest);
                    } catch (Exception e) {
                        // Xử lý ngoại lệ và thông báo lỗi cho người nghe
                        Log.e("", "Error loading conversation: " + e.getMessage());
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Gọi callback nếu có lỗi xảy ra
        }
    }

    //2 Tạo cuộc hội thoại nhóm chat
    //1 lưu trữ groupConversation để hiển thị cuộc trò chuyện-> ẩn vào để chat nhóm
    public void saveGroupConversation(String groupID, String message, String groupName, String time, String avatar, String name) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ddbClient == null) {
                            initializeDynamoDB();
                        }
                        Map<String, AttributeValue> item = new HashMap<>();
                        item.put("_id", new AttributeValue().withS(groupID));
                        item.put("avatar", new AttributeValue().withS(avatar));
                        item.put("groupName", new AttributeValue().withS(groupName));
                        item.put("message", new AttributeValue().withS(message));
                        item.put("name", new AttributeValue().withS(name));
                        item.put("time", new AttributeValue().withS(time));
                        // Tạo yêu cầu chèn mục vào bảng
                        PutItemRequest putItemRequest = new PutItemRequest()
                                .withTableName("GroupConversation")
                                .withItem(item);

                        // Thực hiện yêu cầu chèn mục và nhận kết quả
                        PutItemResult result = ddbClient.putItem(putItemRequest);
                    } catch (Exception e) {
                        // Xử lý ngoại lệ và thông báo lỗi cho người nghe
                        Log.e("", "Error loading conversation: " + e.getMessage());
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Gọi callback nếu có lỗi xảy ra
        }
    }

    //3 cập nhật nhóm vừa tạo cho mỗi người
    public void updateGroupForAccount(String userID, String groupID, String role) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        GetItemRequest getItemRequest = new GetItemRequest()
                                .withTableName("Users")
                                .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userID)));

                        GetItemResult getItemResult = ddbClient.getItem(getItemRequest);

                        Map<String, AttributeValue> item = getItemResult.getItem();
                        List<AttributeValue> existingConversations;

                        // Kiểm tra xem bảng có mảng "groups" chưa
                        if (item.containsKey("groups")) {
                            existingConversations = item.get("groups").getL();
                        } else {
                            // Nếu chưa có, tạo mảng "groups" mới
                            existingConversations = new ArrayList<>();
                        }

                        boolean conversationExists = false;

                        for (AttributeValue conversation : existingConversations) {
                            String existingConversationID = conversation.getM().get("groupID").getS();
                            if (existingConversationID.equals(groupID)) {
                                // Nếu cuộc trò chuyện đã tồn tại, cập nhật nội dung
                                conversationExists = true;
                                break;
                            }
                        }

                        if (!conversationExists) {
                            // Nếu cuộc trò chuyện chưa tồn tại, thêm mới cuộc trò chuyện vào mảng
                            Map<String, AttributeValue> groupItem = new HashMap<>();
                            groupItem.put("groupID", new AttributeValue().withS(groupID));
                            groupItem.put("role", new AttributeValue().withS(role));
                            existingConversations.add(new AttributeValue().withM(groupItem));
                        }
                        // Cập nhật item trong bảng DynamoDB
                        Map<String, AttributeValueUpdate> updates = new HashMap<>();
                        updates.put("groups", new AttributeValueUpdate()
                                .withAction(AttributeAction.PUT)
                                .withValue(new AttributeValue().withL(existingConversations)));
                        UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                                .withTableName("Users")
                                .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userID)))
                                .withAttributeUpdates(updates);
                        Log.d("CheckAddMember", String.valueOf(updateItemRequest));
                        ddbClient.updateItem(updateItemRequest);
                        String s = String.valueOf(ddbClient.updateItem(updateItemRequest));
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

    //4 lưu tin nhắn nhóm
    public void saveGroupMessage(String groupID, String message, String time, String memberID, String avatar, String memberName, String type) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    GetItemRequest getItemRequest = new GetItemRequest()
                            .withTableName("GroupChatHistory")
                            .withKey(Collections.singletonMap("_id", new AttributeValue().withS(groupID)));
                    GetItemResult getItemResult = ddbClient.getItem(getItemRequest);

                    if (getItemResult.getItem() == null) {
                        // Nếu cuộc trò chuyện chưa tồn tại, tạo mới và lưu vào DynamoDB
                        Map<String, AttributeValue> item = new HashMap<>();
                        item.put("_id", new AttributeValue().withS(groupID));
                        List<Map<String, AttributeValue>> messages = new ArrayList<>();
                        Map<String, AttributeValue> messageItem = new HashMap<>();
                        messageItem.put("time", new AttributeValue().withS(time));
                        messageItem.put("message", new AttributeValue().withS(message));
                        messageItem.put("memberID", new AttributeValue().withS(memberID));
                        messageItem.put("memberAvatar", new AttributeValue().withS(avatar));
                        //messageItem.put("avatar", new AttributeValue().withS(avatar));
                        messageItem.put("memberName", new AttributeValue().withS(memberName));
                        messageItem.put("type", new AttributeValue().withS(type));
                        messages.add(messageItem);
                        item.put("messages", new AttributeValue().withL(messages.stream()
                                .map(msg -> new AttributeValue().withM(msg))
                                .collect(Collectors.toList())));

                        PutItemRequest putItemRequest = new PutItemRequest()
                                .withTableName("GroupChatHistory")
                                .withItem(item);

                        ddbClient.putItem(putItemRequest);
                        //đồng thời tạo lu

                    } else {
                        // Nếu cuộc trò chuyện đã tồn tại, thêm tin nhắn mới vào mảng messages
                        Map<String, AttributeValueUpdate> updates = new HashMap<>();
                        Map<String, AttributeValue> messageItem = new HashMap<>();
                        messageItem.put("time", new AttributeValue().withS(time));
                        messageItem.put("message", new AttributeValue().withS(message));
                        messageItem.put("memberID", new AttributeValue().withS(memberID));
                        messageItem.put("memberAvatar", new AttributeValue().withS(avatar));
                        //messageItem.put("avatar", new AttributeValue().withS(avatar));
                        messageItem.put("memberName", new AttributeValue().withS(memberName));
                        messageItem.put("type", new AttributeValue().withS(type));
                        updates.put("messages", new AttributeValueUpdate()
                                .withAction(AttributeAction.ADD)
                                .withValue(new AttributeValue().withL(Collections.singletonList(new AttributeValue().withM(messageItem)))));

                        UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                                .withTableName("GroupChatHistory")
                                .withKey(Collections.singletonMap("_id", new AttributeValue().withS(groupID)))
                                .withAttributeUpdates(updates);

                        ddbClient.updateItem(updateItemRequest);
                    }

                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Gọi callback nếu có lỗi xảy ra

        }
    }
    //5 load tin nhắn của nhóm
    //load tin nhắn group
    public List<GroupChat> loadGroupMessages(String messageId) {
        List<GroupChat> messages = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1); // Khởi tạo CountDownLatch với giá trị ban đầu là 1
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ddbClient == null) {
                        initializeDynamoDB();
                    }
                    GetItemRequest getItemRequest = new GetItemRequest()
                            .withTableName("GroupChatHistory")
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
                                    //String avatar = messageMap.get("avatar").getS();
                                    String avatar = messageMap.get("memberAvatar").getS();
                                    String memberID = messageMap.get("memberID").getS();
                                    String memberName = messageMap.get("memberName").getS();
                                    String message = messageMap.get("message").getS();
                                    String time = messageMap.get("time").getS();
                                    String type = messageMap.get("type").getS();
                                    GroupChat groupChat = new GroupChat(avatar, message, memberName, time, memberID, type);
                                    messages.add(groupChat);
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

    //6 load các cuộc hội thoại cũng như các nhóm chat
    public void loadGroupList(String id, LoadGroupListListener listener) {
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
                            List<AttributeValue> groupList = item.get("groups").getL();
                            for (AttributeValue group : groupList) {
                                String groupID = group.getM().get("groupID").getS();
                                Log.d("checkIDGroupLoad", groupID);
                                loadGroup(groupID, listener);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            Log.e("", "Error checking DynamoDB connection: " + e.getMessage());
        }
    }

    public void loadGroup(String id, LoadGroupListListener listener) {
        try {
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

                        ScanRequest scanRequest = new ScanRequest("GroupConversation").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);

                        // Xử lý kết quả
                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            String id = item.get("_id").getS();
                            String groupName = item.get("groupName").getS();
                            String name = item.get("name").getS();
                            String avatar = item.get("avatar").getS();
                            String message = item.get("message").getS();
                            String time = item.get("time").getS();

                            // Cập nhật giao diện
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onGroupListFound(id, groupName, avatar, message, name, time);
                                }
                            });

                            Log.d("groupName", groupName);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //7 lấy thông tin thành viên đặc biệt là lấy role ra để xác định nhóm trưởng và member
    public void getGroupInfoByUser(String userID, String groupID, LoadGroupInfoListener listener) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Tạo một yêu cầu truy vấn để lấy thông tin từ bảng Users
                        HashMap<String, Condition> scanFilter = new HashMap<>();
                        Condition condition = new Condition()
                                .withComparisonOperator(ComparisonOperator.EQ.toString())
                                .withAttributeValueList(new AttributeValue().withS(userID));
                        scanFilter.put("_id", condition);

                        ScanRequest scanRequest = new ScanRequest("Users").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);

                        // Lấy thông tin từ kết quả truy vấn
                        List<Map<String, AttributeValue>> items = scanResult.getItems();
                        if (!items.isEmpty()) {
                            Map<String, AttributeValue> userItem = items.get(0); // Giả sử chỉ có một mục với userID đã cho

                            // Lấy mảng groups của user
                            AttributeValue groupsAttributeValue = userItem.get("groups");
                            if (groupsAttributeValue != null && groupsAttributeValue.getL() != null) {
                                List<AttributeValue> groupsList = groupsAttributeValue.getL();
                                for (AttributeValue groupValue : groupsList) {
                                    Map<String, AttributeValue> groupMap = groupValue.getM(); // Lấy thông tin của một nhóm
                                    String groupIDFromUser = groupMap.get("groupID").getS(); // Lấy groupID từ thông tin nhóm
                                    if (groupIDFromUser.equals(groupID)) {
                                        String role = groupMap.get("role").getS(); // Lấy vai trò từ thông tin nhóm
                                        // Gửi thông tin về vai trò trong nhóm qua listener
                                        listener.onGroupInfoLoaded(role);
                                        return;
                                    }
                                }
                            }
                        }
                        // Nếu không tìm thấy thông tin về groupID hoặc role, gửi thông báo thông tin không có
                        listener.onGroupInfoNotFound();
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Gửi thông báo lỗi qua listener
                        listener.onLoadError(e);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            Log.e("", "Error checking DynamoDB connection: " + e.getMessage());
        }
    }

    public interface LoadGroupInfoListener {
        void onGroupInfoLoaded(String role);

        void onGroupInfoNotFound();

        void onLoadError(Exception e);
    }

    //8 xóa group
    public void deleteGroup(String groupID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ddbClient == null) {
                        initializeDynamoDB();
                    }

                    // Tạo yêu cầu xóa dựa trên _id của bản ghi
                    DeleteItemRequest deleteRequest = new DeleteItemRequest()
                            .withTableName("Groups")
                            .withKey(Collections.singletonMap("_id", new AttributeValue().withS(groupID)));

                    ddbClient.deleteItem(deleteRequest);
                } catch (Exception e) {
                    // Xử lý ngoại lệ và thông báo lỗi cho người nghe
                    Log.e("", "Error deleting group conversation: " + e.getMessage());
                }
            }
        }).start(); // Khởi chạy thread
    }

    //9 xóa cuộc hội thoại của nhóm
    public void deleteGroupConversation(String groupID) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ddbClient == null) {
                        initializeDynamoDB();
                    }

                    // Tạo yêu cầu xóa dựa trên _id của bản ghi
                    DeleteItemRequest deleteRequest = new DeleteItemRequest()
                            .withTableName("GroupConversation")
                            .withKey(Collections.singletonMap("_id", new AttributeValue().withS(groupID)));

                    ddbClient.deleteItem(deleteRequest);
                } catch (Exception e) {
                    // Xử lý ngoại lệ và thông báo lỗi cho người nghe
                    Log.e("", "Error deleting group conversation: " + e.getMessage());
                }
            }
        }).start(); // Khởi chạy thread
    }

    //10 xóa thành viên ra khỏi nhóm
    //xóa memberID trong mảng members của mỗi group của bảng Groups
    public void deleteUserFromGroup(String groupID, String userID) {
        try {
            if (ddbClient == null) {
                // Khởi tạo DynamoDB client nếu nó chưa được khởi tạo
                initializeDynamoDB();
            }

            // Bắt đầu một thread mới để thực hiện công việc này
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Truy vấn nhóm từ bảng Groups
                        GetItemRequest getItemRequest = new GetItemRequest()
                                .withTableName("Groups")
                                .withKey(Collections.singletonMap("_id", new AttributeValue().withS(groupID)));

                        GetItemResult getItemResult = ddbClient.getItem(getItemRequest);

                        Map<String, AttributeValue> item = getItemResult.getItem();

                        if (item != null && item.containsKey("members")) {
                            AttributeValue membersAttributeValue = item.get("members");
                            List<Map<String, AttributeValue>> members = new ArrayList<>();
                            if (membersAttributeValue.getL() != null) {
                                for (AttributeValue av : membersAttributeValue.getL()) {
                                    if (av.getM() != null) {
                                        members.add(av.getM());
                                    }
                                }
                            }

                            // Duyệt qua từng phần tử trong mảng members để tìm và xóa memberID cần xóa
                            for (Iterator<Map<String, AttributeValue>> iterator = members.iterator(); iterator.hasNext(); ) {
                                Map<String, AttributeValue> member = iterator.next();
                                if (member.containsKey("memberID") && member.get("memberID").getS().equals(userID)) {
                                    iterator.remove(); // Xóa phần tử khỏi mảng nếu tìm thấy userID
                                }
                            }

                            // Cập nhật lại giá trị của mảng members trong item
                            List<AttributeValue> updatedMembers = new ArrayList<>();
                            for (Map<String, AttributeValue> member : members) {
                                updatedMembers.add(new AttributeValue().withM(member));
                            }

                            // Tạo yêu cầu cập nhật
                            UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                                    .withTableName("Groups")
                                    .withKey(Collections.singletonMap("_id", new AttributeValue().withS(groupID)))
                                    .withAttributeUpdates(Collections.singletonMap(
                                            "members",
                                            new AttributeValueUpdate().withValue(new AttributeValue().withL(updatedMembers)).withAction(AttributeAction.PUT)
                                    ));

                            // Thực hiện cập nhật
                            ddbClient.updateItem(updateItemRequest);
                        }

                    } catch (Exception e) {
                        // Ghi log cho lỗi để debug
                        System.err.println("Exception occurred: " + e);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Log exception for debugging
            System.err.println("Error checking DynamoDB connection: " + e.getMessage());
        }
    }

    //11 xóa group khỏi thành viên
    //xóa groupID trong user của bảng Users
    public void deleteGroupFromUser(String userID, String groupID) {
        try {
            if (ddbClient == null) {
                // Khởi tạo DynamoDB client nếu nó chưa được khởi tạo
                initializeDynamoDB();
            }

            // Bắt đầu một thread mới để thực hiện công việc này
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Truy vấn người dùng từ bảng Users
                        GetItemRequest getItemRequest = new GetItemRequest()
                                .withTableName("Users")
                                .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userID)));

                        GetItemResult getItemResult = ddbClient.getItem(getItemRequest);

                        Map<String, AttributeValue> item = getItemResult.getItem();

                        // Kiểm tra nếu item không null và có chứa key "groups"
                        if (item != null && item.containsKey("groups")) {
                            AttributeValue groupsAttributeValue = item.get("groups");
                            List<Map<String, AttributeValue>> groups = new ArrayList<>();
                            if (groupsAttributeValue.getL() != null) {
                                for (AttributeValue av : groupsAttributeValue.getL()) {
                                    if (av.getM() != null) {
                                        groups.add(av.getM());
                                    }
                                }
                            }

                            // Duyệt qua từng phần tử trong mảng groups để tìm và xóa groupID cần xóa
                            for (Iterator<Map<String, AttributeValue>> iterator = groups.iterator(); iterator.hasNext(); ) {
                                Map<String, AttributeValue> group = iterator.next();
                                if (group.containsKey("groupID") && group.get("groupID").getS().equals(groupID)) {
                                    iterator.remove(); // Xóa phần tử khỏi mảng nếu tìm thấy groupID
                                }
                            }

                            // Cập nhật lại giá trị của mảng groups trong item
                            List<AttributeValue> updatedGroups = new ArrayList<>();
                            for (Map<String, AttributeValue> group : groups) {
                                updatedGroups.add(new AttributeValue().withM(group));
                            }

                            // Tạo yêu cầu cập nhật
                            UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                                    .withTableName("Users")
                                    .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userID)))
                                    .withAttributeUpdates(Collections.singletonMap(
                                            "groups",
                                            new AttributeValueUpdate().withValue(new AttributeValue().withL(updatedGroups)).withAction(AttributeAction.PUT)
                                    ));

                            // Thực hiện cập nhật
                            ddbClient.updateItem(updateItemRequest);
                        }

                    } catch (Exception e) {
                        // Ghi log cho lỗi để debug
                        System.err.println("Exception occurred: " + e);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Log exception for debugging
            System.err.println("Error checking DynamoDB connection: " + e.getMessage());
        }
    }
    //12 Tìm bạn bè trong nhóm và kết hợp xóa hết thành viên đó ra khỏi nhóm
    //công dụng 1: hàm này cũng giúp tìm bạn bè trong group và kết hợp bạn bè trong dánh sách
    //bạn bè để lọc ra ai chưa vô nhóm(hàm kết hợp getIDFriend)
    //công dụng 2: tương tự như 1 nhưng sẽ lọc ra những ai có trong nhóm
    public void findMemberOfGroup(String groupId, ListMemberListener listener) {

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
                                .withAttributeValueList(new AttributeValue().withS(groupId));
                        scanFilter.put("_id", condition);

                        ScanRequest scanRequest = new ScanRequest("Groups").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);

                        // Xử lý kết quả
                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            List<AttributeValue> members = item.get("members").getL();
                            for (AttributeValue member : members) {
                                String memberID = member.getM().get("memberID").getS();

                                Log.d("checkmemberID", memberID);
                                listener.ListMemberID(memberID);
                            }

                        }
                    } catch (Exception e) {

                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {

        }
    }

    //13 cập nhật avatar của nhóm
    public void updateGroupAvatar(String groupId, String groupAvatar) {
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
                                .withAttributeValueList(new AttributeValue().withS(groupId));
                        scanFilter.put("_id", condition);

                        ScanRequest scanRequest = new ScanRequest("GroupConversation").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);

                        // Xử lý kết quả
                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            // Cập nhật thông tin người dùng
                            item.put("avatar", new AttributeValue().withS(groupAvatar));
                            // Tạo yêu cầu put item để cập nhật thông tin người dùng trong cơ sở dữ liệu
                            PutItemRequest putItemRequest = new PutItemRequest()
                                    .withTableName("GroupConversation")
                                    .withItem(item);
                            // Thực hiện cập nhật thông tin người dùng trong cơ sở dữ liệu
                            ddbClient.putItem(putItemRequest);

                            // Gọi callback để thông báo cập nhật thành công
                            return;
                        }

                        // Gọi callback nếu không tìm thấy người dùng cần cập nhật
                    } catch (Exception e) {
                        // Gọi callback nếu xảy ra lỗi
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Gọi callback nếu xảy ra lỗi
        }
    }

    //14 cập nhật tên nhóm
    public void updateGroupName(String groupId, String groupName) {
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
                                .withAttributeValueList(new AttributeValue().withS(groupId));
                        scanFilter.put("_id", condition);

                        ScanRequest scanRequest = new ScanRequest("GroupConversation").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);

                        // Xử lý kết quả
                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            // Cập nhật thông tin người dùng
                            item.put("groupName", new AttributeValue().withS(groupName));
                            // Tạo yêu cầu put item để cập nhật thông tin người dùng trong cơ sở dữ liệu
                            PutItemRequest putItemRequest = new PutItemRequest()
                                    .withTableName("GroupConversation")
                                    .withItem(item);
                            // Thực hiện cập nhật thông tin người dùng trong cơ sở dữ liệu
                            ddbClient.putItem(putItemRequest);

                            // Gọi callback để thông báo cập nhật thành công
                            return;
                        }

                        // Gọi callback nếu không tìm thấy người dùng cần cập nhật
                    } catch (Exception e) {
                        // Gọi callback nếu xảy ra lỗi
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Gọi callback nếu xảy ra lỗi
        }
    }

    //15 đếm số lượng thành viên trong nhóm.
    //Dùng để kiểm tra ràng buột cho việc xóa nhóm nếu số lượng <=1 thì xóa group và groupconversation
    public int countMembersInGroup(String groupID, CountMembersCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ddbClient == null) {
                        initializeDynamoDB();
                    }

                    // Tạo yêu cầu truy vấn đến DynamoDB để lấy thông tin của nhóm dựa trên _id
                    GetItemRequest getRequest = new GetItemRequest()
                            .withTableName("Groups")
                            .withKey(Map.of("_id", new AttributeValue().withS(groupID)));

                    GetItemResult getResult = ddbClient.getItem(getRequest);

                    int memberCount = 0; // Khởi tạo biến đếm thành viên

                    // Kiểm tra nếu kết quả không null và có thuộc tính "members"
                    if (getResult != null && getResult.getItem() != null && getResult.getItem().containsKey("members")) {
                        // Lấy mảng members từ kết quả
                        List<AttributeValue> members = getResult.getItem().get("members").getL();
                        // Đếm số lượng thành viên trong mảng
                        memberCount = members.size();
                    }

                    // Gọi callback với kết quả đếm số thành viên
                    callback.onCountComplete(memberCount);

                } catch (Exception e) {
                    // Xử lý ngoại lệ và thông báo lỗi cho người nghe
                    Log.e("", "Error counting members in group: " + e.getMessage());
                }
            }
        }).start(); // Khởi chạy thread
        return 0;
    }

    public interface CountMembersCallback {
        void onCountComplete(int countMember);
    }

    //15 cập nhật thêm nhóm cho các thành viên được thêm vào nhóm
    public void updateGroupForAccounts(List<String> userIDs, String groupID, String role) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (String userID : userIDs) {
                            GetItemRequest getItemRequest = new GetItemRequest()
                                    .withTableName("Users")
                                    .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userID)));

                            GetItemResult getItemResult = ddbClient.getItem(getItemRequest);

                            Map<String, AttributeValue> item = getItemResult.getItem();
                            List<AttributeValue> existingConversations;

                            // Kiểm tra xem bảng có mảng "groups" chưa
                            if (item.containsKey("groups")) {
                                existingConversations = item.get("groups").getL();
                            } else {
                                // Nếu chưa có, tạo mảng "groups" mới
                                existingConversations = new ArrayList<>();
                            }

                            boolean conversationExists = false;

                            for (AttributeValue conversation : existingConversations) {
                                String existingConversationID = conversation.getM().get("groupID").getS();
                                if (existingConversationID.equals(groupID)) {
                                    // Nếu cuộc trò chuyện đã tồn tại, cập nhật nội dung
                                    conversationExists = true;
                                    break;
                                }
                            }

                            if (!conversationExists) {
                                // Nếu cuộc trò chuyện chưa tồn tại, thêm mới cuộc trò chuyện vào mảng
                                Map<String, AttributeValue> groupItem = new HashMap<>();
                                groupItem.put("groupID", new AttributeValue().withS(groupID));
                                groupItem.put("role", new AttributeValue().withS(role));
                                existingConversations.add(new AttributeValue().withM(groupItem));
                            }

                            // Cập nhật item trong bảng DynamoDB
                            Map<String, AttributeValueUpdate> updates = new HashMap<>();
                            updates.put("groups", new AttributeValueUpdate()
                                    .withAction(AttributeAction.PUT)
                                    .withValue(new AttributeValue().withL(existingConversations)));

                            UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                                    .withTableName("Users")
                                    .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userID)))
                                    .withAttributeUpdates(updates);
                            Log.d("CheckAddMember", String.valueOf(updateItemRequest));
                            ddbClient.updateItem(updateItemRequest);
                        }
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

    //16 thêm các thành viên vào nhóm
    public void updateGroup(String groupID, List<String> memberIDs) {
        new Thread(() -> {
            try {
                if (ddbClient == null) {
                    initializeDynamoDB();
                }

                // Lấy danh sách thành viên hiện tại từ cơ sở dữ liệu
                Map<String, AttributeValue> key = Collections.singletonMap("_id", new AttributeValue().withS(groupID));
                GetItemRequest getItemRequest = new GetItemRequest().withTableName("Groups").withKey(key);
                GetItemResult getItemResult = ddbClient.getItem(getItemRequest);
                List<AttributeValue> existingMemberList = getItemResult.getItem().get("members").getL();

                // Thêm thành viên mới vào danh sách hiện có
                for (String memberID : memberIDs) {
                    Map<String, AttributeValue> newMemberMap = new HashMap<>();
                    newMemberMap.put("memberID", new AttributeValue().withS(memberID));
                    existingMemberList.add(new AttributeValue().withM(newMemberMap));
                }

                // Cập nhật danh sách thành viên mới vào cơ sở dữ liệu
                Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
                expressionAttributeValues.put(":members", new AttributeValue().withL(existingMemberList));
                UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                        .withTableName("Groups")
                        .withKey(key)
                        .withUpdateExpression("SET members = :members")
                        .withExpressionAttributeValues(expressionAttributeValues);
                UpdateItemResult updateItemResult = ddbClient.updateItem(updateItemRequest);

            } catch (Exception e) {
                // Xử lý ngoại lệ và thông báo lỗi cho người nghe
                Log.e("", "Error loading conversation: " + e.getMessage());
            }
        }).start(); // Khởi chạy luồng
    }

    //17 xóa nhóm ra khỏi các thành viên
    public void deleteGroupFromUsers(List<String> userIDs, String groupID) {
        try {
            if (ddbClient == null) {
                // Khởi tạo DynamoDB client nếu nó chưa được khởi tạo
                initializeDynamoDB();
            }

            // Bắt đầu một thread mới để thực hiện công việc này
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (String userID : userIDs) {
                            // Truy vấn người dùng từ bảng Users
                            GetItemRequest getItemRequest = new GetItemRequest()
                                    .withTableName("Users")
                                    .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userID)));

                            GetItemResult getItemResult = ddbClient.getItem(getItemRequest);

                            Map<String, AttributeValue> item = getItemResult.getItem();

                            // Kiểm tra nếu item không null và có chứa key "groups"
                            if (item != null && item.containsKey("groups")) {
                                AttributeValue groupsAttributeValue = item.get("groups");
                                List<Map<String, AttributeValue>> groups = new ArrayList<>();
                                if (groupsAttributeValue.getL() != null) {
                                    for (AttributeValue av : groupsAttributeValue.getL()) {
                                        if (av.getM() != null) {
                                            groups.add(av.getM());
                                        }
                                    }
                                }

                                // Duyệt qua từng phần tử trong mảng groups để tìm và xóa groupID cần xóa
                                for (Iterator<Map<String, AttributeValue>> iterator = groups.iterator(); iterator.hasNext(); ) {
                                    Map<String, AttributeValue> group = iterator.next();
                                    if (group.containsKey("groupID") && group.get("groupID").getS().equals(groupID)) {
                                        iterator.remove(); // Xóa phần tử khỏi mảng nếu tìm thấy groupID
                                    }
                                }

                                // Cập nhật lại giá trị của mảng groups trong item
                                List<AttributeValue> updatedGroups = new ArrayList<>();
                                for (Map<String, AttributeValue> group : groups) {
                                    updatedGroups.add(new AttributeValue().withM(group));
                                }

                                // Tạo yêu cầu cập nhật
                                UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                                        .withTableName("Users")
                                        .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userID)))
                                        .withAttributeUpdates(Collections.singletonMap(
                                                "groups",
                                                new AttributeValueUpdate().withValue(new AttributeValue().withL(updatedGroups)).withAction(AttributeAction.PUT)
                                        ));

                                // Thực hiện cập nhật
                                ddbClient.updateItem(updateItemRequest);
                                String s = String.valueOf(ddbClient.updateItem(updateItemRequest));
                                Log.d("Check1052", s);
                            }

                        }
                    } catch (Exception e) {
                        // Ghi log cho lỗi để debug
                        System.err.println("Exception occurred: " + e);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Log exception for debugging
            System.err.println("Error checking DynamoDB connection: " + e.getMessage());
        }
    }

    //18 xóa các thành viên ra khỏi nhóm
    public void deleteUserFromGroups(String groupID, List<String> userIDs) {
        try {
            if (ddbClient == null) {
                // Khởi tạo DynamoDB client nếu nó chưa được khởi tạo
                initializeDynamoDB();
            }

            // Bắt đầu một thread mới để thực hiện công việc này
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Truy vấn nhóm từ bảng Groups
                        GetItemRequest getItemRequest = new GetItemRequest()
                                .withTableName("Groups")
                                .withKey(Collections.singletonMap("_id", new AttributeValue().withS(groupID)));

                        GetItemResult getItemResult = ddbClient.getItem(getItemRequest);

                        Map<String, AttributeValue> item = getItemResult.getItem();

                        if (item != null && item.containsKey("members")) {
                            AttributeValue membersAttributeValue = item.get("members");
                            List<Map<String, AttributeValue>> members = new ArrayList<>();
                            if (membersAttributeValue.getL() != null) {
                                for (AttributeValue av : membersAttributeValue.getL()) {
                                    if (av.getM() != null) {
                                        members.add(av.getM());
                                    }
                                }
                            }

                            // Duyệt qua từng ID người dùng trong danh sách userIDs để xóa
                            for (String userID : userIDs) {
                                // Duyệt qua từng phần tử trong mảng members để tìm và xóa userID cần xóa
                                for (Iterator<Map<String, AttributeValue>> iterator = members.iterator(); iterator.hasNext(); ) {
                                    Map<String, AttributeValue> member = iterator.next();
                                    if (member.containsKey("memberID") && member.get("memberID").getS().equals(userID)) {
                                        iterator.remove(); // Xóa phần tử khỏi mảng nếu tìm thấy userID
                                    }
                                }
                            }

                            // Cập nhật lại giá trị của mảng members trong item
                            List<AttributeValue> updatedMembers = new ArrayList<>();
                            for (Map<String, AttributeValue> member : members) {
                                updatedMembers.add(new AttributeValue().withM(member));
                            }

                            // Tạo yêu cầu cập nhật
                            UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                                    .withTableName("Groups")
                                    .withKey(Collections.singletonMap("_id", new AttributeValue().withS(groupID)))
                                    .withAttributeUpdates(Collections.singletonMap(
                                            "members",
                                            new AttributeValueUpdate().withValue(new AttributeValue().withL(updatedMembers)).withAction(AttributeAction.PUT)
                                    ));

                            // Thực hiện cập nhật
                            ddbClient.updateItem(updateItemRequest);
                        }

                    } catch (Exception e) {
                        // Ghi log cho lỗi để debug
                        System.err.println("Exception occurred: " + e);
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Log exception for debugging
            System.err.println("Error checking DynamoDB connection: " + e.getMessage());
        }
    }

    //19 cập nhật vai trò của thành viên
    public void updateRoleForMember(String userID, String groupID, String role) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Lấy thông tin người dùng từ bảng Users
                        GetItemRequest getItemRequest = new GetItemRequest()
                                .withTableName("Users")
                                .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userID)));

                        GetItemResult getItemResult = ddbClient.getItem(getItemRequest);
                        Map<String, AttributeValue> item = getItemResult.getItem();

                        // Kiểm tra xem người dùng có tồn tại không
                        if (item != null && item.containsKey("groups")) {
                            List<AttributeValue> existingGroups = item.get("groups").getL();

                            // Duyệt qua các group của người dùng
                            for (AttributeValue group : existingGroups) {
                                String existingGroupID = group.getM().get("groupID").getS();

                                // Nếu tìm thấy group cần cập nhật
                                if (existingGroupID.equals(groupID)) {
                                    Map<String, AttributeValue> updatedGroup = new HashMap<>();
                                    updatedGroup.put("groupID", new AttributeValue().withS(groupID));
                                    updatedGroup.put("role", new AttributeValue().withS(role));

                                    // Cập nhật role của group
                                    UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                                            .withTableName("Users")
                                            .withKey(Collections.singletonMap("_id", new AttributeValue().withS(userID)))
                                            .addAttributeUpdatesEntry("groups", new AttributeValueUpdate()
                                                    .withAction(AttributeAction.PUT)
                                                    .withValue(new AttributeValue().withL(Arrays.asList(new AttributeValue().withM(updatedGroup)))));

                                    ddbClient.updateItem(updateItemRequest);
                                    Log.d("UpdateGroup", "Successfully updated role for group " + groupID + " of user " + userID);
                                    return; // Kết thúc khi đã cập nhật thành công
                                }
                            }
                            Log.d("UpdateGroup", "Group " + groupID + " not found for user " + userID);
                        } else {
                            Log.d("UpdateGroup", "User " + userID + " not found or does not have groups");
                        }
                    } catch (Exception e) {
                        Log.e("Error", "Exception occurred: ", e);
                    }
                }
            }).start();
        } catch (Exception e) {
            Log.e("DynamoDBManager", "Error checking DynamoDB connection: " + e.getMessage());
        }
    }


    //other tìm bạn để tạo nhóm chat
    public void findFriendByInfor(String infor, String uid, FriendFoundListener listener) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        regexp = new Regexp();
                        ScanResult scanResult;

                        if (regexp.isValidGmailEmail(infor) == true) {
                            // Tạo một yêu cầu truy vấn
                            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
                            Condition condition = new Condition()
                                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                                    .withAttributeValueList(new AttributeValue().withS(infor));
                            scanFilter.put("email", condition);

                            ScanRequest scanRequest = new ScanRequest("Users").withScanFilter(scanFilter);
                            scanResult = ddbClient.scan(scanRequest);
                        } else {
                            // Tạo một yêu cầu truy vấn
                            HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
                            Condition condition = new Condition()
                                    .withComparisonOperator(ComparisonOperator.EQ.toString())
                                    .withAttributeValueList(new AttributeValue().withS(infor));
                            scanFilter.put("name", condition);
                            ScanRequest scanRequest = new ScanRequest("Users").withScanFilter(scanFilter);
                            scanResult = ddbClient.scan(scanRequest);
                        }


                        // Xử lý kết quả
                        for (Map<String, AttributeValue> item : scanResult.getItems()) {

                            List<AttributeValue> friendsList = item.get("friends").getL();
                            for (AttributeValue friend : friendsList) {
                                String status_friend = friend.getM().get("status").getS();
                                String friendId = friend.getM().get("_idFriend").getS();
                                if (friendId.equals(uid) && status_friend.equals("1")) {
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
                            }

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

    public void getProfileByUID(String uid, String groupID, FriendFoundForGetUIDByEmailListener listener) {
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

                            // Truy cập vào mảng groups để lấy role
                            List<AttributeValue> groups = item.get("groups").getL();
                            String role = null;
                            for (AttributeValue group : groups) {
                                String currentGroupID = group.getM().get("groupID").getS(); // Sử dụng tên biến khác
                                if (currentGroupID.equals(groupID)) { // So sánh với groupID đã truyền vào hàm
                                    role = group.getM().get("role").getS();
                                    break;
                                }
                            }

                            // Tạo một chuỗi để hiển thị trong ListView, ví dụ: "Name: [Tên], Avatar: [Avatar]"
                            String userResult = "name đã nhận: " + name + "email đã nhận: " + email;

                            // Log dữ liệu
                            Log.d("userResult", userResult);
                            listener.onFriendFound(uid, name, email, avatar, sex, dateOfBirth, role);
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

    public interface FriendFoundForGetUIDByEmailListener {
        void onFriendFound(String uid, String name, String email, String avatar, Boolean sex, String dateOfBirth);

        void onFriendFound(String uid, String name, String email, String avatar, Boolean sex, String dateOfBirth, String role);

        void onFriendNotFound();

        void onError(Exception e);
    }

    public void findFriendByID(String id, AlreadyFriendListener listener) {

        try {
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
                            FriendItem friend = new FriendItem(id, avatar, name);
                            FriendItem friendForCreateGroup = new FriendItem(id, avatar, name);
                            listener.onFriendAlreadyFound(friend);
                            listener.onFriendAcceptRequestFound(id, name, avatar);
                            listener.onFriendCreateGroupFound(friendForCreateGroup);
                        }
                    } catch (Exception e) {

                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {

        }
    }

    public interface AlreadyFriendListener {
        void onFriendAlreadyFound(FriendItem data);

        void onFriendAcceptRequestFound(String id, String name, String avatar);

        void onFriendCreateGroupFound(FriendItem friendItem);

        void onFriendNotFound(String error);
    }

    public interface ListMemberListener {
        void ListMemberID(String id);
    }
    public interface LoadGroupListListener {
        void onGroupListFound(String id, String groupName, String avatar, String message, String name, String time);
    }
    //new
    public void recallMessage(String channelID, String message, String time) {
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
                                .withAttributeValueList(new AttributeValue().withS(channelID));
                        scanFilter.put("_id", condition);

                        ScanRequest scanRequest = new ScanRequest("ChatHistory").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);

                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            List<AttributeValue> messages = item.get("messages").getL();
                            List<AttributeValue> updatedMessages = new ArrayList<>();
                            for (AttributeValue messageItem : messages) {
                                String messagesFind = messageItem.getM().get("text").getS();
                                String timeFind = messageItem.getM().get("time").getS();
                                if (messagesFind.equals(message) && timeFind.equals(time)) {
                                    continue; // Bỏ qua tin nhắn cần xóa
                                }
                                updatedMessages.add(messageItem);
                            }

                            // Cập nhật lại danh sách tin nhắn trong item
                            Map<String, AttributeValueUpdate> updates = new HashMap<>();
                            updates.put("messages", new AttributeValueUpdate().withValue(new AttributeValue().withL(updatedMessages)));
                            UpdateItemRequest updateRequest = new UpdateItemRequest()
                                    .withTableName("ChatHistory")
                                    .withKey(Collections.singletonMap("_id", new AttributeValue().withS(item.get("_id").getS())))
                                    .withAttributeUpdates(updates);
                            ddbClient.updateItem(updateRequest);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            Log.e("", "Error checking DynamoDB connection: " + e.getMessage());
        }
    }
    public void recallMessageForGroup(String groupID, String message, String time) {
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
                                .withAttributeValueList(new AttributeValue().withS(groupID));
                        scanFilter.put("_id", condition);

                        ScanRequest scanRequest = new ScanRequest("GroupChatHistory").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);

                        for (Map<String, AttributeValue> item : scanResult.getItems()) {
                            List<AttributeValue> messages = item.get("messages").getL();
                            List<AttributeValue> updatedMessages = new ArrayList<>();
                            for (AttributeValue messageItem : messages) {
                                String messagesFind = messageItem.getM().get("message").getS();
                                String timeFind = messageItem.getM().get("time").getS();
                                if (messagesFind.equals(message) && timeFind.equals(time)) {
                                    continue; // Bỏ qua tin nhắn cần xóa
                                }
                                updatedMessages.add(messageItem);
                            }

                            // Cập nhật lại danh sách tin nhắn trong item
                            Map<String, AttributeValueUpdate> updates = new HashMap<>();
                            updates.put("messages", new AttributeValueUpdate().withValue(new AttributeValue().withL(updatedMessages)));
                            UpdateItemRequest updateRequest = new UpdateItemRequest()
                                    .withTableName("GroupChatHistory")
                                    .withKey(Collections.singletonMap("_id", new AttributeValue().withS(item.get("_id").getS())))
                                    .withAttributeUpdates(updates);
                            ddbClient.updateItem(updateRequest);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            Log.e("", "Error checking DynamoDB connection: " + e.getMessage());
        }
    }
    public void GetTypeOfMessage(String id, String message, String time, GetTypeOfMessageListener listener) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d("CheckType337", "run"+time+ message);
                        // Create query request
                        HashMap<String, Condition> scanFilter = new HashMap<>();
                        Condition idCondition = new Condition()
                                .withComparisonOperator(ComparisonOperator.EQ.toString())
                                .withAttributeValueList(new AttributeValue().withS(id));

                        scanFilter.put("_id", idCondition);

                        ScanRequest scanRequest = new ScanRequest("ChatHistory").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);
                        Log.d("CheckingType", scanResult.toString());

                        // Check if any items match the query
                        if (!scanResult.getItems().isEmpty()) {
                            for (Map<String, AttributeValue> item : scanResult.getItems()) {
                                if (item.containsKey("messages")) {
                                    List<AttributeValue> messages = item.get("messages").getL();
                                    for (AttributeValue msg : messages) {
                                        Map<String, AttributeValue> messageMap = msg.getM();
                                        if (messageMap.get("text").getS().equals(message) &&
                                                messageMap.get("time").getS().equals(time)) {
                                            String type = messageMap.get("type").getS();
                                            listener.onFound(type);
                                            return;
                                        }
                                    }
                                }
                            }
                            Log.e("", "No matching message found");
                        } else {
                            Log.e("", "No matching item found");
                        }
                    } catch (Exception e) {
                        Log.e("", "Error querying DynamoDB: " + e.getMessage());
                    }
                }
            }).start(); // Start the thread
        } catch (Exception e) {
            Log.e("", "Error checking DynamoDB connection: " + e.getMessage());
        }
    }


    public interface GetTypeOfMessageListener {

        void onFound(String type);
    }
    public void GetTypeOfGroupMessage(String id, String message, String time, GetTypeOfMessageListener listener) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Create query request
                        HashMap<String, Condition> scanFilter = new HashMap<>();
                        Condition idCondition = new Condition()
                                .withComparisonOperator(ComparisonOperator.EQ.toString())
                                .withAttributeValueList(new AttributeValue().withS(id));

                        scanFilter.put("_id", idCondition);

                        ScanRequest scanRequest = new ScanRequest("GroupChatHistory").withScanFilter(scanFilter);
                        ScanResult scanResult = ddbClient.scan(scanRequest);

                        // Check if any items match the query
                        if (!scanResult.getItems().isEmpty()) {
                            for (Map<String, AttributeValue> item : scanResult.getItems()) {
                                if (item.containsKey("messages")) {
                                    List<AttributeValue> messages = item.get("messages").getL();
                                    for (AttributeValue msg : messages) {
                                        Map<String, AttributeValue> messageMap = msg.getM();
                                        if (messageMap.get("message").getS().equals(message) &&
                                                messageMap.get("time").getS().equals(time)) {
                                            String type = messageMap.get("type").getS();
                                            listener.onFound(type);
                                            return;
                                        }
                                    }
                                }
                            }
                            Log.e("", "No matching message found");
                        } else {
                            Log.e("", "No matching item found");
                        }
                    } catch (Exception e) {
                        Log.e("", "Error querying DynamoDB: " + e.getMessage());
                    }
                }
            }).start(); // Start the thread
        } catch (Exception e) {
            Log.e("", "Error checking DynamoDB connection: " + e.getMessage());
        }
    }
    public void updatelastReadAndUnread(String groupID, List<String> memberIDs) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            Log.d("CheckMemberList", "group: " + groupID + " " + memberIDs);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ddbClient == null) {
                            initializeDynamoDB();
                        }

                        for (String memberID : memberIDs) {
                            // Truy vấn dữ liệu của user từ DynamoDB
                            Map<String, AttributeValue> userKey = new HashMap<>();
                            userKey.put("_id", new AttributeValue().withS(memberID));
                            GetItemRequest getUserRequest = new GetItemRequest()
                                    .withTableName("Users")
                                    .withKey(userKey);
                            GetItemResult getUserResult = ddbClient.getItem(getUserRequest);
                            Map<String, AttributeValue> userItem = getUserResult.getItem();
                            Log.d("CheckMemberList", "load table User: " + userItem);

                            // Nếu user tồn tại và có thuộc tính "groups"
                            if (userItem != null && userItem.containsKey("groups")) {
                                // Lấy danh sách groups từ item
                                List<AttributeValue> groupsAttr = userItem.get("groups").getL();
                                Log.d("CheckMemberList", "load groups: " + groupsAttr);
                                // Duyệt qua từng phần tử trong danh sách groups
                                for (AttributeValue groupAttr : groupsAttr) {
                                    Map<String, AttributeValue> group = groupAttr.getM();

                                    // Nếu tìm thấy group có groupID tương ứng
                                    if (group.containsKey("groupID") && group.get("groupID").getS().equals(groupID)) {
                                        // Kiểm tra xem thuộc tính "lastRead" đã tồn tại hay chưa
                                        if (!group.containsKey("lastRead")) {
                                            // Nếu không tồn tại, thêm thuộc tính "lastRead" với giá trị mặc định
                                            group.put("lastRead", new AttributeValue().withN("0"));
                                        }
                                        // Kiểm tra xem thuộc tính "unread" đã tồn tại hay chưa
                                        if (!group.containsKey("unread")) {
                                            // Nếu không tồn tại, thêm thuộc tính "unread" với giá trị mặc định
                                            group.put("unread", new AttributeValue().withBOOL(false));
                                        }

                                        // Cập nhật lastRead và unread
                                        int lastRead = Integer.parseInt(group.get("lastRead").getN());
                                        lastRead++;
                                        boolean unread = true;

                                        // Cập nhật thông tin của group
                                        group.put("lastRead", new AttributeValue().withN(Integer.toString(lastRead)));
                                        group.put("unread", new AttributeValue().withBOOL(unread));

                                        break; // Thoát khỏi vòng lặp sau khi tìm thấy groupID
                                    }
                                }

                                // Cập nhật thông tin groups của user
                                UpdateItemRequest updateUserRequest = new UpdateItemRequest()
                                        .withTableName("Users")
                                        .withKey(userKey)
                                        .addAttributeUpdatesEntry(
                                                "groups",
                                                new AttributeValueUpdate().withValue(new AttributeValue().withL(groupsAttr)).withAction(AttributeAction.PUT));

                                // Thực hiện cập nhật thông tin
                                UpdateItemResult updateUserResult = ddbClient.updateItem(updateUserRequest);
                            } else {
                                // Xử lý nếu user không tồn tại hoặc không có danh sách groups
                                Log.e("", "User not found with ID: " + memberID + " or groups attribute is missing.");
                            }
                        }
                    } catch (Exception e) {
                        // Xử lý ngoại lệ và thông báo lỗi cho người nghe
                        Log.e("", "Error updating lastRead and unread: " + e.getMessage());
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Gọi callback nếu có lỗi xảy ra
        }
    }

    public void getMembersGroup(String groupID, GroupMemberListener listener) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ddbClient == null) {
                            initializeDynamoDB();
                        }

                        // Truy vấn dữ liệu của nhóm từ DynamoDB
                        Map<String, AttributeValue> key = new HashMap<>();
                        key.put("_id", new AttributeValue().withS(groupID));
                        GetItemRequest getItemRequest = new GetItemRequest()
                                .withTableName("Groups")
                                .withKey(key);
                        GetItemResult getItemResult = ddbClient.getItem(getItemRequest);
                        Map<String, AttributeValue> item = getItemResult.getItem();

                        // Nếu nhóm tồn tại
                        if (item != null) {
                            // Lấy danh sách thành viên từ item
                            List<AttributeValue> members = item.get("members").getL();
                            for (AttributeValue member : members) {
                                Map<String, AttributeValue> memberMap = member.getM();
                                String memberID = memberMap.get("memberID").getS();
                                // Gọi phương thức của listener để trả về từng thành viên
                                listener.onMemberLoaded(memberID);
                            }
                        } else {
                            // Gọi phương thức của listener để báo lỗi nếu nhóm không tồn tại
                            listener.onError("Group not found with ID: " + groupID);
                        }
                    } catch (Exception e) {
                        // Gọi phương thức của listener để báo lỗi nếu có lỗi xảy ra
                        listener.onError("Error getting members of group: " + e.getMessage());
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Gọi phương thức của listener để báo lỗi nếu có lỗi xảy ra
            listener.onError("Error initializing DynamoDB client: " + e.getMessage());
        }
    }
    public interface GroupMemberListener {
        void onMemberLoaded(String member);
        void onError(String errorMessage);
    }
    public void getLastReadAndUnread(String userID, String groupID, Callback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (ddbClient == null) {
                        initializeDynamoDB();
                    }

                    // Truy vấn dữ liệu của user từ DynamoDB
                    Map<String, AttributeValue> userKey = new HashMap<>();
                    userKey.put("_id", new AttributeValue().withS(userID));
                    GetItemRequest getUserRequest = new GetItemRequest()
                            .withTableName("Users")
                            .withKey(userKey);
                    GetItemResult getUserResult = ddbClient.getItem(getUserRequest);
                    Map<String, AttributeValue> userItem = getUserResult.getItem();
                    Log.d("CheckMemberList", "load table User: " + userItem);

                    // Nếu user tồn tại và có thuộc tính "groups"
                    if (userItem != null && userItem.containsKey("groups")) {
                        // Lấy danh sách groups từ item
                        List<AttributeValue> groupsAttr = userItem.get("groups").getL();

                        // Duyệt qua từng phần tử trong danh sách groups
                        for (AttributeValue groupAttr : groupsAttr) {
                            Map<String, AttributeValue> group = groupAttr.getM();

                            // Nếu tìm thấy group có groupID tương ứng
                            if (group.containsKey("groupID") && group.get("groupID").getS().equals(groupID)) {
                                // Kiểm tra xem thuộc tính "lastRead" đã tồn tại hay không
                                int lastRead = group.containsKey("lastRead") ? Integer.parseInt(group.get("lastRead").getN()) : 0;

                                // Kiểm tra xem thuộc tính "unread" đã tồn tại hay không
                                boolean unread = group.containsKey("unread") && group.get("unread").getBOOL();

                                // Gửi thông tin "lastRead" và "unread" qua callback
                                callback.onLastReadAndUnreadReceived(lastRead, unread);
                                return; // Kết thúc vòng lặp sau khi tìm thấy groupID
                            }
                        }
                    } else {
                        // Xử lý nếu user không tồn tại hoặc không có thuộc tính "groups"
                        Log.e("", "User not found with ID: " + userID + " or groups attribute is missing.");
                    }
                } catch (Exception e) {
                    // Xử lý ngoại lệ và thông báo lỗi
                    Log.e("", "Error getting lastRead and unread: " + e.getMessage());
                }
            }
        }).start(); // Khởi chạy thread
    }

    // Định nghĩa callback interface để trả về thông tin "lastRead" và "unread"
    public interface Callback {
        void onLastReadAndUnreadReceived(int lastRead, boolean unread);
    }
    public void resetlastReadAndUnread(String groupID, String memberID) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            Log.d("CheckMemberListChe", "group: " + groupID + " " + memberID);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (ddbClient == null) {
                            initializeDynamoDB();
                        }

                        // Truy vấn dữ liệu của user từ DynamoDB
                        Map<String, AttributeValue> userKey = new HashMap<>();
                        userKey.put("_id", new AttributeValue().withS(memberID));
                        GetItemRequest getUserRequest = new GetItemRequest()
                                .withTableName("Users")
                                .withKey(userKey);
                        GetItemResult getUserResult = ddbClient.getItem(getUserRequest);
                        Map<String, AttributeValue> userItem = getUserResult.getItem();
                        Log.d("CheckMemberList", "load table User: " + userItem);

                        // Nếu user tồn tại và có thuộc tính "groups"
                        if (userItem != null && userItem.containsKey("groups")) {
                            // Lấy danh sách groups từ item
                            List<AttributeValue> groupsAttr = userItem.get("groups").getL();
                            Log.d("CheckMemberList", "load groups: " + groupsAttr);

                            // Duyệt qua từng phần tử trong danh sách groups
                            for (AttributeValue groupAttr : groupsAttr) {
                                Map<String, AttributeValue> group = groupAttr.getM();

                                // Nếu tìm thấy group có groupID tương ứng
                                if (group.containsKey("groupID") && group.get("groupID").getS().equals(groupID)) {
                                    // Cập nhật lastRead và unread
                                    group.put("lastRead", new AttributeValue().withN("0"));
                                    group.put("unread", new AttributeValue().withBOOL(false));

                                    // Thoát khỏi vòng lặp sau khi tìm thấy groupID
                                    break;
                                }
                            }

                            // Cập nhật thông tin groups của user
                            UpdateItemRequest updateUserRequest = new UpdateItemRequest()
                                    .withTableName("Users")
                                    .withKey(userKey)
                                    .addAttributeUpdatesEntry(
                                            "groups",
                                            new AttributeValueUpdate().withValue(new AttributeValue().withL(groupsAttr)).withAction(AttributeAction.PUT));

                            // Thực hiện cập nhật thông tin
                            UpdateItemResult updateUserResult = ddbClient.updateItem(updateUserRequest);
                        } else {
                            // Xử lý nếu user không tồn tại hoặc không có danh sách groups
                            Log.e("", "User not found with ID: " + memberID + " or groups attribute is missing.");
                        }
                    } catch (Exception e) {
                        // Xử lý ngoại lệ và thông báo lỗi cho người nghe
                        Log.e("", "Error updating lastRead and unread: " + e.getMessage());
                    }
                }
            }).start(); // Khởi chạy thread
        } catch (Exception e) {
            // Gọi callback nếu có lỗi xảy ra
        }
    }

    //new
    public void saveGroupMessageWithListMember(String groupID, String message, String time, String memberID, String avatar, String memberName, String type, List<FriendItem> members) {
        try {
            if (ddbClient == null) {
                initializeDynamoDB();
            }
            new Thread(() -> {
                GetItemRequest getItemRequest = new GetItemRequest()
                        .withTableName("GroupChatHistory")
                        .withKey(Collections.singletonMap("_id", new AttributeValue().withS(groupID)));
                GetItemResult getItemResult = ddbClient.getItem(getItemRequest);

                Map<String, AttributeValue> messageItem = new HashMap<>();
                messageItem.put("time", new AttributeValue().withS(time));
                messageItem.put("message", new AttributeValue().withS(message));
                messageItem.put("memberID", new AttributeValue().withS(memberID));
                messageItem.put("memberAvatar", new AttributeValue().withS(avatar));
                messageItem.put("memberName", new AttributeValue().withS(memberName));
                messageItem.put("type", new AttributeValue().withS(type));
                List<Map<String, AttributeValue>> membersList = members.stream()
                        .map(member -> {
                            Map<String, AttributeValue> memberMap = new HashMap<>();
                            memberMap.put("avatar", new AttributeValue().withS(member.getAvatar()));
                            memberMap.put("memberID", new AttributeValue().withS(member.getId()));
                            memberMap.put("name", new AttributeValue().withS(member.getName()));
                            memberMap.put("role", new AttributeValue().withS(member.getRole()));
                            return memberMap;
                        }).collect(Collectors.toList());
                messageItem.put("members", new AttributeValue().withL(membersList.stream()
                        .map(memberMap -> new AttributeValue().withM(memberMap))
                        .collect(Collectors.toList())));

                if (getItemResult.getItem() == null) {
                    // Nếu cuộc trò chuyện chưa tồn tại, tạo mới và lưu vào DynamoDB
                    Map<String, AttributeValue> item = new HashMap<>();
                    item.put("_id", new AttributeValue().withS(groupID));
                    List<Map<String, AttributeValue>> messages = new ArrayList<>();
                    messages.add(messageItem);
                    item.put("messages", new AttributeValue().withL(messages.stream()
                            .map(msg -> new AttributeValue().withM(msg))
                            .collect(Collectors.toList())));

                    PutItemRequest putItemRequest = new PutItemRequest()
                            .withTableName("GroupChatHistory")
                            .withItem(item);

                    ddbClient.putItem(putItemRequest);

                } else {
                    // Nếu cuộc trò chuyện đã tồn tại, thêm tin nhắn mới vào mảng messages
                    Map<String, AttributeValueUpdate> updates = new HashMap<>();
                    updates.put("messages", new AttributeValueUpdate()
                            .withAction(AttributeAction.ADD)
                            .withValue(new AttributeValue().withL(Collections.singletonList(new AttributeValue().withM(messageItem)))));

                    UpdateItemRequest updateItemRequest = new UpdateItemRequest()
                            .withTableName("GroupChatHistory")
                            .withKey(Collections.singletonMap("_id", new AttributeValue().withS(groupID)))
                            .withAttributeUpdates(updates);

                    ddbClient.updateItem(updateItemRequest);
                }

            }).start(); // Bắt đầu thread
        } catch (Exception e) {
            // Xử lý ngoại lệ nếu có lỗi xảy ra
        }
    }

}