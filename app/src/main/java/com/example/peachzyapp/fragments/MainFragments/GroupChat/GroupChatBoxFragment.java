package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
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
import android.widget.ScrollView;
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
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.example.peachzyapp.LiveData.MyGroupViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.Other.Utils;
import com.example.peachzyapp.R;
import com.example.peachzyapp.SocketIO.MyWebSocket;
import com.example.peachzyapp.adapters.ChatBoxAdapter;
import com.example.peachzyapp.adapters.GroupChatBoxAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.GroupChat;
import com.example.peachzyapp.entities.Item;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
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

    private TextView tvGroupName;
    private EditText etGroupMessage;
    private ImageButton btnSend, btnImage, btnVideo, btnOption, btnBack, btnLink, btnAudio;
    private RecyclerView recyclerView;
    private String groupID, groupName, groupAvatar, userID, userName, userAvatar;
    private List<GroupChat> listGroupMessage = new ArrayList<>();
    private MyWebSocket myWebSocket;
    private MainActivity mainActivity;
    private DynamoDBManager dynamoDBManager;
    private GroupChatBoxAdapter adapter;
    private int newPosition;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_DOCUMENT_REQUEST = 2;
    private static final int PICK_VIDEO_REQUEST = 3;
    PutObjectRequest request;
    private static final String BUCKET_NAME = "chat-app-image-cnm";
    private static final String BUCKET_NAME_FOR_DOCUMENT = "chat-app-document-cnm";
    private static final String BUCKET_NAME_FOR_VIDEO = "chat-app-video-cnm";
    private AmazonS3 s3Client;
    private MyGroupViewModel viewModel;
    private String thisType, key;
    private ArrayList<String> listMember;
    String outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";
    private boolean isRecording = false; // Flag to check if recording is in progress
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private static final String LOG_TAG = "AudioRecordTest";
    private boolean permissionToRecordAccepted = false;
    private MediaRecorder recorder = null;
    private String audioFilePath;
    private static final String BUCKET_NAME_FOR_VOICE = "chat-app-audio-cnm";
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
        btnVideo=view.findViewById(R.id.btnGroupVideo);
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
        listMember=new ArrayList<>();
        dynamoDBManager.getMembersGroup(groupID, new DynamoDBManager.GroupMemberListener() {
            @Override
            public void onMemberLoaded(String member) {
                listMember.add(member);
            }

            @Override
            public void onError(String errorMessage) {

            }
        });
        dynamoDBManager.resetlastReadAndUnread(userID, groupID);
        scrollToBottom();
        btnSend.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String message = etGroupMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    // Add the new message to the list and notify adapter
                    String currentTime = getCurrentDateTime();
                    //B1 hiển thị lên giao diện chat
                    listGroupMessage.add(new GroupChat(groupID, groupName, userAvatar, message, userName, currentTime, userID, "text"));
                    adapter.notifyItemInserted(listGroupMessage.size() - 1);
                    recyclerView.scrollToPosition(listGroupMessage.size() - 1);
                    //B2 gửi lên socket
                    JSONObject messageToSend = new JSONObject();
                    // Tạo đối tượng JSON chứa trường type và message
                    JSONObject json = new JSONObject();
                    try{
                        messageToSend.put("memberID", userID);
                        messageToSend.put("memberName", userName);
                        messageToSend.put("memberAvatar", userAvatar);
                        messageToSend.put("message", message);
                        messageToSend.put("time", currentTime);
                        messageToSend.put("type", "text");
                        json.put("type", "send-group-message");
                        json.put("message", messageToSend);
                    }catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    myWebSocket.sendMessage(String.valueOf(json));
                    //B3 đẩy lên dynamoDB để load lại tin nhắn khi out ra khung chat
                    dynamoDBManager.saveGroupMessage(groupID, message, currentTime, userID, userAvatar, userName, "text");
                    dynamoDBManager.saveGroupConversation(groupID, message, groupName, currentTime,userAvatar, userName);
                    updateLastSeenAndRead();
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
            intent.setType("image/*"); // Chỉ lấy video
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Cho phép chọn nhiều video
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST);
        });
        btnVideo.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("video/*"); // Chỉ lấy video
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // Cho phép chọn nhiều video
            startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST);
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
        btnAudio=view.findViewById(R.id.btnAudio);
        btnAudio=view.findViewById(R.id.btnAudio);
        btnAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Nếu đang ghi âm, dừng ghi âm. Ngược lại, bắt đầu ghi âm.
                if (!isRecording) {
                    // Bắt đầu ghi âm
                    startRecording();
                    btnAudio.setImageResource(R.drawable.baseline_stop_circle_24);
                } else {
                    // Dừng ghi âm
                    stopRecording();
                    btnAudio.setImageResource(R.drawable.baseline_keyboard_voice_24);
                }
                // Đảo ngược trạng thái của biến isRecording
                isRecording = !isRecording;
            }
        });
        btnBack.setOnClickListener(v->{
            getActivity().getSupportFragmentManager().popBackStack();
            mainActivity.showBottomNavigation(true);
        });

        adapter.setOnItemLongClickListener(new ChatBoxAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(int position) {
                // Xử lý hành động khi nhấn giữ
                showOptionsDialog(position);
            }
        });

        return view;
    }

    private void startRecording() {
        // Khởi tạo MediaRecorder
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        // Tạo một temporary file để lưu trữ dữ liệu âm thanh trước khi tải lên S3
        try {
            // Tạo một file tạm thời với đường dẫn tạm thời
            File directory = getActivity().getExternalFilesDir(Environment.DIRECTORY_MUSIC);
            audioFilePath = directory.getAbsolutePath() + "/audio_record.3gp";

            recorder.setOutputFile(audioFilePath);

            // Chuẩn bị và bắt đầu ghi âm
            recorder.prepare();
            recorder.start();
            Log.i(LOG_TAG, "Recording started");
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            Log.i(LOG_TAG, "Recording stopped");

            // Tải tệp âm thanh lên S3 sau khi ghi âm kết thúc
            uploadAudioToS3AndSocketAndDynamoDB(audioFilePath);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_RECORD_AUDIO_PERMISSION:
                permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
        if (!permissionToRecordAccepted) {
            getActivity().finish();
        }
    }
    private void uploadFile (Uri uri){
        new Thread(()->{
            try {
                File file = new File(uri.getPath());
                ContentResolver contentResolver = getActivity().getContentResolver();
                String mimeType = contentResolver.getType(uri);
                String fileExtension =getFileExtension(mimeType);
                uploadDocumentToS3AndSocketAndDynamoDB(uri, fileExtension);
            }catch (Exception e){

            }
        }).start();
    }
    private String getFileExtension(String mimetype){

        if(mimetype.equals("text/plain"))
            return "txt";
        if(mimetype.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
            return "docx";
        if(mimetype.equals("application/pdf"))
            return "pdf";
        if(mimetype.equals("image/png"))
            return "png";
        else
            return null;
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
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
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
        }
        if (requestCode == PICK_VIDEO_REQUEST && resultCode == Activity.RESULT_OK && data != null){
            if (data.getClipData() != null) {
                // Người dùng chọn nhiều hình ảnh
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri videoUri = data.getClipData().getItemAt(i).getUri();
                    Log.d("CheckVideoUri", videoUri.toString());
                    String mimeType = getActivity().getContentResolver().getType(videoUri);
                    if(mimeType != null && mimeType.startsWith("video/")){
                        uploadVideoToS3AndSocketAndDynamoDB(videoUri);
                    }
                }
            }
        }
    }

    private void uploadImageToS3AndSocketAndDynamoDB(Uri uri) {
        new Thread(() -> {
            try {
                // Mở InputStream từ Uri
                Log.d("IsImage", "OnUploadImage");
                // Mở InputStream từ Uri
                InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);

                if (inputStream != null) {
                    // Tạo tên file duy nhất
                    String fileName = generateFileName("image");

                    // Tạo đối tượng để lưu trữ ETags của các phần đã tải lên
                    Map<Integer, String> partETags = new HashMap<>();

                    // Khởi tạo UploadPartRequest với kích thước phần tối đa
                    final int MB = 1024 * 1024; // 1 MB
                    final long partSize = 5 * MB; // Kích thước tối đa của mỗi phần
                    InitiateMultipartUploadRequest initiateRequest = new InitiateMultipartUploadRequest(BUCKET_NAME, fileName + ".jpg");
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
                                    .withBucketName(BUCKET_NAME)
                                    .withKey(fileName + ".jpg")
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
                        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(BUCKET_NAME, fileName + ".jpg", initResponse.getUploadId(), partETagList);
                        s3Client.completeMultipartUpload(completeRequest);

                        // Đóng InputStream sau khi upload hoàn tất
                        inputStream.close();

                        // Nếu cần, bạn có thể thực hiện các thao tác khác sau khi upload hoàn tất ở đây

                    } catch (Exception e) {
                        // Xử lý lỗi
                        e.printStackTrace();
                        Log.e("UploaImageToS3", "Error uploading video to S3: " + e.getMessage());

                        // Hủy bỏ multipart upload nếu có lỗi xảy ra
                        s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(BUCKET_NAME, fileName + ".jpg", initResponse.getUploadId()));
                    }

                    // Lấy URL của video trên S3
                    String urlImage = s3Client.getUrl(BUCKET_NAME, fileName + ".jpg").toString();
                    // Gửi tin nhắn chứa URL video đến WebSocket
                    JSONObject messageToSend = new JSONObject();
                    JSONObject json = new JSONObject();
                    try{
                        messageToSend.put("memberID", userID);
                        messageToSend.put("memberName", userName);
                        messageToSend.put("memberAvatar", userAvatar);
                        //messageToSend.put("avatar", userAvatar);
                        messageToSend.put("message", urlImage);
                        messageToSend.put("time", getCurrentDateTime());
                        messageToSend.put("type", "image");

                        json.put("type", "send-group-message");
                        json.put("message", messageToSend);
                    }catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    //myWebSocket.sendMessage(String.valueOf(messageToSend));
                    myWebSocket.sendMessage(String.valueOf(json));

                    // Lưu tin nhắn và cuộc trò chuyện vào DynamoDB
                    saveMessageAndConversationToDB(urlImage, "Image", "image");

                    // Cập nhật giao diện trên luồng UI
                    getActivity().runOnUiThread(() -> {
                        String currentTime = getCurrentDateTime();
                        // Thêm tin nhắn mới vào danh sách
                        listGroupMessage.add(new GroupChat(groupID, groupName, userAvatar, urlImage, userName, currentTime, userID, "image"));
                        adapter.notifyItemInserted(listGroupMessage.size() - 1); // Thông báo cho adapter về sự thay đổi

                        // Cuộn xuống cuối RecyclerView
                        scrollToBottom();
                        changeData();
                    });
                } else {
                    // Xử lý trường hợp inputStream là null
                    Log.e("UploadImageToS3", "InputStream is null");
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Xử lý lỗi IOException
                Log.e("UploadImageToS3", "Error uploading image to S3: " + e.getMessage());
            } catch (AmazonServiceException e) {
                e.printStackTrace();
                // Xử lý lỗi AmazonServiceException
                Log.e("UploadImageToS3", "Error uploading image to S3: " + e.getMessage());
            } catch (AmazonClientException e) {
                e.printStackTrace();
                // Xử lý lỗi AmazonClientException
                Log.e("UploadImageToS3", "Error uploading image to S3: " + e.getMessage());
            }
        }).start();
    }
    private void uploadAudioToS3AndSocketAndDynamoDB(String filePath) {
        new Thread(() -> {
            try {
                Log.d("IsAudio", "OnUploadAudio");

                // Kiểm tra đường dẫn file âm thanh
                if (filePath != null && !filePath.isEmpty()) {
                    // Tạo tên file duy nhất
                    String fileName = generateFileName("audio");

                    // Đọc dữ liệu từ file âm thanh
                    File audioFile = new File(filePath);
                    InputStream inputStream = new FileInputStream(audioFile);

                    // Tạo đối tượng để lưu trữ ETags của các phần đã tải lên
                    Map<Integer, String> partETags = new HashMap<>();

                    // Khởi tạo UploadPartRequest với kích thước phần tối đa
                    final int MB = 1024 * 1024; // 1 MB
                    final long partSize = 5 * MB; // Kích thước tối đa của mỗi phần
                    InitiateMultipartUploadRequest initiateRequest = new InitiateMultipartUploadRequest(BUCKET_NAME_FOR_VOICE, fileName + ".mp3");
                    InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initiateRequest);

                    // Tính số lượng phần
                    long fileSize = audioFile.length();
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
                                    .withBucketName(BUCKET_NAME_FOR_VOICE)
                                    .withKey(fileName + ".mp3")
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
                        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(BUCKET_NAME_FOR_VOICE, fileName + ".mp3", initResponse.getUploadId(), partETagList);
                        s3Client.completeMultipartUpload(completeRequest);

                        // Đóng InputStream sau khi upload hoàn tất
                        inputStream.close();

                        // Lấy URL của audio trên S3
                        String urlAudio = s3Client.getUrl(BUCKET_NAME_FOR_VOICE, fileName + ".mp3").toString();

                        // Gửi tin nhắn chứa URL audio đến WebSocket
                        JSONObject messageToSend = new JSONObject();
                        JSONObject json = new JSONObject();
                        try {
                            messageToSend.put("memberID", userID);
                            messageToSend.put("memberName", userName);
                            messageToSend.put("memberAvatar", userAvatar);
                            messageToSend.put("message", urlAudio);
                            messageToSend.put("time", getCurrentDateTime());
                            messageToSend.put("type", "voice");
                            json.put("type", "send-group-message");
                            json.put("message", messageToSend);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        myWebSocket.sendMessage(String.valueOf(json));

                        // Lưu tin nhắn và cuộc trò chuyện vào DynamoDB
                        saveMessageAndConversationToDB(urlAudio, "Voice", "voice");

                        // Cập nhật giao diện trên luồng UI
                        getActivity().runOnUiThread(() -> {
                            String currentTime = getCurrentDateTime();
                            // Thêm tin nhắn mới vào danh sách
                            listGroupMessage.add(new GroupChat(groupID, groupName, userAvatar, urlAudio, userName, currentTime, userID, "voice"));
                            adapter.notifyItemInserted(listGroupMessage.size() - 1); // Thông báo cho adapter về sự thay đổi

                            // Cuộn xuống cuối RecyclerView
                            scrollToBottom();
                            changeData();
                        });
                    } catch (Exception e) {
                        // Xử lý lỗi
                        e.printStackTrace();
                        Log.e("UploadAudioToS3", "Error uploading audio to S3: " + e.getMessage());

                        // Hủy bỏ multipart upload nếu có lỗi xảy ra
                        s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(BUCKET_NAME_FOR_VOICE, fileName + ".mp3", initResponse.getUploadId()));
                    }
                } else {
                    // Xử lý trường hợp đường dẫn là null hoặc rỗng
                    Log.e("UploadAudioToS3", "File path is null or empty");
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Xử lý lỗi IOException
                Log.e("UploadAudioToS3", "Error uploading audio to S3: " + e.getMessage());
            } catch (AmazonServiceException e) {
                e.printStackTrace();
                // Xử lý lỗi AmazonServiceException
                Log.e("UploadAudioToS3", "Error uploading audio to S3: " + e.getMessage());
            } catch (AmazonClientException e) {
                e.printStackTrace();
                // Xử lý lỗi AmazonClientException
                Log.e("UploadAudioToS3", "Error uploading audio to S3: " + e.getMessage());
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
                    String fileName = generateFileName("video");

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
                    JSONObject messageToSend = new JSONObject();
                    JSONObject json = new JSONObject();
                    try{

                        messageToSend.put("memberID", userID);
                        messageToSend.put("memberName", userName);

                        messageToSend.put("memberAvatar", userAvatar);
                        //messageToSend.put("avatar", userAvatar);
                        messageToSend.put("message", urlVideo);//
                        messageToSend.put("time", getCurrentDateTime());
                        messageToSend.put("type", "video");

                        json.put("type", "send-group-message");
                        json.put("message", messageToSend);
                    }catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    //myWebSocket.sendMessage(String.valueOf(messageToSend));
                    myWebSocket.sendMessage(String.valueOf(json));

                    // Lưu tin nhắn và cuộc trò chuyện vào DynamoDB
                    saveMessageAndConversationToDB(urlVideo, "Video", "video");

                    // Cập nhật giao diện trên luồng UI
                    getActivity().runOnUiThread(() -> {
                        String currentTime = getCurrentDateTime();
                        // Thêm tin nhắn mới vào danh sách
                        listGroupMessage.add(new GroupChat(groupID, groupName, userAvatar, urlVideo, userName, currentTime, userID, "video"));
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

    private void uploadDocumentToS3AndSocketAndDynamoDB(Uri uri, String fileType) {
        new Thread(() -> {
            try {
                // Mở InputStream từ Uri
                Log.d("IsImage", "OnUploadImage");
                // Mở InputStream từ Uri
                InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);

                if (inputStream != null) {
                    // Tạo tên file duy nhất
                    String fileName = generateFileName("document");

                    // Tạo đối tượng để lưu trữ ETags của các phần đã tải lên
                    Map<Integer, String> partETags = new HashMap<>();

                    // Khởi tạo UploadPartRequest với kích thước phần tối đa
                    final int MB = 1024 * 1024; // 1 MB
                    final long partSize = 5 * MB; // Kích thước tối đa của mỗi phần
                    InitiateMultipartUploadRequest initiateRequest = new InitiateMultipartUploadRequest(BUCKET_NAME_FOR_DOCUMENT, fileName + "."+fileType);
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
                                    .withBucketName(BUCKET_NAME_FOR_DOCUMENT)
                                    .withKey(fileName + "."+fileType)
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
                        CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(BUCKET_NAME_FOR_DOCUMENT, fileName + "."+fileType, initResponse.getUploadId(), partETagList);
                        s3Client.completeMultipartUpload(completeRequest);

                        // Đóng InputStream sau khi upload hoàn tất
                        inputStream.close();

                        // Nếu cần, bạn có thể thực hiện các thao tác khác sau khi upload hoàn tất ở đây

                    } catch (Exception e) {
                        // Xử lý lỗi
                        e.printStackTrace();
                        Log.e("UploadDocumentToS3", "Error uploading document to S3: " + e.getMessage());

                        // Hủy bỏ multipart upload nếu có lỗi xảy ra
                        s3Client.abortMultipartUpload(new AbortMultipartUploadRequest(BUCKET_NAME, fileName + "."+fileType, initResponse.getUploadId()));
                    }

                    // Lấy URL của video trên S3
                    String urlDocument = s3Client.getUrl(BUCKET_NAME_FOR_DOCUMENT, fileName + "."+fileType).toString();
                    // Gửi tin nhắn chứa URL video đến WebSocket
                    JSONObject messageToSend = new JSONObject();
                    JSONObject json = new JSONObject();
                    try{
                        json.put("type", "send-group-message");
                        messageToSend.put("memberID", userID);
                        messageToSend.put("memberName", userName);
                        messageToSend.put("memberAvatar", userAvatar);
                        messageToSend.put("message", urlDocument);
                        messageToSend.put("time", getCurrentDateTime());
                        messageToSend.put("type", "document");
                        json.put("message", messageToSend);
                    }catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    //myWebSocket.sendMessage(String.valueOf(messageToSend));
                    myWebSocket.sendMessage(String.valueOf(json));
                    // Lưu tin nhắn và cuộc trò chuyện vào DynamoDB
                    saveMessageAndConversationToDB(urlDocument, "Document", "document");
                    // Cập nhật giao diện trên luồng UI
                    getActivity().runOnUiThread(() -> {
                        String currentTime = getCurrentDateTime();
                        // Thêm tin nhắn mới vào danh sách
                        listGroupMessage.add(new GroupChat(groupID, groupName, userAvatar, urlDocument, userName, currentTime, userID, "document"));
                        adapter.notifyItemInserted(listGroupMessage.size() - 1); // Thông báo cho adapter về sự thay đổi

                        // Cuộn xuống cuối RecyclerView
                        scrollToBottom();
                        changeData();
                    });
                } else {
                    // Xử lý trường hợp inputStream là null
                    Log.e("UploadDocumentToS3", "InputStream is null");
                }
            } catch (IOException e) {
                e.printStackTrace();
                // Xử lý lỗi IOException
                Log.e("UploadDocumentToS3", "Error uploading Document to S3: " + e.getMessage());
            } catch (AmazonServiceException e) {
                e.printStackTrace();
                // Xử lý lỗi AmazonServiceException
                Log.e("UploadDocumentToS3", "Error uploading Document to S3: " + e.getMessage());
            } catch (AmazonClientException e) {
                e.printStackTrace();
                // Xử lý lỗi AmazonClientException
                Log.e("UploadDocumentToS3", "Error uploading Document to S3: " + e.getMessage());
            }
        }).start();
    }

    private void saveMessageAndConversationToDB(String urlImage, String message, String type) {
        String currentTime = getCurrentDateTime();
        // Lưu tin nhắn và cuộc trò chuyện vào DynamoDB
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                dynamoDBManager.saveGroupMessage(groupID, urlImage, currentTime, userID, userAvatar, userName, type);
                dynamoDBManager.saveGroupConversation(groupID, message, groupName, currentTime,userAvatar, userName);
                return null;
            }
        }.execute();
    }

    private String generateFileName(String type) {
        // Lấy ngày giờ hiện tại
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        // Tạo dãy số random
        int randomNumber = new Random().nextInt(1000000);

        // Kết hợp ngày giờ và dãy số random để tạo tên file
        return type+"_" + timeStamp + "_" + randomNumber;
    }

    public void updateRecyclerView() {
        // Xóa bỏ các tin nhắn cũ từ listMessage
        listGroupMessage.clear();
        // Thêm các tin nhắn mới từ DynamoDB vào danh sách hiện tại
        List<GroupChat> newMessages = dynamoDBManager.loadGroupMessages(groupID);
        for (GroupChat message : newMessages) {
            // Tạo một đối tượng Message mới với thông tin từ tin nhắn và avatar
            GroupChat newMessage=new GroupChat(message.getAvatar(), message.getMessage(), message.getName(), message.getTime(), message.getUserID(), message.getType());
            Log.d("CheckCheck905", newMessage.toString());
            listGroupMessage.add(newMessage);
        }

        // Cập nhật RecyclerView
        adapter.notifyDataSetChanged();

        // Cuộn đến vị trí cuối cùng
        recyclerView.scrollToPosition(listGroupMessage.size() - 1);
    }
    private void initWebSocket() {
        Log.d("CheckGroupID", groupID);
        // Kiểm tra xem channel_id đã được thiết lập chưa
        if (groupID != null) {
            myWebSocket = new MyWebSocket("wss://free.blr2.piesocket.com/v3/"+groupID+"?api_key=ujXx32mn0joYXVcT2j7Gp18c0JcbKTy3G6DE9FMB&notify_self=0", this);
        } else {
            // Nếu channel_id vẫn chưa được thiết lập, hiển thị thông báo hoặc xử lý lỗi tương ứng
            Log.e("WebSocket", "Error: Channel ID is null");
        }
    }
    @Override
    public void onMessageReceived(String receivedMessage) {
        try {
            JSONObject jsonObject  = new JSONObject(receivedMessage);
            String jsonType = jsonObject.getString("type");
            if(jsonType.equals("send-group-message")){
                JSONObject messageJson = jsonObject.getJSONObject("message");
                String avatar = messageJson.getString("memberAvatar"); // Đường dẫn ảnh đại diện mặc định
                String userName = messageJson.getString("memberName");
                String userID = messageJson.getString("memberID");
                String message = messageJson.getString("message");
                String currentTime = messageJson.getString("time");
                String type=messageJson.getString("type");
                Log.d("onMessageReceived1", avatar+" "+userName+" "+message+" "+userID);

                // Kiểm tra xem tin nhắn nhận được có trùng với tin nhắn đã gửi không
                boolean isDuplicate = false;
                for (GroupChat groupChatItem : listGroupMessage) {
                    if (groupChatItem.getMessage().equals(message)) {
                        isDuplicate = true;
                        break;
                    }
                }

                scrollToBottom();
                if (!isDuplicate) {
                    listGroupMessage.add(new GroupChat(groupID, groupName, avatar, message, userName, currentTime, userID, type));
                    int newPosition = listGroupMessage.size() - 1; // Vị trí mới của tin nhắn
                    adapter.notifyItemInserted(newPosition);
                    scrollToBottom();
                    if (recyclerView.getLayoutManager() != null) {
                        // Cuộn xuống vị trí mới
                        recyclerView.post(() -> recyclerView.smoothScrollToPosition(newPosition));
                    } else {
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
            if(jsonType.equals("delete-group-message")){
                JSONObject messageJson = jsonObject.getJSONObject("message");

                // String avatar = messageJson.getString("avatar");
                String avatar = messageJson.getString("memberAvatar"); // Đường dẫn ảnh đại diện mặc định
                String userName = messageJson.getString("memberName");
                String userID = messageJson.getString("memberID");
                String message = messageJson.getString("message");
                String currentTime = messageJson.getString("time");
                String type=messageJson.getString("type");
                Log.d("onMessageReceived2", avatar+" "+userName+" "+message+" "+userID);
                GroupChat newMessage=new GroupChat(groupID, groupName, avatar, message, userName, currentTime, userID, type);
                int position = listGroupMessage.indexOf(newMessage);
                Log.d("onMessageReceived posotion: ",position+"");
                removeItemFromListMessage(position);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onConnectionStateChanged(boolean isConnected) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Ngắt kết nối khi Fragment bị hủy
        mainActivity.showBottomNavigation(true);
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

    ///++++
//******
    private void showOptionsDialog(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.message_option_fragment, null);
        builder.setView(dialogView);
        GroupChat item = ((GroupChatBoxAdapter) recyclerView.getAdapter()).getItem(position);
        Button btnRecall = dialogView.findViewById(R.id.btnRecall);
        Button btnForward = dialogView.findViewById(R.id.btnForward);
        Button btnButton = dialogView.findViewById(R.id.btnButton);
        if(!userID.equals(item.getUserID())){

            //btnRecall.setVisibility(View.VISIBLE);
            btnRecall.setEnabled(false);
            btnRecall.setAlpha(0.5f);
        }


        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        dynamoDBManager.GetTypeOfGroupMessage(groupID, item.getMessage(), item.getTime(), new DynamoDBManager.GetTypeOfMessageListener() {
            @Override
            public void onFound(String type) {
                thisType = type;
                if (type.equals("text")) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnButton.setText("Copy");
                        }
                    });
                } else if(type.equals("voice")){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnButton.setText("Download");
                        }
                    });
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnButton.setText("Download");
                        }
                    });
                }
            }
        });
        btnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (thisType == null) {
                    btnButton.setVisibility(View.GONE);
                } else {
                    switch (thisType) {
                        case "text":
                            Log.d("CheckingType", "Copy"+item.getMessage());
                            copyToClipboard(item.getMessage());
                            break;
                        case "image":
                            try {
                                key = getKey(item.getMessage());
                                Log.d("CheckingType", "Download image: "+key);
                                downloadImage(BUCKET_NAME, key);
                                changeData();
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "document":
                            try {
                                key = getKey(item.getMessage());
                                Log.d("CheckingType", "Download video: "+key);
                                downloadDocument(BUCKET_NAME_FOR_DOCUMENT, key);
                                changeData();
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "video":
                            try {
                                key = getKey(item.getMessage());
                                Log.d("CheckingType", "Download video: "+key);
                                downloadVideo(BUCKET_NAME_FOR_VIDEO, key);
                                changeData();
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                            break;
                        case "voice":
                            try {
                                key = getKey(item.getMessage());
                                Log.d("CheckingType", "Download voice: "+key);
                                downloadAudio(BUCKET_NAME_FOR_VOICE, key);
                                changeData();
                            } catch (MalformedURLException e) {
                                throw new RuntimeException(e);
                            }
                        default:
                            // Xử lý trường hợp không xác định
                            break;
                    }
                }
            }
        });
        btnRecall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xử lý logic khi người dùng chọn Recall
                recallMessage(position);
                alertDialog.dismiss();
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Xử lý logic khi người dùng chọn Delete
                forwardMessage(position);
                alertDialog.dismiss();
            }
        });
    }
    private void recallMessage(int position) {
        GroupChat item = ((GroupChatBoxAdapter) recyclerView.getAdapter()).getItem(position);
        Log.d("recallMessage: ",item.getMessage() +"+"+item.getTime()+"+"+groupID);
        dynamoDBManager.recallMessageForGroup(groupID,item.getMessage(),item.getTime());
        listGroupMessage.remove(position);
        String currentTime = getCurrentDateTime();
        dynamoDBManager.saveGroupConversation(groupID, "message has been recalled", groupName, currentTime,userAvatar, userName);

        JSONObject messageToSend = new JSONObject();
        // Tạo đối tượng JSON chứa trường type và message
        JSONObject json = new JSONObject();
        try{

            messageToSend.put("memberID", item.getUserID());
            messageToSend.put("memberName", item.getName());
            messageToSend.put("memberAvatar", item.getAvatar());
            messageToSend.put("message", item.getMessage());
            messageToSend.put("time", item.getTime());
            messageToSend.put("type", item.getType());
            json.put("type", "delete-group-message");
            json.put("message", messageToSend);

        }catch (JSONException e) {
            throw new RuntimeException(e);
        }
        myWebSocket.sendMessage(String.valueOf(json));

        adapter.notifyDataSetChanged();

        changeData();


        Toast.makeText(getContext(), "Message recalled", Toast.LENGTH_SHORT).show();
    }

    private void forwardMessage(int position) {
        GroupChat item = ((GroupChatBoxAdapter) recyclerView.getAdapter()).getItem(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Chọn loại chat");
        builder.setMessage("Bạn muốn chuyển tiếp tin nhắn tới:");

        // Tạo các nút cho hộp thoại
        builder.setPositiveButton("Chat đơn", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle bundle=new Bundle();
                bundle.putString("forwardIDfriend",  "");
                bundle.putString("forwardType",  item.getType());
                bundle.putString("forwardMessage",  item.getMessage());
                bundle.putString("forwardMyName", userName);
                bundle.putString("forwardMyAvatar", userAvatar);
                mainActivity.goToChatBoxListFragment(bundle);
                Toast.makeText(getContext(), "Forward to Single Chat", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Chat nhóm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle bundle=new Bundle();
                bundle.putString("forwardID",  groupID);
                bundle.putString("forwardType",  item.getType());
                bundle.putString("forwardMessage",  item.getMessage());
                bundle.putString("forwardMyName", userName);
                bundle.putString("forwardMyAvatar", userAvatar);
                mainActivity.goToGroupChatBoxListFragment(bundle);
                Toast.makeText(getContext(), "Forward to Group Chat", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNeutralButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Hiển thị hộp thoại
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getContext(), "Text copied to clipboard", Toast.LENGTH_SHORT).show();
    }
    private String getKey(String mediaUrl) throws MalformedURLException {
        URL url = new URL(mediaUrl);
        // Lấy phần path của URL
        String key=null;
        String path = url.getPath();

        // Trích xuất key từ phần path (tên tệp)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            key = Paths.get(path).getFileName().toString();
        }
        return key;
    }
    private void downloadAudio(String bucketName, String key) {
        new AsyncTask<String, Void, File>() {
            @Override
            protected File doInBackground(String... params) {
                try {
                    S3Object s3Object = s3Client.getObject(new GetObjectRequest(params[0], params[1]));
                    InputStream inputStream = s3Object.getObjectContent();
                    File file = new File(getContext().getFilesDir(), "audio_" + System.currentTimeMillis() + ".mp3"); // Tạo tên tệp mới dựa trên thời gian hiện tại
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                    return file;
                } catch (Exception e) {
                    Log.e(TAG, "Error downloading audio", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(File file) {
                if (file != null) {
                    saveAudioToDownloads(file);
                } else {
                    Toast.makeText(getContext(), "Failed to download audio", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(bucketName, key);
    }

    private void saveAudioToDownloads(File file) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            File audioFile = new File(downloadsDir, file.getName());
            try (FileInputStream in = new FileInputStream(file);
                 FileOutputStream out = new FileOutputStream(audioFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) > 0) {
                    out.write(buffer, 0, length);
                }
            }
            Toast.makeText(getContext(), "Audio saved to Downloads", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error saving audio to Downloads", e);
            Toast.makeText(getContext(), "Failed to save audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void downloadImage(String bucketName, String key) {
        new AsyncTask<String, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(String... params) {
                try {
                    S3Object s3Object = s3Client.getObject(new GetObjectRequest(params[0], params[1]));
                    InputStream inputStream = s3Object.getObjectContent();
                    return BitmapFactory.decodeStream(inputStream);
                } catch (Exception e) {
                    Log.e(TAG, "Error downloading image", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    saveImageToDownloads(bitmap);
                } else {
                    Toast.makeText(getContext(), "Failed to download image", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(bucketName, key);
    }

    private void saveImageToDownloads(Bitmap bitmap) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs(); // Tạo thư mục nếu chưa tồn tại
            }
            String fileName = "image_" + System.currentTimeMillis() + ".jpg"; // Tạo tên tệp mới dựa trên thời gian hiện tại
            File file = new File(downloadsDir, fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Toast.makeText(getContext(), "Image saved: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving image", e);
            Toast.makeText(getContext(), "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }
    private void downloadDocument(String bucketName, String key) {
        new AsyncTask<String, Void, File>() {
            @Override
            protected File doInBackground(String... params) {
                try {
                    S3Object s3Object = s3Client.getObject(new GetObjectRequest(params[0], params[1]));
                    InputStream inputStream = s3Object.getObjectContent();
                    File file = new File(getContext().getFilesDir(), "document_" + System.currentTimeMillis() + ".pdf"); // Tạo tên tệp mới dựa trên thời gian hiện tại
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                    return file;
                } catch (Exception e) {
                    Log.e(TAG, "Error downloading document", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(File file) {
                if (file != null) {
                    saveDocumentToDownloads(file);
                    Toast.makeText(getContext(), "Document downloaded", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to download document", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(bucketName, key);
    }
    private void saveDocumentToDownloads(File documentFile) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs(); // Tạo thư mục nếu chưa tồn tại
            }
            String fileName="" ;
            if(key.endsWith("pdf")){
                fileName = "document_" + System.currentTimeMillis() + ".pdf";
            }
            else if(key.endsWith("docx")){
                fileName = "document_" + System.currentTimeMillis() + ".docx";

            }
            else if(key.endsWith("txt")){
                fileName = "document_" + System.currentTimeMillis() + ".txt";

            }
            // String fileName = "document_" + System.currentTimeMillis() + ".pdf"; // Tạo tên tệp mới dựa trên thời gian hiện tại
            File file = new File(downloadsDir, fileName);

            // Copy tài liệu vào thư mục Downloads
            try (InputStream inStream = new FileInputStream(documentFile);
                 OutputStream outStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }
            }

            Toast.makeText(getContext(), "Document saved: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error saving document", e);
            Toast.makeText(getContext(), "Error saving document", Toast.LENGTH_SHORT).show();
        }
    }
    private void downloadVideo(String bucketName, String key) {
        new AsyncTask<String, Void, File>() {
            @Override
            protected File doInBackground(String... params) {
                try {
                    S3Object s3Object = s3Client.getObject(new GetObjectRequest(params[0], params[1]));
                    InputStream inputStream = s3Object.getObjectContent();
                    File file = new File(getContext().getFilesDir(), "video_" + System.currentTimeMillis() + ".mp4"); // Tạo tên tệp mới dựa trên thời gian hiện tại
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                    return file;
                } catch (Exception e) {
                    Log.e(TAG, "Error downloading video", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(File file) {
                if (file != null) {
                    saveVideoToDownloads(file);
                } else {
                    Toast.makeText(getContext(), "Failed to download video", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(bucketName, key);
    }

    private void saveVideoToDownloads(File videoFile) {
        try {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs(); // Tạo thư mục nếu chưa tồn tại
            }
            String fileName = "video_" + System.currentTimeMillis() + ".mp4"; // Tạo tên tệp mới dựa trên thời gian hiện tại
            File file = new File(downloadsDir, fileName);

            // Copy video vào thư mục Downloads
            try (InputStream inStream = new FileInputStream(videoFile);
                 OutputStream outStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }
            }

            Toast.makeText(getContext(), "Video saved: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error saving video", e);
            Toast.makeText(getContext(), "Error saving video", Toast.LENGTH_SHORT).show();
        }
    }

    Handler handler = new Handler(Looper.getMainLooper());
    private void removeItemFromListMessage(int position) {
        Log.d("removeItemFromListMessage", "Removing item at position: " + position);

        GroupChat item = ((GroupChatBoxAdapter) recyclerView.getAdapter()).getItem(position);

        if (item != null) {
            Log.d("removeItemFromListMessage", "Found item: " + item.getMessage());

            listGroupMessage.remove(item);

            // Gửi một tin nhắn tới Handler của luồng giao diện chính để thực hiện cập nhật giao diện
            handler.post(() -> {
                adapter.notifyDataSetChanged();
                Log.d("removeItemFromListMessage", "Item removed and adapter notified");
            });
        } else {
            Log.d("removeItemFromListMessage", "Item not found at position: " + position);
        }
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
    public void updateLastSeenAndRead()
    {
        dynamoDBManager.updatelastReadAndUnread(groupID, listMember);
    }


}
