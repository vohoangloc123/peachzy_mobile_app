package com.example.peachzyapp.fragments.MainFragments.Chats;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.peachzyapp.LiveData.MyViewChatModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.Other.Utils;
import com.example.peachzyapp.R;
import com.example.peachzyapp.SocketIO.MyWebSocket;
import com.example.peachzyapp.adapters.MyAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.Item;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;


public class ChatBoxFragment extends Fragment implements MyWebSocket.WebSocketListener {
    ImageButton btnSend;
    ImageButton btnImage;
    ImageButton btnBack;
    EditText etMessage;
    TextView etName;
    private List<Item> listMessage = new ArrayList<>();
    private MyAdapter adapter;
    public static final String TAG= ChatHistoryFragment.class.getName();
    MyWebSocket myWebSocket;
    RecyclerView recyclerView;
    int newPosition;
    String uid;
    String friend_id;
    private String channel_id = null;
    DynamoDBManager dynamoDBManager;
    MainActivity mainActivity;
    private String urlAvatar;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String BUCKET_NAME = "chat-app-image-cnm";
    PutObjectRequest request;
    private AmazonS3 s3Client;
    ImageButton btnLink;
    String userName;
    String friendName;
    private static final int PICK_DOCUMENT_REQUEST = 2;

    private MyViewChatModel viewModel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyViewChatModel.class);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewChatModel.class);
        View view= inflater.inflate(R.layout.fragment_chat_box, container, false);
        recyclerView = view.findViewById(R.id.recycleview);
        btnSend = view.findViewById(R.id.btnSend);
        btnImage=view.findViewById(R.id.btnImage);
        btnBack=view.findViewById(R.id.btnBack);
        etName=view.findViewById(R.id.etName);
        etMessage = view.findViewById(R.id.etMessage);
        //main activity
        mainActivity = (MainActivity) getActivity();
        // Initialize the adapter only once
        adapter = new MyAdapter(getContext(), listMessage);
        recyclerView.setAdapter(adapter);
        // initialize dynamoDB
        dynamoDBManager=new DynamoDBManager(getContext());
        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAZI2LEH5QOVBLO5IY", "qjAXFoZGxhu9u7IaDV818wbZNQeao2it1Wm8yEju");
        // Tạo Amazon S3 client
        s3Client = new AmazonS3Client(credentials);
        s3Client.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
        // Set up RecyclerView layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
        //Get ID
        Bundle bundleReceive=getArguments();
        uid = bundleReceive.getString("uid");
        Log.d("RequestUIDChat", "onCreateView: "+uid);
        friend_id= bundleReceive.getString("friend_id");
        Log.d("RequestUIDfriend", "onCreateView: "+friend_id);
        urlAvatar= bundleReceive.getString("urlAvatar");
        Log.d("RequesturlAvatar", "onCreateView: "+urlAvatar);
        friendName= bundleReceive.getString("friendName");
        etName.setText(friendName);
        dynamoDBManager.getChannelID(uid, friend_id, new DynamoDBManager.ChannelIDinterface() {
            @Override
            public void GetChannelId(String channelID) {
                channel_id= channelID;
                Log.d("RequestUIDchannel1", "onCreateView: "+channel_id);
                initWebSocket();

            }
        });
        Log.d("RequestUIDchannel2", "onCreateView: "+channel_id);
        //xử lý resize giao diện và đẩy edit text và button lên khi chat ngoài ra còn load tin nhắn mói từ dưới lên
        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        //nhận uid của bản thân và id của người bạn
        //bản thân
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);
        dynamoDBManager.getProfileByUID(uid, new DynamoDBManager.FriendFoundForGetUIDByEmailListener(){

            @Override
            public void onFriendFound(String uid, String name, String email, String avatar, Boolean sex, String dateOfBirth) {
                userName=name;

            }

            @Override
            public void onFriendNotFound() {

            }

            @Override
            public void onError(Exception e) {

            }
        });
        //Log.d("CheckUserName", userName);
        updateRecyclerView();
        ((LinearLayoutManager)recyclerView.getLayoutManager()).setStackFromEnd(true);
        LiveData<List<Item>> messageLiveData = new MutableLiveData<>();
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        scrollToBottom();

        messageLiveData.observe(getViewLifecycleOwner(), new Observer<List<Item>>() {
            @Override
            public void onChanged(List<Item> items) {
                adapter.setItems(items);
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    // Add the new message to the list and notify adapter
                    String currentTime = Utils.getCurrentTime();
                    listMessage.add(new Item(currentTime, message,urlAvatar ,true));
                    adapter.notifyItemInserted(listMessage.size() - 1);
                    recyclerView.scrollToPosition(listMessage.size() - 1);
                    myWebSocket.sendMessage(message);
                    Log.d("CheckFriendName", friendName);
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            dynamoDBManager.saveMessage(uid + friend_id, message, currentTime, true);
                            dynamoDBManager.saveMessage(friend_id + uid, message, currentTime, false);
                            dynamoDBManager.saveConversation(uid, uid + friend_id, friend_id, message, currentTime, urlAvatar, friendName);
                            dynamoDBManager.saveConversation(friend_id, friend_id + uid, uid,message, currentTime, urlAvatar, userName);
                            return null;
                        }
                    }.execute();
                    scrollToBottom();
//                    changeData();
                } else {
                    Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                }
                // Clear the message input field after sending
                etMessage.getText().clear();
            }
        });
        btnImage.setOnClickListener(v->{
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        });
        btnBack.setOnClickListener(v -> {
            // Sử dụng FragmentManager để quản lý back stack và pop back khi cần thiết
            getParentFragmentManager().popBackStack();
            mainActivity.showBottomNavigation(true);
        });
        //File
        btnLink=view.findViewById(R.id.btnLink);
        btnLink.setOnClickListener(v->{
                    Intent intent = new Intent();
                    intent.setType("*/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Document"), PICK_DOCUMENT_REQUEST);
                }
        );
        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_DOCUMENT_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Log.d("CheckUri", uri.toString());
                uploadFile(uri);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                // Chuyển đổi bitmap thành chuỗi Base64
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                // Upload ảnh lên S3 và gửi tin nhắn chứa URL ảnh đến WebSocket, sau đó lưu vào DynamoDB
                uploadImageToS3AndSocketAndDynamoDB(uri);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToS3AndSocketAndDynamoDB(Uri uri) {
        new Thread(() -> {
            try {
                // Mở InputStream từ Uri
                InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);

                // Tạo tên file duy nhất
                String fileName = generateFileName();

                // Tạo đối tượng PutObjectRequest và đặt tên bucket và key
                request = new PutObjectRequest(BUCKET_NAME, fileName + ".jpg", inputStream, new ObjectMetadata());

                // Upload ảnh lên S3
                s3Client.putObject(request);

                // Lưu URL của ảnh vào biến để sử dụng sau này
                String urlImage = "https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/" + fileName + ".jpg";

                // Đóng InputStream sau khi tải lên thành công
                inputStream.close();

                // Gửi tin nhắn chứa URL ảnh đến WebSocket
                myWebSocket.sendMessage(urlImage);

                // Lưu tin nhắn và cuộc trò chuyện vào DynamoDB
                saveMessageAndConversationToDB(urlImage, "Hình ảnh");

                // Sau khi tất cả các thao tác hoàn tất, cập nhật giao diện
                getActivity().runOnUiThread(() -> {
                    String currentTime = Utils.getCurrentTime();
                    // Thêm tin nhắn mới vào danh sách
                    listMessage.add(new Item(currentTime, urlImage, urlAvatar, true));
                    adapter.notifyItemInserted(listMessage.size() - 1); // Thông báo cho adapter về sự thay đổi

                    // Cuộn xuống cuối RecyclerView
                    scrollToBottom();
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void saveMessageAndConversationToDB(String urlImage, String message) {
        String currentTime = Utils.getCurrentTime();
        // Lưu tin nhắn và cuộc trò chuyện vào DynamoDB
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                dynamoDBManager.saveMessage(uid + friend_id, urlImage, currentTime, true);
                dynamoDBManager.saveMessage(friend_id + uid, urlImage, currentTime, false);
                dynamoDBManager.saveConversation(uid, uid + friend_id, friend_id, message, currentTime, urlAvatar, friendName);
                dynamoDBManager.saveConversation(friend_id, friend_id + uid, uid, message, currentTime, urlAvatar, userName);
                return null;
            }
        }.execute();
    }



    private String getFileExtension(String mimetype){

        if(mimetype.equals("text/plain"))
            return ".txt";
        if(mimetype.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            return ".docx";
        if(mimetype.equals("application/pdf"))
            return ".pdf";
        if(mimetype.equals("image/png"))
            return ".png";
        else
            return null;
    }
    private void uploadFile (Uri uri){
        new Thread(()->{
            try {

                InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                Log.d("checkURI", uri.toString());
                File file = new File(uri.getPath());
                String random=generateFileName();
                String fileName = file.getName()+random;
                Log.d("checkFile", file.toString());
                Log.d("checkFileName", fileName);
                ContentResolver contentResolver = getActivity().getContentResolver();
                String mimeType = contentResolver.getType(uri);
                Log.d("mimeType", mimeType);
                String fileExtension =getFileExtension(mimeType);
                Log.d("fileExtension", fileExtension);

//
                request = new PutObjectRequest("chat-app-document-cnm", fileName+fileExtension, inputStream, new ObjectMetadata());
                String urlFile = "https://chat-app-document-cnm.s3.ap-southeast-1.amazonaws.com/" + fileName +fileExtension;
                Log.d("uploadFile: ",urlFile);
                s3Client.putObject(request);
                myWebSocket.sendMessage(urlFile);
                String currentTime = Utils.getCurrentTime();
                listMessage.add(new Item(currentTime, urlFile,urlAvatar ,true));
                adapter.notifyItemInserted(listMessage.size() - 1);
                recyclerView.scrollToPosition(listMessage.size() - 1);

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        String currentTime = Utils.getCurrentTime();
                        dynamoDBManager.saveMessage(uid + friend_id, urlFile, currentTime, true);
                        dynamoDBManager.saveMessage(friend_id + uid, urlFile, currentTime, false);
                        dynamoDBManager.saveConversation(uid, uid + friend_id, friend_id, "Tập tin", currentTime, urlAvatar, friendName);
                        dynamoDBManager.saveConversation(friend_id, friend_id + uid, uid,urlFile, "Tập tin", urlAvatar, userName);
                        return null;
                    }
                }.execute();
                // Đóng InputStream sau khi tải lên thành công
                inputStream.close();

            }catch (Exception e){

            }
        }).start();
    }
    private String generateFileName() {
        // Lấy ngày giờ hiện tại
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        // Tạo dãy số random
        int randomNumber = new Random().nextInt(10000);

        // Kết hợp ngày giờ và dãy số random để tạo tên file
        return "image_" + timeStamp + "_" + randomNumber;
    }

    private void initWebSocket() {
        // Kiểm tra xem channel_id đã được thiết lập chưa
        if (channel_id != null) {
            // Nếu đã có channel_id, thì khởi tạo myWebSocket
            myWebSocket = new MyWebSocket("wss://s12275.nyc1.piesocket.com/v3/"+channel_id+"?api_key=CIL9dbE6489dDCZhDUngwMm43Btfp4J9bdnxEK4m&notify_self=1", this);
        } else {
            // Nếu channel_id vẫn chưa được thiết lập, hiển thị thông báo hoặc xử lý lỗi tương ứng
            Log.e("WebSocket", "Error: Channel ID is null");
        }
    }

    @Override
    public void onMessageReceived(String receivedMessage) {
        Log.d("MessageReceived", receivedMessage);

        // Kiểm tra xem tin nhắn nhận được có trùng với tin nhắn đã gửi không
        boolean isDuplicate = false;
        for (Item item : listMessage) {
            if (item.getMessage().equals(receivedMessage)) {
                isDuplicate = true;
                break;
            }
        }
        scrollToBottom();
        if (!isDuplicate) {
            // Tin nhắn không trùng, thêm nó vào danh sách và cập nhật giao diện
            String currentTime = Utils.getCurrentTime();
            listMessage.add(new Item(currentTime, receivedMessage, urlAvatar,false));
            Log.d("CheckingListMessage", listMessage.toString());
            newPosition = listMessage.size() - 1; // Vị trí mới của tin nhắn
            adapter.notifyItemInserted(newPosition);

            // Kiểm tra nếu RecyclerView đã được attach vào layout
            if (recyclerView.getLayoutManager() != null) {
                // Cuộn xuống vị trí mới
                recyclerView.post(() -> recyclerView.smoothScrollToPosition(newPosition));
            } else {
                // Nếu RecyclerView chưa được attach, thì cuộn xuống khi RecyclerView được attach vào layout
                recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        recyclerView.smoothScrollToPosition(newPosition);
                    }
                });
            }
        }
    }

    @Override
    public void onConnectionStateChanged(boolean isConnected) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Ngắt kết nối khi Fragment bị hủy
        myWebSocket.closeWebSocket();
    }
    private void scrollToBottom() {
        if (recyclerView != null && recyclerView.getAdapter() != null) {
            int itemCount = recyclerView.getAdapter().getItemCount();
            if (itemCount > 0) {
                recyclerView.smoothScrollToPosition(itemCount - 1);
                // Hoặc có thể sử dụng recyclerView.scrollToPosition(itemCount - 1); nếu muốn cuộn mà không có hiệu ứng smooth
            }
        }
    }
    public void updateRecyclerView() {
        // Xóa bỏ các tin nhắn cũ từ listMessage
        listMessage.clear();

        // Thêm các tin nhắn mới từ DynamoDB vào danh sách hiện tại
        List<Item> newMessages = dynamoDBManager.loadMessages(uid+friend_id);
        for (Item message : newMessages) {
            // Tạo một đối tượng Message mới với thông tin từ tin nhắn và avatar
            Item newMessage = new Item(message.getTime(), message.getMessage(), urlAvatar,message.isSentByMe());
            listMessage.add(newMessage);
        }

        // Cập nhật RecyclerView
        adapter.notifyDataSetChanged();

        // Cuộn đến vị trí cuối cùng
        recyclerView.scrollToPosition(listMessage.size() - 1);
    }
    private void changeData() {
        viewModel.setData("New data");
    }

}