package com.example.peachzyapp.fragments.MainFragments.Chats;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.peachzyapp.adapters.ConversationAdapter;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.adapters.FriendAlreadyAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.Conversation;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;

public class ChatListsFragment extends Fragment {

    private MainActivity mainActivity;
    private RecyclerView rcvChatList;
    private View view;
    private ArrayList<Conversation> conversationsList;
    private DynamoDBManager dynamoDBManager;
    private String uid;
    private ConversationAdapter conversationAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat_lists, container, false);
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
                Conversation conversation = new Conversation(conversationID, friendID, message, time, avatar, name);
                conversationsList.add(conversation);
                Log.d("ConversationListSize", "Size: " + conversationsList.size());

                Log.d("ConversationFound", "Conversation ID: " + conversationID + ", Message: " + message + ", Time: " + time + ", Avatar: " + avatar + ", Name: " + name+", FriendI: "+friendID);
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
//                bundle.putString("friendName");
                Log.d("urlAvatarhere", urlAvatar);
                mainActivity.goToChatBoxFragment(bundle);
            }
        });
        return view;
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
