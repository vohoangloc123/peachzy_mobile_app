package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.example.peachzyapp.LiveData.MyGroupViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.Other.Utils;
import com.example.peachzyapp.R;
import com.example.peachzyapp.SocketIO.MyWebSocket;
import com.example.peachzyapp.adapters.GroupChatBoxAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.GroupChat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.Upload;


public class GroupChatBoxFragment extends Fragment  implements MyWebSocket.WebSocketListener {
    public static final String TAG= GroupChatBoxFragment.class.getName();
    TextView tvGroupName;
    EditText etGroupMessage;
    ImageButton btnSend;
    RecyclerView recyclerView;
    String groupID;
    String groupName;
    String groupAvatar;
    String userID;
    private List<GroupChat> listGroupMessage = new ArrayList<>();
    MyWebSocket myWebSocket;
    MainActivity mainActivity;
    private DynamoDBManager dynamoDBManager;
    private GroupChatBoxAdapter adapter;
    int newPosition;
    private String userName;
    private String userAvatar;
    ImageButton btnImage;
    ImageButton btnOption;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_DOCUMENT_REQUEST = 2;
    PutObjectRequest request;
    private static final String BUCKET_NAME = "chat-app-image-cnm";
    private static final String BUCKET_NAME_FOR_VIDEO = "chat-app-video-cnm";
    private AmazonS3 s3Client;
    private ImageButton btnLink;
    private ImageButton btnBack;
    private MyGroupViewModel viewModel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyGroupViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_group_chat_box, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MyGroupViewModel.class);
        Bundle bundleReceive=getArguments();
        tvGroupName=view.findViewById(R.id.tvGroupName);
        recyclerView=view.findViewById(R.id.rcvGroupChat);
        btnSend = view.findViewById(R.id.btnGroupSend);
        btnImage=view.findViewById(R.id.btnGroupImage);
        btnBack=view.findViewById(R.id.btnBack);
        btnOption=view.findViewById(R.id.btnOption);
        etGroupMessage=view.findViewById(R.id.etGroupMessage);
        // Set up RecyclerView layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //xử lý resize giao diện và đẩy edit text và button lên khi chat ngoài ra còn load tin nhắn mói từ dưới lên
        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);

        //bundle
        groupID = bundleReceive.getString("groupID");
        Log.d("CheckBundleOfGroupChat", "onCreateView: "+groupID);
        groupName= bundleReceive.getString("groupName");
        Log.d("CheckBundleOfGroupChat", "onCreateView: "+groupName);
        groupAvatar= bundleReceive.getString("groupAvatar");
        Log.d("CheckBundleOfGroupChat", "onCreateView: "+groupAvatar);
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userID = preferences.getString("uid", null);
        if (userID != null) {
            Log.d("FriendcheckUID", userID);
            // Sử dụng "uid" ở đây cho các mục đích của bạn
        } else {
            Log.e("FriendcheckUID", "UID is null");
        }
        //


        //set to UI from bundle
        tvGroupName.setText(groupName);
        //initial
        mainActivity = (MainActivity) getActivity();
        dynamoDBManager = new DynamoDBManager(getContext());
        // Tạo Amazon S3 client
        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAZI2LEH5QHYJMDGHD", "57MJpyB+ZOaL1XHIgjb1fdBsXc4HnH/S2lkEYDQ/");
        s3Client = new AmazonS3Client(credentials);
        s3Client.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
        //adapter
        adapter = new GroupChatBoxAdapter(getContext(), listGroupMessage, userID);
        recyclerView.setAdapter(adapter);
        //load messages
        updateRecyclerView();
        ((LinearLayoutManager)recyclerView.getLayoutManager()).setStackFromEnd(true);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        //web socket
        initWebSocket();
        dynamoDBManager.getProfileByUID(userID, new DynamoDBManager.FriendFoundForGetUIDByEmailListener() {
            @Override
            public void onFriendFound(String uid, String name, String email, String avatar, Boolean sex, String dateOfBirth) {
            }

            @Override
            public void onFriendFound(String uid, String name, String email, String avatar, Boolean sex, String dateOfBirth, String role) {
                userName=name;
                userAvatar=avatar;
            }

            @Override
            public void onFriendNotFound() {

            }

            @Override
            public void onError(Exception e) {

            }
        });
        scrollToBottom();
        btnSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String message = etGroupMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    // Add the new message to the list and notify adapter
                    String currentTime = Utils.getCurrentTime();
                    //B1 hiển thị lên giao diện chat
                    listGroupMessage.add(new GroupChat(groupID, groupName, userAvatar, message, userName, currentTime, userID));
                    adapter.notifyItemInserted(listGroupMessage.size() - 1);
                    recyclerView.scrollToPosition(listGroupMessage.size() - 1);
                    //B2 gửi lên socket
                    myWebSocket.sendMessage(message);
                    //B3 đẩy lên dynamoDB để load lại tin nhắn khi out ra khung chat
                    dynamoDBManager.saveGroupMessage(groupID, message, currentTime, userID, userAvatar, userName);
                    dynamoDBManager.saveGroupConversation(groupID, message, groupName, currentTime,userAvatar, userName);
                    scrollToBottom();
                    changeData();
                } else {
                    Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                }
                // Clear the message input field after sending
                etGroupMessage.getText().clear();
            }
        });
        btnImage.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("*/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"image/*", "video/*"}); // Cho phép chọn cả video và ảnh
            startActivityForResult(Intent.createChooser(intent, "Select Image or Video"), PICK_IMAGE_REQUEST);
        });
        btnLink=view.findViewById(R.id.btnGroupLink);
        btnLink.setOnClickListener(v->{
                    Intent intent = new Intent();
                    intent.setType("*/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Document"), PICK_DOCUMENT_REQUEST);
                }
        );
        btnOption.setOnClickListener(v->{
            Bundle bundle = new Bundle();
            bundle.putString("groupID", groupID);
            bundle.putString("groupName",groupName);
            bundle.putString("groupAvatar", groupAvatar);
            mainActivity.goToGroupOption(bundle);
        });
        btnBack.setOnClickListener(v->{
            getActivity().getSupportFragmentManager().popBackStack();
            mainActivity.showBottomNavigation(true);
        });
        return view;
    }

    private void uploadFile (Uri uri){
        new Thread(()->{
            try {

                InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);
                File file = new File(uri.getPath());
                String random=generateFileName();
                String fileName = file.getName()+random;
                ContentResolver contentResolver = getActivity().getContentResolver();
                String mimeType = contentResolver.getType(uri);
                String fileExtension =getFileExtension(mimeType);


                request = new PutObjectRequest("chat-app-document-cnm", fileName+fileExtension, inputStream, new ObjectMetadata());
                String urlFile = "https://chat-app-document-cnm.s3.ap-southeast-1.amazonaws.com/" + fileName +fileExtension;
                Log.d("uploadFile: ",urlFile);
                //đẩy lên s3
                s3Client.putObject(request);
                myWebSocket.sendMessage(urlFile);
                String currentTime = Utils.getCurrentTime();
                listGroupMessage.add(new GroupChat(groupID, groupName, userAvatar, urlFile, userName, currentTime, userID));
                adapter.notifyItemInserted(listGroupMessage.size() - 1);
                recyclerView.scrollToPosition(listGroupMessage.size() - 1);

                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        String currentTime = Utils.getCurrentTime();
                        dynamoDBManager.saveGroupMessage(groupID, urlFile, currentTime, userID, userAvatar, userName);
                        dynamoDBManager.saveGroupConversation(groupID, urlFile, groupName, currentTime,userAvatar, userName);
                        return null;
                    }
                }.execute();
                // Đóng InputStream sau khi tải lên thành công
                inputStream.close();

            }catch (Exception e){

            }
        }).start();
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
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_DOCUMENT_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri documentUri = data.getData();
            try {
                Log.d("CheckDocumentUri", documentUri.toString());
                uploadFile(documentUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri fileUri=data.getData();
            String mimeType = getActivity().getContentResolver().getType(fileUri);
            if(mimeType != null && mimeType.startsWith("image/"))
            {
                Log.d("IsVideo", "NO");
                if (data.getClipData() != null) {
                    // Người dùng chọn nhiều hình ảnh
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                            // Chuyển đổi bitmap thành chuỗi Base64
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                            // Upload ảnh lên S3 và gửi tin nhắn chứa URL ảnh đến WebSocket, sau đó lưu vào DynamoDB
                            uploadImageToS3AndSocketAndDynamoDB(imageUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (data.getData() != null) {
                    // Người dùng chỉ chọn một hình ảnh
                    Uri imageUri = data.getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        // Chuyển đổi bitmap thành chuỗi Base64
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                        // Upload ảnh lên S3 và gửi tin nhắn chứa URL ảnh đến WebSocket, sau đó lưu vào DynamoDB
                        uploadImageToS3AndSocketAndDynamoDB(imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else if(mimeType != null && mimeType.startsWith("video/")) {
                Log.d("IsVideo", "YES");
                Log.d("IsVideo", fileUri.toString());
                uploadVideoToS3AndSocketAndDynamoDB(fileUri);
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
                    listGroupMessage.add(new GroupChat(groupID, groupName, userAvatar,urlImage, userName, currentTime, userID));
                    adapter.notifyItemInserted(listGroupMessage.size() - 1); // Thông báo cho adapter về sự thay đổi

                    // Cuộn xuống cuối RecyclerView
                    scrollToBottom();
                    changeData();
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void uploadVideoToS3AndSocketAndDynamoDB(Uri uri) {
        new Thread(() -> {
            try {
                Log.d("IsVideo", "OnUploadVideo");
                // Mở InputStream từ Uri
                InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);

                if (inputStream != null) {
                    // Tạo tên file duy nhất
                    String fileName = generateFileName();

                    // Tạo đối tượng để lưu trữ ETags của các phần đã tải lên
                    Map<Integer, String> partETags = new HashMap<>();

                    // Khởi tạo UploadPartRequest với kích thước phần tối đa
                    final int MB = 1024 * 1024; // 1 MB
                    final long partSize = 5 * MB; // Kích thước tối đa của mỗi phần
                    InitiateMultipartUploadRequest initiateRequest = new InitiateMultipartUploadRequest(BUCKET_NAME_FOR_VIDEO, fileName + ".mp4");
                    InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initiateRequest);

                    // Tính số lượng phần
                    long fileSize = inputStream.available();
                    int partCount = (int) Math.ceil((double) fileSize / partSize);

                    try {
                        // Tải lần lượt từng phần lên S3
                        for (int i = 0; i < partCount; i++) {
                            long offset = i * partSize;
                            long remainingBytes = fileSize - offset;
                            long bytesToRead = Math.min(partSize, remainingBytes);

                            // Đọc phần dữ liệu từ InputStream
                            byte[] partData = new byte[(int) bytesToRead];
                            inputStream.read(partData);

                            // Upload phần lên S3
                            UploadPartRequest uploadRequest = new UploadPartRequest()
                                    .withBucketName(BUCKET_NAME_FOR_VIDEO)
                                    .withKey(fileName + ".mp4")
                                    .withUploadId(initResponse.getUploadId())
                                    .withPartNumber(i + 1)
                                    .withPartSize(bytesToRead)
                                    .withInputStream(new ByteArrayInputStream(partData));
                            UploadPartResult uploadResult = s3Client.uploadPart(uploadRequest);

                            // Lưu ETag của phần đã tải lên
                            partETags.put(i + 1, uploadResult.getETag());
                        }

                        // Tạo danh sách PartETag từ Map<Integer, String>
                        List<PartETag> partETagList = new ArrayList<>();
                        for (Map.Entry<Integer, String> entry : partETags.entrySet()) {
                            partETagList.add(new PartETag(entry.getKey(), entry.getValue()));
                        }

                        // Hoàn thành multipart upload
                        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(BUCKET_NAME_FOR_VIDEO, fileName + ".mp4", initResponse.getUploadId(), partETagList);
                        s3Client.completeMultipartUpload(completeRequest);

                        // Đóng InputStream sau khi upload hoàn tất
                        inputStream.close();

                        // Nếu cần, bạn có thể thực hiện các thao tác khác sau khi upload hoàn tất ở đây

                    } catch (Exception e) {
                        // Xử lý lỗi
                        e.printStackTrace();
                        Log.e("UploadVideoToS3", "Error uploading video to S3: " + e.getMessage());

                        // Hủy bỏ multipart upload nếu có lỗi xảy ra
                        s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(BUCKET_NAME_FOR_VIDEO, fileName + ".mp4", initResponse.getUploadId()));
                    }

                    // Lấy URL của video trên S3
                    String urlVideo = s3Client.getUrl(BUCKET_NAME_FOR_VIDEO, fileName + ".mp4").toString();

                    // Gửi tin nhắn chứa URL video đến WebSocket
                    myWebSocket.sendMessage(urlVideo);

                    // Lưu tin nhắn và cuộc trò chuyện vào DynamoDB
                    saveMessageAndConversationToDB(urlVideo, "Video");

                    // Cập nhật giao diện trên luồng UI
                    getActivity().runOnUiThread(() -> {
                        String currentTime = Utils.getCurrentTime();
                        // Thêm tin nhắn mới vào danh sách
                        listGroupMessage.add(new GroupChat(groupID, groupName, userAvatar, urlVideo, userName, currentTime, userID));
                        adapter.notifyItemInserted(listGroupMessage.size() - 1); // Thông báo cho adapter về sự thay đổi

                        // Cuộn xuống cuối RecyclerView
                        scrollToBottom();
                        changeData();
                    });
                } else {
                    // Xử lý trường hợp inputStream là null
                    Log.e("UploadVideoToS3", "InputStream is null");
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Xử lý lỗi IOException
                Log.e("UploadVideoToS3", "Error uploading video to S3: " + e.getMessage());
            } catch (AmazonServiceException e) {
                e.printStackTrace();
                // Xử lý lỗi AmazonServiceException
                Log.e("UploadVideoToS3", "Error uploading video to S3: " + e.getMessage());
            } catch (AmazonClientException e) {
                e.printStackTrace();
                // Xử lý lỗi AmazonClientException
                Log.e("UploadVideoToS3", "Error uploading video to S3: " + e.getMessage());
            }
        }).start();
    }

    private void saveMessageAndConversationToDB(String urlImage, String message) {
        String currentTime = Utils.getCurrentTime();
        // Lưu tin nhắn và cuộc trò chuyện vào DynamoDB
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                dynamoDBManager.saveGroupMessage(groupID, urlImage, currentTime, userID, userAvatar, userName);
                dynamoDBManager.saveGroupConversation(groupID, message, groupName, currentTime,userAvatar, userName);
                return null;
            }
        }.execute();
    }

    private String generateFileName() {
        // Lấy ngày giờ hiện tại
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        // Tạo dãy số random
        int randomNumber = new Random().nextInt(10000);

        // Kết hợp ngày giờ và dãy số random để tạo tên file
        return "image_" + timeStamp + "_" + randomNumber;
    }

    public void updateRecyclerView() {
        // Xóa bỏ các tin nhắn cũ từ listMessage
        listGroupMessage.clear();
        // Thêm các tin nhắn mới từ DynamoDB vào danh sách hiện tại
        List<GroupChat> newMessages = dynamoDBManager.loadGroupMessages(groupID);
        for (GroupChat message : newMessages) {
            // Tạo một đối tượng Message mới với thông tin từ tin nhắn và avatar
            GroupChat newMessage=new GroupChat(message.getAvatar(), message.getMessage(), message.getName(), message.getTime(), message.getUserID());
            Log.d("CheckNewMessage", newMessage.toString());
            listGroupMessage.add(newMessage);
        }

        // Cập nhật RecyclerView
        adapter.notifyDataSetChanged();

        // Cuộn đến vị trí cuối cùng
        recyclerView.scrollToPosition(listGroupMessage.size() - 1);
    }
    private void initWebSocket() {
        // Kiểm tra xem channel_id đã được thiết lập chưa
        if (groupID != null) {
            // Nếu đã có channel_id, thì khởi tạo myWebSocket
            myWebSocket = new MyWebSocket("wss://s12275.nyc1.piesocket.com/v3/"+groupID+"?api_key=CIL9dbE6489dDCZhDUngwMm43Btfp4J9bdnxEK4m&notify_self=1", this);
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
        for (GroupChat groupChatItem : listGroupMessage) {
            if (groupChatItem.getMessage().equals(receivedMessage)) {
                isDuplicate = true;
                break;
            }
        }
        scrollToBottom();
        if (!isDuplicate) {
            // Tin nhắn không trùng, thêm nó vào danh sách và cập nhật giao diện
            String currentTime = Utils.getCurrentTime();
            listGroupMessage.add(new GroupChat(groupID, groupName,"https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/avatar.jpg", receivedMessage, "Loc", currentTime, "111"));
            Log.d("CheckingListMessage",  listGroupMessage.toString());
            //B2.1 load dữ liệu
            newPosition = listGroupMessage.size() - 1; // Vị trí mới của tin nhắn
            adapter.notifyItemInserted(newPosition);
            scrollToBottom();
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
    private void changeData() {
        viewModel.setData("New data");
    }

}
