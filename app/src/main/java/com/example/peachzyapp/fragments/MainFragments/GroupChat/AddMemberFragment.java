package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.example.peachzyapp.adapters.AddMemeberAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatHistoryFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AddMemberFragment extends Fragment {
    public static final String TAG= ChatHistoryFragment.class.getName();
    private Button btnAddMember;
    private Button btnCancel;
    private ImageButton btnFindFriend;
    private EditText etNameOrEmail;
    private View view;
    private MainActivity mainActivity;
    private ArrayList<FriendItem> friendList;
    private DynamoDBManager dynamoDBManager;
    private AddMemeberAdapter addMemeberAdapter;
    private RecyclerView rcvAddMember;
    private String uid;
    private String groupID;
    private FriendItem friendItem;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.add_member_fragment, container, false);
        friendList = new ArrayList<>();
        mainActivity= (MainActivity) getActivity();
        dynamoDBManager = new DynamoDBManager(getActivity());
        btnAddMember=view.findViewById(R.id.btnAddMember);
        btnFindFriend=view.findViewById(R.id.btnFindFriend);
        btnCancel=view.findViewById(R.id.btnCancel);
        etNameOrEmail=view.findViewById(R.id.etNameOrEmail);
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


        rcvAddMember.setAdapter(addMemeberAdapter);
//        RecyclerView.ItemDecoration itemDecoration=new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
//        rcvAddMember.addItemDecoration(itemDecoration);
        btnAddMember.setOnClickListener(v->{
            Log.d("CheckAddMember", "userID "+uid+" groupID: "+groupID);
            List<String> selectedMemberIds = addMemeberAdapter.getSelectedMemberIds();
            Log.d("CheckFriendIDFor",selectedMemberIds.toString());
            for (String memberId : selectedMemberIds) {
                CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
                    dynamoDBManager.updateGroupForAccount(memberId, groupID, "member");
                });

                CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
                    dynamoDBManager.updateGroup(groupID, memberId);
                });

                // Kết hợp hai CompletableFuture lại với nhau
                CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(future1, future2);

                // Chờ đợi tất cả các tác vụ hoàn thành trước khi tiếp tục
                combinedFuture.join();
            }
            getActivity().getSupportFragmentManager().popBackStack();
        });
        btnFindFriend.setOnClickListener(v->{
            String infor = etNameOrEmail.getText().toString().trim();
             Log.d("Information", infor);
            dynamoDBManager.findFriendByInfor(infor, uid,new DynamoDBManager.FriendFoundListener() {
                @Override
                public void onFriendFound(String id, String name, String avatar) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            friendItem = new FriendItem(id, avatar, name);
                            friendList.clear();
                            friendList.add(friendItem);

                            addMemeberAdapter.notifyDataSetChanged();
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
        btnCancel.setOnClickListener(v->{
            getActivity().getSupportFragmentManager().popBackStack();
        });


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

        List<String> memberList = new ArrayList<>();
        dynamoDBManager.findMemberOfGroup(groupID, new DynamoDBManager.ListMemberListener() {
            @Override
            public void ListMemberID(String id) {
                memberList.add(id);

            }
        });

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
//                        //Log.d("onDATAREquest", "run: "+data.getName());
//                        friendList.add(friendItem);
//                        addMemeberAdapter.notifyDataSetChanged();

                        if (!memberList.contains(friendItem.getId())) {
                            friendList.add(friendItem);
                            addMemeberAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

        });



    }


}
