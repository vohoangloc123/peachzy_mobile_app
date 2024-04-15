package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.content.Context;
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
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.entities.GroupChat;

import java.util.ArrayList;
import java.util.List;


public class GroupChatListFragment extends Fragment {
    RecyclerView rcvGroupChatList;
    private ArrayList<GroupChat> listGroupChats;
    private View view;
    private MainActivity mainActivity;
    private GroupChatListAdapter groupChatListAdapter;
    Button btnOpenCreateGroup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listGroupChats = new ArrayList<>();
        view = inflater.inflate(R.layout.fragment_group_chat_list, container, false);

        mainActivity= (MainActivity) getActivity();
        rcvGroupChatList = view.findViewById(R.id.rcvGroupChatList);

        btnOpenCreateGroup=view.findViewById(R.id.btnOpenCreateGroup);
        btnOpenCreateGroup.setOnClickListener(v->{
          //  mainActivity.goToDetailFragmentAddFriend();
            mainActivity.goToCreateGroupChat();
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvGroupChatList.setLayoutManager(linearLayoutManager);


        //groupChatListAdapter= new GroupChatListAdapter(listGroupChats);
        groupChatListAdapter= new GroupChatListAdapter(getList());

        rcvGroupChatList.setAdapter(groupChatListAdapter);

        groupChatListAdapter.setOnItemClickListener(new GroupChatListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick() {
                mainActivity.goToGroupChat();
            }
        });

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