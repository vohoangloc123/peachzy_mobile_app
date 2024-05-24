package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.LiveData.MyGroupViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.Other.Utils;
import com.example.peachzyapp.R;
import com.example.peachzyapp.SocketIO.MyWebSocket;
import com.example.peachzyapp.adapters.CreateGroupChatAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class CreateGroupChatFragment extends Fragment implements MyWebSocket.WebSocketListener{
    public static final String TAG= CreateGroupChatFragment.class.getName();
    MyWebSocket myWebSocket;
    private View view;
    private ImageButton btnFindFriend;
    private Button btnCreateGroup;
    private EditText etFindByNameOrEmail;
    private EditText etGroupName;
    private MainActivity mainActivity;
    private ArrayList<FriendItem> friendList;
    RecyclerView rcvFriendListForGroup;
    CreateGroupChatAdapter createGroupChatAdapter;
    private DynamoDBManager dynamoDBManager;
    String uid;
    FriendItem friendItem;
    private MyGroupViewModel viewModel;
    private ImageButton btnAvatarGroup;
    private CheckBox cbAddToGroup;
    private Button btnCancel;
    private static final int PICK_IMAGE_REQUEST = 1;
    private AmazonS3 s3Client;
    private PutObjectRequest request;
    private String urlAvatar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyGroupViewModel.class);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        friendList = new ArrayList<>();
        view = inflater.inflate(R.layout.create_group_fragment, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MyGroupViewModel.class);
        etFindByNameOrEmail= view.findViewById(R.id.etNameOrEmail);
        etGroupName= view.findViewById(R.id.etGroupName);
        btnFindFriend = view.findViewById(R.id.btnFindFriend);
        cbAddToGroup = view.findViewById(R.id.cbAddToGroup);
        btnCreateGroup = view.findViewById(R.id.btnCreateGroup);
        btnCancel=view.findViewById(R.id.btnCancel);
        btnAvatarGroup = view.findViewById(R.id.btnAvatarGroup);
        dynamoDBManager = new DynamoDBManager(getActivity());
        mainActivity= (MainActivity) getActivity();
        //truyen id
        Bundle bundleReceive=getArguments();
        uid = bundleReceive.getString("uid");
        Log.d("CheckUIDhere", uid);

        rcvFriendListForGroup = view.findViewById(R.id.rcvFriendListForGroup);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvFriendListForGroup.setLayoutManager(linearLayoutManager);
        loadFriends();
        btnCancel.setOnClickListener(v->{
            getActivity().getSupportFragmentManager().popBackStack();
            mainActivity.showBottomNavigation(true);
        });
        btnFindFriend.setOnClickListener(v->{
            String infor = etFindByNameOrEmail.getText().toString().trim();
            // Log.d("Information", infor);
            dynamoDBManager.findFriendByInfor(infor, uid,new DynamoDBManager.FriendFoundListener() {
                @Override
                public void onFriendFound(String id, String name, String avatar) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            friendItem = new FriendItem(id, avatar, name);
                            friendList.clear();
                            friendList.add(friendItem);

                            createGroupChatAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivity(), "Friend found!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFriendNotFound() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Friend not found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("Error", "Exception occurred: ", e);
                            Toast.makeText(getActivity(), "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        });
        createGroupChatAdapter= new CreateGroupChatAdapter(friendList);

        rcvFriendListForGroup.setAdapter(createGroupChatAdapter);

        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            String groupID = "-"+randomNumber() + "-" + uid;
            String currentTime = getCurrentDateTime();
            List<String> selectedFriendIds = createGroupChatAdapter.getSelectedFriendIds();
            List<String> selectedFriendIDsToCreateGroup = new ArrayList<>(selectedFriendIds);
            selectedFriendIDsToCreateGroup.add(uid);
            Log.d("CheckCreateGroup140", "Mảng có uid mọi người member "+selectedFriendIds);
            Log.d("CheckCreateGroup140", "Mảng có uid mọi người member và cả leader"+selectedFriendIDsToCreateGroup);
            if (groupName.equals("")) {
                Toast.makeText(getActivity(), "Tên group không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }
            if (urlAvatar != null) {
                // Kiểm tra số lượng thành viên đã chọn
                if (selectedFriendIds.size() >=2) {
                    // Thực hiện các thao tác khi số lượng thành viên đủ
                    dynamoDBManager.updateGroupForAccount(uid, groupID, "leader");
                    for (String friendId : selectedFriendIds) {
                        dynamoDBManager.updateGroupForAccount(friendId, groupID, "member");
                    }
                    dynamoDBManager.createGroup(groupID,  selectedFriendIDsToCreateGroup);
                    dynamoDBManager.saveGroupConversation(groupID, "Vừa tạo group", groupName, currentTime, urlAvatar, "");



                    getActivity().getSupportFragmentManager().popBackStack();
                    mainActivity.showBottomNavigation(true);
                } else {
                    // Hiển thị Toast thông báo khi số lượng thành viên không đủ
                    Toast.makeText(getContext(), "Chưa đủ số lượng thành viên để tạo group tối thiểu là 2 người", Toast.LENGTH_LONG).show();
                }
            } else if (urlAvatar == null) {
                // Kiểm tra số lượng thành viên đã chọn
                if (selectedFriendIds.size() >= 2) {
                    // Thực hiện các thao tác khi số lượng thành viên đủ
                    dynamoDBManager.updateGroupForAccount(uid, groupID, "leader");
                    for (String friendId : selectedFriendIds) {
                        dynamoDBManager.updateGroupForAccount(friendId, groupID, "member");
                    }
                    dynamoDBManager.createGroup(groupID,  selectedFriendIDsToCreateGroup);
                    dynamoDBManager.saveGroupConversation(groupID, "Vừa tạo group", groupName, currentTime, "https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/avatar.jpg", "");

                    getActivity().getSupportFragmentManager().popBackStack();
                    mainActivity.showBottomNavigation(true);
                }else {
                    // Hiển thị Toast thông báo khi số lượng thành viên không đủ
                    Toast.makeText(getContext(), "Chưa đủ số lượng thành viên để tạo group tối thiểu là 2 người", Toast.LENGTH_LONG).show();
                }
            }
            ///*
            ArrayList<FriendItem> listMember = new ArrayList<>();
            CountDownLatch latch = new CountDownLatch(selectedFriendIds.size());
            for (String friendId : selectedFriendIds) {
                dynamoDBManager.getProfileByUID(friendId, new DynamoDBManager.FriendFoundForGetUIDByEmailListener() {
                    @Override
                    public void onFriendFound(String uid, String name, String email, String avatar, Boolean sex, String dateOfBirth) {

                    }

                    @Override
                    public void onFriendFound(String id, String name, String email, String avatar, Boolean sex, String dateOfBirth, String role) {
                        if(uid.equals(id)){
                            listMember.add(new FriendItem(id,avatar,name,"leader"));
                        }else{
                            listMember.add(new FriendItem(id,avatar,name,"member"));
                        }

                        latch.countDown(); // Giảm giá trị của CountDownLatch
                    }

                    @Override
                    public void onFriendNotFound() {

                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
            try {
                latch.await(); // Đợi cho đến khi tất cả các cuộc gọi đã hoàn thành
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            JSONArray membersArray = new JSONArray();
            for (FriendItem member : listMember) {
                JSONObject memberObject = new JSONObject();
                try {
                    memberObject.put("name", member.getName());
                    memberObject.put("memberID", member.getId());
                    memberObject.put("avatar", member.getAvatar());
                    memberObject.put("role", member.getRole());
                    // Thêm các trường khác nếu cần thiết
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                membersArray.put(memberObject);
            }

            JSONObject messageToSend = new JSONObject();
            JSONObject json = new JSONObject();

            try{

                messageToSend.put("_id", groupID);
                if(urlAvatar==null){
                    messageToSend.put("avatar", "https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/avatar.jpg");
                }else{
                    messageToSend.put("avatar", urlAvatar);
                }
                messageToSend.put("groupName", groupName);
                messageToSend.put("time", currentTime);
                messageToSend.put("name", "");
                messageToSend.put("message", "Vừa tạo group");

                json.put("type", "create-group");

                messageToSend.put("members",membersArray);
                json.put("message", messageToSend);


            }catch (JSONException e) {
                throw new RuntimeException(e);
            }
            for(String id: selectedFriendIDsToCreateGroup){
                initWebSocket(id);
                myWebSocket.sendMessage(String.valueOf(json));
                myWebSocket.closeWebSocket();
            }
            ///*
        });
        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAZI2LEH5QHYJMDGHD", "57MJpyB+ZOaL1XHIgjb1fdBsXc4HnH/S2lkEYDQ/");
        // Tạo Amazon S3 client
        s3Client = new AmazonS3Client(credentials);
        s3Client.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
        btnAvatarGroup.setOnClickListener(v->{
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                }
        );
        return view;
    }
    public static void loadCircularImage(Context context, Bitmap bitmap, ImageView imageView) {
        Glide.with(context)
                .load(bitmap)
                .encodeFormat(Bitmap.CompressFormat.JPEG)
                .encodeQuality(10)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(imageView);
    }
    private String generateFileName() {
        // Lấy ngày giờ hiện tại
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        // Tạo dãy số random
        int randomNumber = new Random().nextInt(10000);

        // Kết hợp ngày giờ và dãy số random để tạo tên file
        return "avatar_" + timeStamp + "_" + randomNumber + ".jpg";
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                btnAvatarGroup.setImageBitmap(bitmap);

                //crop image circle
                loadCircularImage(getActivity(),bitmap,btnAvatarGroup);

                // Upload ảnh lên S3

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Mở InputStream từ Uri
                            String fileName=generateFileName();
                            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);

                            // Tạo đối tượng PutObjectRequest và đặt tên bucket và key
                            request = new PutObjectRequest("chat-app-image-cnm", fileName+".jpg", inputStream, new ObjectMetadata());
                            urlAvatar="https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/"+fileName+".jpg";
                            // Upload ảnh lên S3
                            s3Client.putObject(request);

                            // Đóng InputStream sau khi tải lên thành công
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private String randomNumber()
    {
        int randomNumber = new Random().nextInt(10000);
        return String.valueOf(randomNumber);
    }
    private void loadFriends()
    {
        dynamoDBManager.getIDFriend(uid,"1", new DynamoDBManager.AlreadyFriendListener() {
            @Override
            public void onFriendAlreadyFound(FriendItem data) {

            }

            @Override
            public void onFriendAcceptRequestFound(String id, String name, String avatar) {

            }

            @Override
            public void onFriendCreateGroupFound(FriendItem friendItem) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d("onDATAREquest", "run: "+data.getName());
                        friendList.add(friendItem);
                        createGroupChatAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFriendNotFound(String error) {

            }

        });
    }
    private void changeData() {
        viewModel.setData("New data");
    }

    @Override
    public void onMessageReceived(String message) {

    }

    @Override
    public void onConnectionStateChanged(boolean isConnected) {

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Ngắt kết nối khi Fragment bị hủy
        mainActivity.showBottomNavigation(true);
    }
    private void initWebSocket(String channelId) {
        Log.d("initWebSocket: ",channelId);
        myWebSocket = new MyWebSocket("wss://free.blr2.piesocket.com/v3/"+channelId+"?api_key=ujXx32mn0joYXVcT2j7Gp18c0JcbKTy3G6DE9FMB&notify_self=0", this);
    }
    public static String getCurrentDateTime() {
        // Định dạng cho ngày tháng năm và giờ
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        // Lấy thời gian hiện tại
        Date currentTime = new Date();
        // Định dạng thời gian hiện tại thành chuỗi
        String formattedDateTime = dateFormat.format(currentTime);
        return formattedDateTime;
    }
}