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
import com.example.peachzyapp.SocketIO.MySocketManager;
import com.example.peachzyapp.adapters.MyAdapter;
import com.example.peachzyapp.entities.Item;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatHistoryFragment;

import java.util.ArrayList;
import java.util.List;


public class ChatBoxFragment extends Fragment {
    Button btnSend;
    EditText etMessage;
    private List<Item> listMessage = new ArrayList<>();
    private MyAdapter adapter;
    public static final String TAG= ChatBoxFragment.class.getName();
    private MySocketManager socketManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_chat_box, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycleview);
        btnSend = view.findViewById(R.id.btnSend);
        etMessage = view.findViewById(R.id.etMessage);

        // Initialize the adapter only once
        adapter = new MyAdapter(getContext(), listMessage);
        recyclerView.setAdapter(adapter);
        // Initialize and connect Socket.IO manager
        socketManager = new MySocketManager();
        socketManager.connect();

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
                    socketManager.sendMessage(message);
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
    public void onDestroyView() {
        super.onDestroyView();
        // Disconnect from Socket.IO when fragment is destroyed
        socketManager.disconnect();
    }
}
