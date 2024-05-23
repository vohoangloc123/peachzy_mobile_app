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
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.Other.Utils;
import com.example.peachzyapp.R;
import com.example.peachzyapp.adapters.ConversationAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.Conversation;

import java.util.ArrayList;

public class ChatListForForwardMessageFragment extends Fragment {
    public static final String TAG= ChatListForForwardMessageFragment.class.getName();
    private MainActivity mainActivity;
    private RecyclerView rcvChatList;
    private View view;
    private ArrayList<Conversation> conversationsList;
    private DynamoDBManager dynamoDBManager;
    private String uid;
    private ConversationAdapter conversationAdapter;
    private MyViewChatModel viewModel;
    private String forwardType, forwardMessage;
    private String forwardChannelID,forwardMyAvatar,forwardMyName;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat_list_for_forward_message, container, false);
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
        Bundle bundle = getArguments();
        if (bundle != null) {
            forwardType = bundle.getString("forwardType");
            forwardMessage=bundle.getString("forwardMessage");
            forwardMyAvatar = bundle.getString("forwardMyAvatar");
            forwardMyName=bundle.getString("forwardMyName");
            Log.d("ForwardData", "Type: " + forwardType + ", Message: ");
        }

        dynamoDBManager.loadConversation(uid, new DynamoDBManager.LoadConversationListener() {
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

                dynamoDBManager.getChannelID(uid, id, new DynamoDBManager.ChannelIDinterface() {
                    @Override
                    public void GetChannelId(String channelID) {
                        forwardChannelID=channelID;
                        Log.d("ForwardData", "Channel ID: " + forwardChannelID + ", Message: " + forwardMessage +
                                ", Type: " + forwardType + ", My Avatar: " + forwardMyAvatar +
                                ", Friend Name: " + urlAvatar + ", My Name: " + forwardMyName+", Channel ID: " + forwardChannelID);
                        String currentTime = Utils.getCurrentTime();
                        dynamoDBManager.saveMessageOneToOne(forwardChannelID,forwardMessage,currentTime,forwardType,uid,id);
                        if(forwardType.equals("text"))
                        {
                            dynamoDBManager.saveConversation(uid,  id, forwardMyName+": "+forwardMessage, currentTime, forwardMyAvatar, friendName);
                            dynamoDBManager.saveConversation(id, uid,forwardMyName+": "+forwardMessage, currentTime, urlAvatar, forwardMyName);
                        }else
                        {
                            dynamoDBManager.saveConversation(uid,  id, forwardMyName+": "+forwardType, currentTime, forwardMyAvatar, friendName);
                            dynamoDBManager.saveConversation(id, uid,forwardMyName+": "+forwardType, currentTime, urlAvatar, forwardMyName);
                        }
                    }
                });
                conversationsList.clear();
                    getParentFragmentManager().popBackStack();
                    mainActivity.showBottomNavigation(false);
            }
        });
        //Live data
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewChatModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String newData) {
                Log.d("Livedata1", "onChanged: Yes");
                resetRecycleView();
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
                dynamoDBManager.loadConversation(uid, new DynamoDBManager.LoadConversationListener() {
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
}