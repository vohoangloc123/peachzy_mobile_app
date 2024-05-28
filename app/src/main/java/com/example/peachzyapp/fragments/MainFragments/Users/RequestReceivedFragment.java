package com.example.peachzyapp.fragments.MainFragments.Users;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.LiveData.MyViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.SocketIO.MyWebSocket;
import com.example.peachzyapp.adapters.RequestReceivedAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RequestReceivedFragment extends Fragment implements MyWebSocket.WebSocketListener{
    public static final String TAG = RequestReceivedFragment.class.getName();
    private MyWebSocket myWebSocket;
    private ImageButton btnBack;
    private String uid;
    private DynamoDBManager dynamoDBManager;
    private RecyclerView rcvRequestReceived;
    private RequestReceivedAdapter requestReceivedAdapter;
    private View view;
    private MainActivity mainActivity;
    private FriendItem friendItem;
    private ArrayList<FriendItem> friendList;

    private MyViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        friendList = new ArrayList<>();
        dynamoDBManager = new DynamoDBManager(getActivity());
        view = inflater.inflate(R.layout.request_received_fragment, container, false);
        btnBack = view.findViewById(R.id.btnBack);
        Bundle bundleReceive = getArguments();
        uid = bundleReceive.getString("uid");

        mainActivity = (MainActivity) getActivity();
        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
            mainActivity.showBottomNavigation(true);
        });

        rcvRequestReceived = view.findViewById(R.id.rcvRequestReceived);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvRequestReceived.setLayoutManager(linearLayoutManager);
        requestReceivedAdapter = new RequestReceivedAdapter(friendList);
        requestReceivedAdapter.setUid(uid);
        rcvRequestReceived.setAdapter(requestReceivedAdapter);

        dynamoDBManager.getIDFriend(uid, "3", new DynamoDBManager.AlreadyFriendListener() {
            @Override
            public void onFriendAlreadyFound(FriendItem data) {
                // Handle case when friend is already found
            }
            @Override
            public void onFriendAcceptRequestFound(String id, String name, String avatar) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        friendItem = new FriendItem(id, avatar, name);
                        friendList.add(friendItem);
                        requestReceivedAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFriendCreateGroupFound(FriendItem friendItem) {

            }

            @Override
            public void onFriendNotFound(String error) {

            }


        });

        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
//        changeData();
        initWebSocket(uid);
        return view;
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

    @Override
    public void onMessageReceived(String receivedMessage) {
        Log.d("onMessageReceived2: ",receivedMessage);
//        viewModel.setData(message);
        try {

            JSONObject jsonObject  = new JSONObject(receivedMessage);
            String typeJson = jsonObject.getString("type");


            if(typeJson.equals("friend-request")){
                friendList.clear();
                dynamoDBManager.getIDFriend(uid, "3", new DynamoDBManager.AlreadyFriendListener() {
                    @Override
                    public void onFriendAlreadyFound(FriendItem data) {
                        // Handle case when friend is already found
                    }
                    @Override
                    public void onFriendAcceptRequestFound(String id, String name, String avatar) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                friendItem = new FriendItem(id, avatar, name);
                                friendList.add(friendItem);
                                requestReceivedAdapter.notifyDataSetChanged();
                            }
                        });
                    }

                    @Override
                    public void onFriendCreateGroupFound(FriendItem friendItem) {

                    }

                    @Override
                    public void onFriendNotFound(String error) {

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
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        myWebSocket.closeWebSocket();
    }
}