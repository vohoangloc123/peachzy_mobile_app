package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.adapters.AddMemeberAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatHistoryFragment;

import java.util.ArrayList;
import java.util.List;

public class AddMemberFragment extends Fragment {
    public static final String TAG= ChatHistoryFragment.class.getName();
    private View view;
    private MainActivity mainActivity;
    private ArrayList<FriendItem> friendList;
    private DynamoDBManager dynamoDBManager;
    AddMemeberAdapter addMemeberAdapter;
    RecyclerView rcvAddMember;
    String uid;
    String groupID;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.add_member_fragment, container, false);
        friendList = new ArrayList<>();
        mainActivity= (MainActivity) getActivity();
        dynamoDBManager = new DynamoDBManager(getActivity());


        //truyen id
        Bundle bundleReceive=getArguments();
        uid = bundleReceive.getString("uid");
        Log.d("CheckUIDhereAdd", uid);
        groupID = bundleReceive.getString("groupID");
        Log.d("CheckGroupID", "onCreateView: "+groupID);


        rcvAddMember = view.findViewById(R.id.rcvAddMember);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvAddMember.setLayoutManager(linearLayoutManager);

        //addMemeberAdapter = new AddMemeberAdapter(getListFriends());
        loadFriends();
        addMemeberAdapter = new AddMemeberAdapter(friendList);


        //dynamoDBManager.findMemberOfGroup(groupID);
        dynamoDBManager.findMemberToAddOfGroup(uid,groupID);

        rcvAddMember.setAdapter(addMemeberAdapter);
        RecyclerView.ItemDecoration itemDecoration=new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
        rcvAddMember.addItemDecoration(itemDecoration);

        return view;
    }

    private List<FriendItem> getListFriends() {
        List<FriendItem> list= new ArrayList<>();
        for(int i=1; i<10;i++){
            list.add(new FriendItem("name"+i));
        }
        return list;
    }
    private void loadFriends()
    {
        friendList.clear();
        dynamoDBManager.getIDFriend(uid,"1", new DynamoDBManager.AlreadyFriendListener() {
            @Override
            public void onFriendAlreadyFound(FriendItem data) {

            }

            @Override
            public void onFriendAcceptRequestFound(String id, String name, String avatar) {

            }

            @Override
            public void onFriendCreateGroupFound(FriendItem friendItem) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d("onDATAREquest", "run: "+data.getName());
                        friendList.add(friendItem);
                        addMemeberAdapter.notifyDataSetChanged();
                    }
                });
            }

        });
    }
}
