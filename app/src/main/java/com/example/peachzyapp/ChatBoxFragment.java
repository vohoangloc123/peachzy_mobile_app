package com.example.peachzyapp;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.peachzyapp.Other.Utils;
import com.example.peachzyapp.SocketIO.MyWebSocket;
import com.example.peachzyapp.adapters.MyAdapter;
import com.example.peachzyapp.entities.Item;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatHistoryFragment;

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

        // Set up RecyclerView layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    // Add the new message to the list and notify adapter
                    String currentTime = Utils.getCurrentTime();
                    listMessage.add(new Item(currentTime, message));
                    adapter.notifyItemInserted(listMessage.size() - 1);
                    recyclerView.scrollToPosition(listMessage.size() - 1);
                    myWebSocket.sendMessage(message);
                } else {
                    Toast.makeText(getContext(), "Please enter a message", Toast.LENGTH_SHORT).show();
                }
                // Clear the message input field after sending
                etMessage.getText().clear();
            }
        });


        return view;
    }
    @Override
    public void onMessageReceived(String message) {
        Log.d("MessageReceived", message);
        String currentTime = Utils.getCurrentTime();
        listMessage.add(new Item(currentTime, message));
        adapter.notifyItemInserted(listMessage.size() - 1);
        recyclerView.scrollToPosition(listMessage.size() - 1);
    }
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        // Ngắt kết nối khi Fragment bị hủy
//        myWebSocket.closeWebSocket();
//    }


}
