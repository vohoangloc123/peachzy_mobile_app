package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.adapters.CreateGroupChatAdapter;
import com.example.peachzyapp.adapters.RequestSentAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatHistoryFragment;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupChatFragment extends Fragment {
    public static final String TAG= ChatHistoryFragment.class.getName();
    private View view;
    private MainActivity mainActivity;
    private ArrayList<FriendItem> friendList;
    RecyclerView rcvFriendListForGroup;
    CreateGroupChatAdapter createGroupChatAdapter;
    private DynamoDBManager dynamoDBManager;
    String uid;
    ImageButton btnFindFriend;
    EditText etInforFriend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        friendList = new ArrayList<>();
        view = inflater.inflate(R.layout.activity_create_group_fragments, container, false);
        dynamoDBManager = new DynamoDBManager(getActivity());
        mainActivity= (MainActivity) getActivity();

        Bundle bundleReceive=getArguments();
        uid = bundleReceive.getString("uid");
        Log.d("CheckUIDhere", uid);




        rcvFriendListForGroup = view.findViewById(R.id.rcvFriendListForGroup);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvFriendListForGroup.setLayoutManager(linearLayoutManager);

        dynamoDBManager.getIDFriend(uid,"1", new DynamoDBManager.AlreadyFriendListener() {
            @Override
            public void onFriendAlreadyFound(FriendItem data) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //Log.d("onDATAREquest", "run: "+data.getName());
                        friendList.add(data);
                        createGroupChatAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFriendAcceptRequestFound(String id, String name, String avatar) {

            }
        });

        etInforFriend= view.findViewById(R.id.etInforFriend);
        btnFindFriend = view.findViewById(R.id.btnFindFriend);
        btnFindFriend.setOnClickListener(v->{
            String infor = etInforFriend.getText().toString().trim();
           // Log.d("Information", infor);
            dynamoDBManager.findFriendByInfor(infor, uid,new DynamoDBManager.FriendFoundListener() {
                @Override
                public void onFriendFound(String id, String name, String avatar) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FriendItem friendItem = new FriendItem(id, avatar, name);
                            friendList.clear();
                            friendList.add(friendItem);

                            createGroupChatAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivity(), "Friend found!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFriendNotFound() {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Friend not found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("Error", "Exception occurred: ", e);
                            Toast.makeText(getActivity(), "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        });

        createGroupChatAdapter= new CreateGroupChatAdapter(friendList);
        //createGroupChatAdapter= new CreateGroupChatAdapter(getListFriends());

        rcvFriendListForGroup.setAdapter(createGroupChatAdapter);
        RecyclerView.ItemDecoration itemDecoration=new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
        rcvFriendListForGroup.addItemDecoration(itemDecoration);

        return view;
    }

    private List<FriendItem> getListFriends() {
        List<FriendItem> list= new ArrayList<>();
        for(int i=1; i<20;i++){
            list.add(new FriendItem("name"+i));
        }
        return list;
    }
}
