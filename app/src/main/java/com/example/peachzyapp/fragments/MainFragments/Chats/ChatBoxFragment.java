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
    Button btnSend;
    EditText etMessage;
    private List<Item> listMessage = new ArrayList<>();
    private MyAdapter adapter;
    public static final String TAG= ChatBoxFragment.class.getName();
    MyWebSocket myWebSocket;
    RecyclerView recyclerView;
    int newPosition;
    String uid;
    String friendId;
    DynamoDBManager dynamoDBManager;
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

        myWebSocket = new MyWebSocket("wss://s12275.nyc1.piesocket.com/v3/1?api_key=CIL9dbE6489dDCZhDUngwMm43Btfp4J9bdnxEK4m&notify_self=1", this);
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
        //xử lý resize giao diện và đẩy edit text và button lên khi chat ngoài ra còn load tin nhắn mói từ dưới lên
        InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        //nhận uid của bản thân và id của người bạn
        //bản thân
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);
        //người bạn
//        friendId = getArguments().getString("friend_id");
        friendId = "ur gon";
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
                    listMessage.add(new Item(currentTime, message, true));
                    adapter.notifyItemInserted(listMessage.size() - 1);
                    recyclerView.scrollToPosition(listMessage.size() - 1);
                    myWebSocket.sendMessage(message);
                    dynamoDBManager.saveMessage(uid+friendId, message,currentTime, true);
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
//    @Override
//    public void onMessageReceived(String message) {
//        Log.d("MessageReceived", message);
//        String currentTime = Utils.getCurrentTime();
//        listMessage.add(new Item(currentTime, message));
//        adapter.notifyItemInserted(listMessage.size() - 1);
//        recyclerView.scrollToPosition(listMessage.size() - 1);
//    }
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
        listMessage.add(new Item(currentTime, message, false));
        dynamoDBManager.saveMessage(uid + friendId, message, currentTime, false);
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


}
