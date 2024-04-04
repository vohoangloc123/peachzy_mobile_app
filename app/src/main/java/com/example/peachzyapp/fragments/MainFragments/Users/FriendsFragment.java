package com.example.peachzyapp.fragments.MainFragments.Users;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

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
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;

import java.util.ArrayList;

public class FriendsFragment extends Fragment {
    Button btnAddfriend;

    Button btnRequestReceived;
    Button btnRequestSent;
    RecyclerView rcvFriendList;
    private MainActivity mainActivity;
    public static final String TAG= AddFriendFragment.class.getName();

    private DynamoDBManager dynamoDBManager;
    private FriendAlreadyAdapter friendAdapter;

    private ArrayList<FriendItem> friendList;
    private View view;
    String uid;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        friendList = new ArrayList<>();
        view= inflater.inflate(R.layout.fragment_friends, container, false);

        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);
        if (uid != null) {
            Log.d("FriendcheckUID", uid);
            // Sử dụng "uid" ở đây cho các mục đích của bạn
        } else {
            Log.e("FriendcheckUID", "UID is null");
        }

        dynamoDBManager = new DynamoDBManager(getActivity());

        dynamoDBManager.getIDFriend(uid,"1", new DynamoDBManager.AlreadyFriendListener() {
            @Override
            public void onFriendAlreadyFound(FriendItem data) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("onDATA", "run: "+data.getName());
                        friendList.add(data);
                        friendAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        mainActivity= (MainActivity) getActivity();
        btnAddfriend=view.findViewById(R.id.btnAddFriend);
        btnAddfriend.setOnClickListener(v->{
            mainActivity.goToDetailFragmentAddFriend();
        });

        rcvFriendList = view.findViewById(R.id.rcvFriendList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvFriendList.setLayoutManager(linearLayoutManager);

        btnRequestReceived=view.findViewById(R.id.btnRequestReceived);
        btnRequestReceived.setOnClickListener(v->{
            mainActivity.goToRequestReceivedFragment();
        });
        btnRequestSent=view.findViewById(R.id.btnRequestSent);
        btnRequestSent.setOnClickListener(v->{
            mainActivity.goToRequestSentFragment();
        });

        //      FriendAdapter friendAdapter= new FriendAdapter(getListFriends());
        friendAdapter= new FriendAlreadyAdapter(friendList);
        rcvFriendList.setAdapter(friendAdapter);
        RecyclerView.ItemDecoration itemDecoration=new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
        rcvFriendList.addItemDecoration(itemDecoration);
        return view;
    }


}