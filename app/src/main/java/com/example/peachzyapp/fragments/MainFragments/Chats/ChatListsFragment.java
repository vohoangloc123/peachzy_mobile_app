package com.example.peachzyapp.fragments.MainFragments.Chats;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.peachzyapp.LiveData.MyViewChatModel;
import com.example.peachzyapp.adapters.ConversationAdapter;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.Conversation;

import java.util.ArrayList;

public class ChatListsFragment extends Fragment {

    private MainActivity mainActivity;
    private RecyclerView rcvChatList;
    private View view;
    private ArrayList<Conversation> conversationsList;
    private DynamoDBManager dynamoDBManager;
    private String uid;
    private ConversationAdapter conversationAdapter;
    private MyViewChatModel viewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        dynamoDBManager = new DynamoDBManager(getActivity());
        rcvChatList = view.findViewById(R.id.rcvConversation);
        rcvChatList.setLayoutManager(new LinearLayoutManager(mainActivity));
        // Initialize conversationsList before calling loadConversations()
        conversationsList = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversationsList);
        // Set adapter to RecyclerView
        rcvChatList.setAdapter(conversationAdapter);
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);
        if (uid != null) {
            Log.d("FriendcheckUIDInChatListFragment", uid);
            // Sử dụng "uid" ở đây cho các mục đích của bạn
        } else {
            Log.e("FriendcheckUID", "UID is null");
        }
        dynamoDBManager.loadConversation1(uid, new DynamoDBManager.LoadConversationListener() {
            @Override
            public void onConversationFound(String conversationID, String friendID ,String message, String time, String avatar, String name) {
                Conversation conversation = new Conversation(conversationID, friendID ,message, time, avatar, name);
                conversationsList.add(conversation);
                Log.d("ConversationListSize", "Size: " + conversationsList.size());

                Log.d("ConversationFound", "Conversation ID: " + conversationID + ", Message: " + message + ", Time: " + time + ", Avatar: " + avatar + ", Name: " + name);
                // Notify adapter that data set has changed after all conversations are added
                conversationAdapter.notifyDataSetChanged();
            }
            @Override
            public void onLoadConversationError(Exception e) {
                e.printStackTrace();
            }
        });
        conversationAdapter.setOnItemClickListener(new ConversationAdapter.OnItemClickListener(){

            @Override
            public void onItemClick(String id, String urlAvatar, String friendName) {
                Bundle bundle = new Bundle();
                bundle.putString("friend_id", id);
                bundle.putString("urlAvatar",urlAvatar);
                bundle.putString("friendName", friendName);
                Log.d("urlAvatarhere", urlAvatar);
                mainActivity.goToChatBoxFragment(bundle);
            }
        });
        //Live data
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewChatModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String newData) {
                Log.d("Livedata1", "onChanged: Yes");
                // Cập nhật RecyclerView hoặc bất kỳ thành phần UI nào khác ở đây
                // newData chứa dữ liệu mới từ Fragment con
               //conversationsList.clear();
                resetRecycleView();
                ///
            }
        });

        return view;
    }

    private void resetRecycleView() {
        conversationsList.clear();
        Log.d("batdauchay", "yes: ");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dynamoDBManager.loadConversation1(uid, new DynamoDBManager.LoadConversationListener() {
                    @Override
                    public void onConversationFound(String conversationID, String friendID,String message, String time, String avatar, String name) {
                        Conversation conversation = new Conversation(conversationID, friendID, message, time, avatar, name);
                        conversationsList.add(conversation);
                        conversationAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onLoadConversationError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }, 1000); // 2000 milliseconds = 2 seconds
    }
    // Function to load conversations
//    private void loadConversations() {
//        dynamoDBManager.loadConversation1(uid, new DynamoDBManager.LoadConversationListener() {
//            @Override
//            public void onConversationFound(String conversationID, String message, String time, String avatar, String name) {
//                Conversation conversation = new Conversation(conversationID, message, time, avatar, name);
//                conversationsList.add(conversation);
//                Log.d("ConversationListSize", "Size: " + conversationsList.size());
//
//                Log.d("ConversationFound", "Conversation ID: " + conversationID + ", Message: " + message + ", Time: " + time + ", Avatar: " + avatar + ", Name: " + name);
//                // Notify adapter that data set has changed after all conversations are added
//                conversationAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onLoadConversationError(Exception e) {
//                e.printStackTrace();
//            }
//        });
//    }
}
