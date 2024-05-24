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
import com.example.peachzyapp.Other.Utils;
import com.example.peachzyapp.R;
import com.example.peachzyapp.SocketIO.MyWebSocket;
import com.example.peachzyapp.adapters.GroupChatListAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.GroupConversation;
import com.example.peachzyapp.fragments.MainFragments.Users.AddFriendFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class GroupChatListForForwardMessageFragment extends Fragment  implements MyWebSocket.WebSocketListener {
    public static final String TAG = GroupChatListForForwardMessageFragment.class.getName();
    private MyWebSocket myWebSocket;
    private RecyclerView rcvGroupChatList;
    private ArrayList<GroupConversation> listGroupChats;
    private View view;
    private MainActivity mainActivity;
    private GroupChatListAdapter groupChatListAdapter;
    private DynamoDBManager dynamoDBManager;
    private ArrayList<GroupConversation> groupConversationList;
    private String uid;
    private MyGroupViewModel viewModel;
    private String forwardType, forwardMessage;
    private String forwardMyAvatar,forwardMyName;
    private ImageButton btnBack;
    private SimpleDateFormat dateFormat;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        listGroupChats = new ArrayList<>();
        view = inflater.inflate(R.layout.fragment_group_chat_list_for_forward_message, container, false);
        dynamoDBManager = new DynamoDBManager(getActivity());
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);
        if (uid != null) {
            Log.d("FriendcheckUID", uid);
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
        }
        mainActivity= (MainActivity) getActivity();
        rcvGroupChatList = view.findViewById(R.id.rcvGroupChatList);
        DynamoDBManager dynamoDBManager=new DynamoDBManager(getContext());
        groupConversationList=new ArrayList<>();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvGroupChatList.setLayoutManager(linearLayoutManager);

        viewModel = new ViewModelProvider(requireActivity()).get(MyGroupViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String newData) {
                Log.d("LivedataGroup", "onChanged: Yes");
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
                String currentTime = Utils.getCurrentTime();
                initWebSocket(id);
                sendMessageToSocket(uid, forwardMyName, forwardMyAvatar, forwardMessage, currentTime, forwardType);
                if(forwardType.equals("text"))
                {
                    dynamoDBManager.saveGroupMessage(id, forwardMessage, currentTime, uid, forwardMyAvatar, forwardMyName, forwardType);
                    dynamoDBManager.saveGroupConversation(id, forwardMessage, groupName, currentTime,forwardMyAvatar,forwardMyName);
                }else
                {
                    dynamoDBManager.saveGroupMessage(id, forwardMessage, currentTime, uid, forwardMyAvatar, forwardMyName, forwardType);
                    dynamoDBManager.saveGroupConversation(id, forwardType, groupName, currentTime,forwardMyAvatar,forwardMyName);
                }
                //live data
                changeData();
                listGroupChats.clear();
                getParentFragmentManager().popBackStack();
                mainActivity.showBottomNavigation(false);
            }
        });
        btnBack=view.findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
            mainActivity.showBottomNavigation(false);
        });
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
    private void sendMessageToSocket(String senderID, String userName, String senderAvatar, String message, String time, String type )
    {
        JSONObject messageToSend = new JSONObject();
        JSONObject json = new JSONObject();
        try{
            messageToSend.put("memberID", senderID);
            messageToSend.put("memberName", userName);
            messageToSend.put("memberAvatar", senderAvatar);
            messageToSend.put("message", message);
            messageToSend.put("time", time);
            messageToSend.put("type", type);
            json.put("type", "send-group-message");
            json.put("message", messageToSend);
        }catch (JSONException e) {
            throw new RuntimeException(e);
        }
        Log.d(TAG, "sending data"+String.valueOf(json));
        myWebSocket.sendMessage(String.valueOf(json));
    }
    private void initWebSocket(String groupID) {
        // Kiểm tra xem channel_id đã được thiết lập chưa
        if (groupID != null) {
            // Nếu đã có channel_id, thì khởi tạo myWebSocket
            myWebSocket = new MyWebSocket("wss://free.blr2.piesocket.com/v3/"+groupID+"?api_key=ujXx32mn0joYXVcT2j7Gp18c0JcbKTy3G6DE9FMB&notify_self=0", this);
        } else {
            // Nếu channel_id vẫn chưa được thiết lập, hiển thị thông báo hoặc xử lý lỗi tương ứng
            Log.e(TAG, "Error: Channel ID is null");
        }
    }

    @Override
    public void onMessageReceived(String message) {

    }

    @Override
    public void onConnectionStateChanged(boolean isConnected) {
        if (isConnected) {
            Log.d(TAG, "WebSocket Connected");
        } else {
            Log.e(TAG, "WebSocket Disconnected");
        }
    }
    private void changeData() {
        viewModel.setData("New data");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mainActivity.showBottomNavigation(false);
    }
}