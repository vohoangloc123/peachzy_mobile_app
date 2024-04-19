package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.peachzyapp.LiveData.MyGroupViewModel;
import com.example.peachzyapp.LiveData.MyViewModel;
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
    private ImageButton btnOpenCreateGroup;
    private DynamoDBManager dynamoDBManager;
    private ArrayList<GroupConversation> groupConversationList;
    private String uid;
    private MyGroupViewModel viewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listGroupChats = new ArrayList<>();
        view = inflater.inflate(R.layout.fragment_group_chat_list, container, false);
        dynamoDBManager = new DynamoDBManager(getActivity());
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

        viewModel = new ViewModelProvider(requireActivity()).get(MyGroupViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String newData) {
                Log.d("LivedataGroup", "onChanged: Yes");
                // Cập nhật RecyclerView hoặc bất kỳ thành phần UI nào khác ở đây
                // newData chứa dữ liệu mới từ Fragment con
                resetRecycleView();
            }
        });

        dynamoDBManager.loadGroupList(uid, new DynamoDBManager.LoadGroupListListener() {
            @Override
            public void onGroupListFound(String id, String groupName, String avatar, String message, String name, String time) {
                GroupChat groupChat = new GroupChat(id,  groupName,  avatar,  message,  name,  time);
                listGroupChats.add(groupChat);
                groupChatListAdapter.notifyDataSetChanged();
            }
        });
        groupChatListAdapter= new GroupChatListAdapter(listGroupChats);

        rcvGroupChatList.setAdapter(groupChatListAdapter);

        groupChatListAdapter.setOnItemClickListener(new GroupChatListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String id, String groupName, String avatar) {
                Bundle bundle = new Bundle();
                bundle.putString("groupID", id);
                bundle.putString("groupName",groupName);
                bundle.putString("groupAvatar", avatar);
                mainActivity.goToGroupChat(bundle);
            }
        });



        return view;
    }

//    private void resetRecycleView(){
//        Log.d("LivedataGroup1", "ok"+uid);
//        listGroupChats.clear();
//        dynamoDBManager.loadGroupList(uid, new DynamoDBManager.LoadGroupListListener() {
//            @Override
//            public void onGroupListFound(String id, String groupName, String avatar, String message, String name, String time) {
//                GroupChat groupChat = new GroupChat(id,  groupName,  avatar,  message,  name,  time);
//                listGroupChats.add(groupChat);
//                groupChatListAdapter.notifyDataSetChanged();
//
//            }
//        });
//    }
private void resetRecycleView() {
    new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {

            listGroupChats.clear();

            dynamoDBManager.loadGroupList(uid, new DynamoDBManager.LoadGroupListListener() {
                @Override
                public void onGroupListFound(String id, String groupName, String avatar, String message, String name, String time) {
                    GroupChat groupChat = new GroupChat(id, groupName, avatar, message, name, time);
                    listGroupChats.add(groupChat);
                    groupChatListAdapter.notifyDataSetChanged();

                }
            });
            groupChatListAdapter.notifyDataSetChanged();
            Log.d("LivedataGroup1", "ok" + uid);
        }
    }, 400); // 0.2 giây (200 mili giây)
}


}