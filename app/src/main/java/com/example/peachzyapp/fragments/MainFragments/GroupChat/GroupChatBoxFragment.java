package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.Other.Utils;
import com.example.peachzyapp.R;
import com.example.peachzyapp.SocketIO.MyWebSocket;
import com.example.peachzyapp.adapters.GroupChatBoxAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.GroupChat;
import com.example.peachzyapp.entities.Item;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatHistoryFragment;

import java.util.ArrayList;
import java.util.List;

public class GroupChatBoxFragment extends Fragment  implements MyWebSocket.WebSocketListener {
    public static final String TAG= ChatHistoryFragment.class.getName();
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
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view=inflater.inflate(R.layout.fragment_group_chat_box, container, false);
        Bundle bundleReceive=getArguments();
        tvGroupName=view.findViewById(R.id.tvGroupName);
        recyclerView=view.findViewById(R.id.rcvGroupChat);
        btnSend = view.findViewById(R.id.btnGroupSend);
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
        //adapter
        adapter = new GroupChatBoxAdapter(getContext(), listGroupMessage, userID);
        recyclerView.setAdapter(adapter);
        //load messages
        updateRecyclerView();
        //web socket
        initWebSocket();
        dynamoDBManager.getProfileByUID(userID, new DynamoDBManager.FriendFoundForGetUIDByEmailListener() {
            @Override
            public void onFriendFound(String uid, String name, String email, String avatar, Boolean sex, String dateOfBirth) {
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
                    listGroupMessage.add(new GroupChat(groupID, groupName, userAvatar, message, userName, currentTime, userID));
                    adapter.notifyItemInserted(listGroupMessage.size() - 1);
                    recyclerView.scrollToPosition(listGroupMessage.size() - 1);
                    myWebSocket.sendMessage(message);
                    dynamoDBManager.saveGroupMessage(groupID, message, currentTime, userID, userAvatar, userName);
                    dynamoDBManager.saveGroupConversation(groupID, message, groupName, currentTime,userAvatar, userName);
                    scrollToBottom();
                } else {
                    Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                }
                // Clear the message input field after sending
                etGroupMessage.getText().clear();
            }
        });
       return view;
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
}
