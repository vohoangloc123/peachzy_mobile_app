package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.adapters.FriendAlreadyAdapter;
import com.example.peachzyapp.adapters.GroupChatListAdapter;
import com.example.peachzyapp.adapters.RequestSentAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.Conversation;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.entities.GroupChat;
import com.example.peachzyapp.entities.GroupConversation;
import com.example.peachzyapp.fragments.MainFragments.Users.AddFriendFragment;

import java.util.ArrayList;
import java.util.List;


public class GroupChatListFragment extends Fragment {
    RecyclerView rcvGroupChatList;
    private ArrayList<GroupChat> listGroupChats;
    private View view;
    private MainActivity mainActivity;
    private GroupChatListAdapter groupChatListAdapter;
    Button btnOpenCreateGroup;
    private DynamoDBManager dynamoDBManager;
    private ArrayList<GroupConversation> groupConversationList;
    String uid;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listGroupChats = new ArrayList<>();
        view = inflater.inflate(R.layout.fragment_group_chat_list, container, false);

        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);
        if (uid != null) {
            Log.d("FriendcheckUID", uid);
            // Sử dụng "uid" ở đây cho các mục đích của bạn
        } else {
            Log.e("FriendcheckUID", "UID is null");
        }
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        AddFriendFragment fragment = new AddFriendFragment();
        fragment.setArguments(bundle);
        Log.d("checkIDUser", uid);

        mainActivity= (MainActivity) getActivity();
        rcvGroupChatList = view.findViewById(R.id.rcvGroupChatList);

        btnOpenCreateGroup=view.findViewById(R.id.btnOpenCreateGroup);
        btnOpenCreateGroup.setOnClickListener(v->{
          //  mainActivity.goToDetailFragmentAddFriend();
            mainActivity.goToCreateGroupChat();
        });
        DynamoDBManager dynamoDBManager=new DynamoDBManager(getContext());
        groupConversationList=new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvGroupChatList.setLayoutManager(linearLayoutManager);


        dynamoDBManager.loadGroupList(uid, new DynamoDBManager.LoadGroupListListener() {
            @Override
            public void onGroupListFound(String id, String groupName, String avatar, String message, String name, String time) {
                GroupChat groupChat = new GroupChat(id,  groupName,  avatar,  message,  name,  time);
                listGroupChats.add(groupChat);
                groupChatListAdapter.notifyDataSetChanged();
            }
        });
        groupChatListAdapter= new GroupChatListAdapter(listGroupChats);
//        groupChatListAdapter= new GroupChatListAdapter(getList());
//        dynamoDBManager.loadGroupConversation("Tjye9dPTx4c8sERXBcbiBXDs1Cm16745", new DynamoDBManager.LoadGroupConversationListener(){
//
//            @Override
//            public void onGroupConversationFound(String conversationID, String groupName, String message, String time, String avatar, String name) {
//                GroupConversation groupConversation=new GroupConversation(conversationID, groupName, message, time, avatar, name);
//                groupConversationList.add(groupConversation);
//                Log.d("ConversationListSize", "Size: " +groupConversationList.size());
//
//                Log.d("ConversationFound", "Conversation ID: " + conversationID + ", Message: " + message + ", Time: " + time + ", Avatar: " + avatar + ", Name: " + name);
//                // Notify adapter that data set has changed after all conversations are added
//                groupChatListAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onLoadGroupConversationError(Exception e) {
//
//            }
//        });

        rcvGroupChatList.setAdapter(groupChatListAdapter);
//
//        groupChatListAdapter.setOnItemClickListener(new GroupChatListAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick() {
//                mainActivity.goToGroupChat();
//            }
//        });
//
        RecyclerView.ItemDecoration itemDecoration=new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
        rcvGroupChatList.addItemDecoration(itemDecoration);

        // Inflate the layout for this fragment
        return view;
    }

    private List<GroupChat> getList() {
        List<GroupChat> list= new ArrayList<>();
        for(int i=1; i<20;i++){
            list.add(new GroupChat("GroupChat "+i));
        }
        return list;
    }
}