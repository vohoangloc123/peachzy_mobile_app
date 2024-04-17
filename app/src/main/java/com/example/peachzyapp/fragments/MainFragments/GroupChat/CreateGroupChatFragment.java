package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.LiveData.MyGroupViewModel;
import com.example.peachzyapp.LiveData.MyViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.Other.Utils;
import com.example.peachzyapp.R;
import com.example.peachzyapp.adapters.CreateGroupChatAdapter;
import com.example.peachzyapp.adapters.RequestSentAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatHistoryFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CreateGroupChatFragment extends Fragment {
    public static final String TAG= ChatHistoryFragment.class.getName();
    private View view;
    private ImageButton btnFindFriend;
    private Button btnCreateGroup;
    private EditText etFindByNameOrEmail;
    private EditText etGroupName;
    private CheckBox cbAddToGroup;
    private MainActivity mainActivity;
    private ArrayList<FriendItem> friendList;
    RecyclerView rcvFriendListForGroup;
    CreateGroupChatAdapter createGroupChatAdapter;
    private DynamoDBManager dynamoDBManager;
    String uid;
    FriendItem friendItem;
    private MyGroupViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyGroupViewModel.class);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        friendList = new ArrayList<>();
        view = inflater.inflate(R.layout.activity_create_group_fragments, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MyGroupViewModel.class);
        etFindByNameOrEmail= view.findViewById(R.id.etNameOrEmail);
        etGroupName= view.findViewById(R.id.etGroupName);
        btnFindFriend = view.findViewById(R.id.btnFindFriend);
        cbAddToGroup = view.findViewById(R.id.cbAddToGroup);
        btnCreateGroup = view.findViewById(R.id.btnCreateGroup);
        dynamoDBManager = new DynamoDBManager(getActivity());
        mainActivity= (MainActivity) getActivity();
        //truyen id
        Bundle bundleReceive=getArguments();
        uid = bundleReceive.getString("uid");
        Log.d("CheckUIDhere", uid);

        rcvFriendListForGroup = view.findViewById(R.id.rcvFriendListForGroup);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvFriendListForGroup.setLayoutManager(linearLayoutManager);
        loadFriends();

        btnFindFriend.setOnClickListener(v->{
            String infor = etFindByNameOrEmail.getText().toString().trim();
           // Log.d("Information", infor);
            dynamoDBManager.findFriendByInfor(infor, uid,new DynamoDBManager.FriendFoundListener() {
                @Override
                public void onFriendFound(String id, String name, String avatar) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            friendItem = new FriendItem(id, avatar, name);
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
//        RecyclerView.ItemDecoration itemDecoration=new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
//        rcvFriendListForGroup.addItemDecoration(itemDecoration);

        btnCreateGroup.setOnClickListener(v->{
            String groupName=etGroupName.getText().toString().trim();
            String groupID=randomNumber()+"-"+uid;
            String currentTime = Utils.getCurrentTime();

            // Lấy danh sách ID đã chọn từ adapter
            List<String> selectedFriendIds = createGroupChatAdapter.getSelectedFriendIds();
            // Thực hiện các thao tác với danh sách ID đã chọn
            dynamoDBManager.updateGroupForAccount(uid, groupID);
            for (String friendId : selectedFriendIds) {
                dynamoDBManager.updateGroupForAccount(friendId, groupID);
            }
            List<String> memberIDs = new ArrayList<>();
            for (String memberID : selectedFriendIds) {
                memberIDs.add(memberID);
            }
            dynamoDBManager.createGroup(groupID, memberIDs);
            dynamoDBManager.saveGroupConversation(groupID, "Vừa tạo group", groupName,currentTime, "https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/avatar.jpg", "");
            changeData();
        });

        return view;
    }
    private String randomNumber()
    {
        int randomNumber = new Random().nextInt(10000);
        return String.valueOf(randomNumber);
    }
    private void loadFriends()
    {
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
                        createGroupChatAdapter.notifyDataSetChanged();
                    }
                });
            }

        });
    }
    private void changeData() {
        viewModel.setData("New data");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        viewModel.setData("Change");
        Log.d("Detach", "onDetach: ");
    }

}
