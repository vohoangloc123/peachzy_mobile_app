package com.example.peachzyapp.fragments.MainFragments.GroupChat;

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
import android.widget.ImageButton;
import com.example.peachzyapp.LiveData.MyGroupViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.SocketIO.MyWebSocket;
import com.example.peachzyapp.adapters.GroupChatListAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.GroupConversation;
import com.example.peachzyapp.fragments.MainFragments.Users.AddFriendFragment;
import com.example.peachzyapp.fragments.MainFragments.Users.RequestReceivedFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class GroupChatListFragment extends Fragment implements MyWebSocket.WebSocketListener{
    public static final String TAG = GroupChatListFragment.class.getName();
    private MyWebSocket myWebSocket;
    private RecyclerView rcvGroupChatList;
    private ArrayList<GroupConversation> listGroupChats;
    private View view;
    private MainActivity mainActivity;
    private GroupChatListAdapter groupChatListAdapter;
    private ImageButton btnOpenCreateGroup;
    private DynamoDBManager dynamoDBManager;
    private ArrayList<GroupConversation> groupConversationList;
    private String uid;
    private MyGroupViewModel viewModel;
    private SimpleDateFormat dateFormat;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyGroupViewModel.class);
    }

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
        });//
        listGroupChats.clear();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        dynamoDBManager.loadGroupList(uid, new DynamoDBManager.LoadGroupListListener() {
            @Override
            public void onGroupListFound(String id, String groupName, String avatar, String message, String name, String time) {
                try {
                    // Parse the time string into a Date object
                    Date messageTime = dateFormat.parse(time);

                    // Create the group conversation object
                    GroupConversation groupConversation = new GroupConversation(id, groupName, name, avatar, message, time);

                    // Add the group conversation to the list
                    listGroupChats.add(groupConversation);

                    // Sort the list based on the messageTime in descending order
                    Collections.sort(listGroupChats, new Comparator<GroupConversation>() {
                        @Override
                        public int compare(GroupConversation gc1, GroupConversation gc2) {
                            Date date1 = null;
                            Date date2 = null;
                            try {
                                date1 = dateFormat.parse(gc1.getTime());
                                date2 = dateFormat.parse(gc2.getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return date2.compareTo(date1); // Sort in descending order
                        }
                    });

                    // Notify the adapter of the dataset change
                    groupChatListAdapter.notifyDataSetChanged();
                } catch (ParseException e) {
                    // Handle parsing exceptions if any
                    e.printStackTrace();
                }
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
        initWebSocket(uid);
        return view;
    }

private void resetRecycleView() {
    new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {

            listGroupChats.clear();

            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            dynamoDBManager.loadGroupList(uid, new DynamoDBManager.LoadGroupListListener() {
                @Override
                public void onGroupListFound(String id, String groupName, String avatar, String message, String name, String time) {
                    try {
                        // Parse the time string into a Date object
                        Date messageTime = dateFormat.parse(time);

                        // Create the group conversation object
                        GroupConversation groupConversation = new GroupConversation(id, groupName, name, avatar, message, time);

                        // Add the group conversation to the list
                        listGroupChats.add(groupConversation);

                        // Sort the list based on the messageTime in descending order
                        Collections.sort(listGroupChats, new Comparator<GroupConversation>() {
                            @Override
                            public int compare(GroupConversation gc1, GroupConversation gc2) {
                                Date date1 = null;
                                Date date2 = null;
                                try {
                                    date1 = dateFormat.parse(gc1.getTime());
                                    date2 = dateFormat.parse(gc2.getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                return date2.compareTo(date1); // Sort in descending order
                            }
                        });

                        // Notify the adapter of the dataset change
                        groupChatListAdapter.notifyDataSetChanged();
                    } catch (ParseException e) {
                        // Handle parsing exceptions if any
                        e.printStackTrace();
                    }
                }
            });
            groupChatListAdapter.notifyDataSetChanged();
            Log.d("LivedataGroup1", "ok" + uid);
        }
    }, 400); // 0.2 giây (200 mili giây)
}


    @Override
    public void onMessageReceived(String receivedMessage) {
        Log.d("onMessageReceived2: ",receivedMessage);
//        viewModel.setData(message);
        try {

            JSONObject jsonObject  = new JSONObject(receivedMessage);
            String typeJson = jsonObject.getString("type");

            if(typeJson.equals("create-group")){
                JSONObject messageJson = jsonObject.getJSONObject("message");
                String groupID = messageJson.getString("_id");
                String avatar = messageJson.getString("avatar"); // Đường dẫn ảnh đại diện mặc định
                String groupName = messageJson.getString("groupName");
                String name = messageJson.getString("name");
                String time = messageJson.getString("time");
                String message=messageJson.getString("message");

                Log.d("onMessageReceived3: ",groupID+":"+groupName+":"+name+":"+message);


                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        changeData();
                    }
                });




            }





        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionStateChanged(boolean isConnected) {

    }
    private void initWebSocket( String channelId) {

        myWebSocket = new MyWebSocket("wss://free.blr2.piesocket.com/v3/"+channelId+"?api_key=ujXx32mn0joYXVcT2j7Gp18c0JcbKTy3G6DE9FMB&notify_self=0", this);

    }
    private void changeData() {
        viewModel.setData("New data");
        Log.d("changeData888: ","ok");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Ngắt kết nối khi Fragment bị hủy
        myWebSocket.closeWebSocket();
    }

}