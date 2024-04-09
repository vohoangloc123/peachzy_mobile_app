package com.example.peachzyapp.fragments.MainFragments.Chats;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.Toast;

import com.example.peachzyapp.Other.Utils;
import com.example.peachzyapp.R;
import com.example.peachzyapp.SocketIO.MyWebSocket;
import com.example.peachzyapp.adapters.MyAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.Item;

import java.util.ArrayList;
import java.util.List;


public class ChatBoxFragment extends Fragment implements MyWebSocket.WebSocketListener {
    private String test;
    ImageButton btnSend;
    EditText etMessage;
    private List<Item> listMessage = new ArrayList<>();
    private MyAdapter adapter;
    public static final String TAG= ChatHistoryFragment.class.getName();
    MyWebSocket myWebSocket;
    RecyclerView recyclerView;
    int newPosition;
    String avatar;

    //// new
    String uid;
    String friend_id;
    View view;
    private String channel_id = null;
    DynamoDBManager dynamoDBManager;
    private String urlAvatar;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_chat_box, container, false);
        recyclerView = view.findViewById(R.id.recycleview);
        btnSend = view.findViewById(R.id.btnSend);
        etMessage = view.findViewById(R.id.etMessage);

        // Initialize the adapter only once
        adapter = new MyAdapter(getContext(), listMessage);
        recyclerView.setAdapter(adapter);
        // Initialize and connect Socket.IO manager
        // initialize dynamoDB
        dynamoDBManager=new DynamoDBManager(getContext());
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
        urlAvatar= bundleReceive.getString("avatarUrl");
        Log.d("RequestUIDfriend", "onCreateView: "+friend_id);
        avatar="https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/avatar_20240409_151015_1719.jpg.jpg";

        Log.d("CheckAvatarReceived", avatar);
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
                    scrollToBottom();
                    String currentTime = Utils.getCurrentTime();
                    listMessage.add(new Item(currentTime, message,urlAvatar ,true));
                    adapter.notifyItemInserted(listMessage.size() - 1);
                    recyclerView.scrollToPosition(listMessage.size() - 1);
                    myWebSocket.sendMessage(message);
                    dynamoDBManager.saveMessage(uid+friend_id, message,currentTime, true);
                    scrollToBottom();
                } else {
                    Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                }
                // Clear the message input field after sending
                etMessage.getText().clear();
            }
        });


        return view;
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
public void onMessageReceived(String message) {
    Log.d("MessageReceived", message);

    // Kiểm tra xem tin nhắn nhận được có trùng với tin nhắn đã gửi không
    boolean isDuplicate = false;
    for (Item item : listMessage) {
        if (item.getMessage().equals(message)) {
            isDuplicate = true;
            break;
        }
    }
    scrollToBottom();
    if (!isDuplicate) {
        // Tin nhắn không trùng, thêm nó vào danh sách và cập nhật giao diện
        String currentTime = Utils.getCurrentTime();
        listMessage.add(new Item(currentTime, message, urlAvatar,false));
        Log.d("CheckingListMessage", listMessage.toString());
        //saveMessage(message,currentTime);
        dynamoDBManager.saveMessage(uid+friend_id, message, currentTime, false);
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


}
