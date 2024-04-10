package com.example.peachzyapp.fragments.MainFragments.Users;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.peachzyapp.LiveData.MyViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.adapters.FriendAlreadyAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.fragments.ForgotPasswordFragments.ForgetPasswordOTPFragments;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatHistoryFragment;

import java.util.ArrayList;

public class FriendsFragment extends Fragment {
    private String test;
    ImageButton btnAddfriend;

    ImageButton btnRequestReceived;
    ImageButton btnRequestSent;
    RecyclerView rcvFriendList;
    private MainActivity mainActivity;
    public static final String TAG= AddFriendFragment.class.getName();

    private DynamoDBManager dynamoDBManager;
    private FriendAlreadyAdapter friendAdapter;

    private ArrayList<FriendItem> friendList;
    FriendItem friendItem;
    private View view;
    String uid;
    private MyViewModel viewModel;
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
        Bundle bundle = new Bundle();
        bundle.putString("uid", uid);
        AddFriendFragment fragment = new AddFriendFragment();
        fragment.setArguments(bundle);
        dynamoDBManager = new DynamoDBManager(getActivity());

        dynamoDBManager.getIDFriend(uid,"1", new DynamoDBManager.AlreadyFriendListener() {
            @Override
            public void onFriendAlreadyFound(FriendItem data) {

            }

            @Override
            public void onFriendAcceptRequestFound(String id, String name, String avatar) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        friendItem = new FriendItem(id, avatar, name);

                        friendList.add(friendItem);
                        friendAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        //Live data
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String newData) {
                Log.d("Livedata", "onChanged: Yes");
                // Cập nhật RecyclerView hoặc bất kỳ thành phần UI nào khác ở đây
                // newData chứa dữ liệu mới từ Fragment con
                resetRecycleView();
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
        friendAdapter= new FriendAlreadyAdapter(friendList);
        rcvFriendList.setAdapter(friendAdapter);

        friendAdapter.setOnItemClickListener(new FriendAlreadyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String id, String urlAvatar, String friendName) {
                Bundle bundle = new Bundle();
                bundle.putString("friend_id", id);
                bundle.putString("urlAvatar",urlAvatar);
                bundle.putString("friendName", friendName);
                Log.d("urlAvatarhere", urlAvatar);
                mainActivity.goToChatBoxFragment(bundle);
            }
        });

        RecyclerView.ItemDecoration itemDecoration=new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
        rcvFriendList.addItemDecoration(itemDecoration);
        return view;
    }

    public void resetRecycleView(){
        friendList.clear();
        dynamoDBManager.getIDFriend(uid,"1", new DynamoDBManager.AlreadyFriendListener() {
            @Override
            public void onFriendAlreadyFound(FriendItem data) {

            }

            @Override
            public void onFriendAcceptRequestFound(String id, String name, String avatar) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        friendItem = new FriendItem(id, avatar, name);

                        friendList.add(friendItem);
                        friendAdapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }

}