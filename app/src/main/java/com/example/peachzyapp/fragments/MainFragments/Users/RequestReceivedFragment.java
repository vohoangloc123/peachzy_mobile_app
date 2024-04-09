package com.example.peachzyapp.fragments.MainFragments.Users;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.LiveData.MyViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.adapters.RequestReceivedAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatHistoryFragment;

import java.util.ArrayList;
import java.util.List;

public class RequestReceivedFragment extends Fragment {
    private String test;
    public static final String TAG2= ChatHistoryFragment.class.getName();
    String uid;

    private DynamoDBManager dynamoDBManager;
    RecyclerView rcvRequestReceived;
    private RequestReceivedAdapter requestReceivedAdapter;
    private View view;
    private MainActivity mainActivity;
    FriendItem friendItem;

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
        // Inflate the layout for this fragment
        friendList = new ArrayList<>();
        dynamoDBManager = new DynamoDBManager(getActivity());
        view = inflater.inflate(R.layout.activity_request_received_fragments, container, false);
        Bundle bundleReceive = getArguments();
        uid = bundleReceive.getString("uid");
        Log.d("RequestUID", "onCreateView: " + uid);
        requestReceivedAdapter = new RequestReceivedAdapter(friendList);
        requestReceivedAdapter.setUid(uid);
        mainActivity = (MainActivity) getActivity();
        rcvRequestReceived = view.findViewById(R.id.rcvRequestReceived);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvRequestReceived.setLayoutManager(linearLayoutManager);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
        rcvRequestReceived.addItemDecoration(itemDecoration);

        dynamoDBManager.getIDFriend(uid, "3", new DynamoDBManager.AlreadyFriendListener() {
            @Override
            public void onFriendAlreadyFound(FriendItem data) {

            }

            @Override
            public void onFriendAcceptRequestFound(String id, String name, String avatar) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        friendItem = new FriendItem(id, avatar, name);
                        friendList.clear();
                        friendList.add(friendItem);
                        requestReceivedAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        rcvRequestReceived.setAdapter(requestReceivedAdapter);
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        changeData();

        return view;
    }

    private List<FriendItem> getListFriends() {
        List<FriendItem> list= new ArrayList<>();
        for(int i=1; i<20;i++){
            list.add(new FriendItem("name"+i));
        }
        return list;
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
