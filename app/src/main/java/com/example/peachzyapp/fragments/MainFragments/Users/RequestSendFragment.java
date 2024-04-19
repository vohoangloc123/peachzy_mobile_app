package com.example.peachzyapp.fragments.MainFragments.Users;

import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.adapters.RequestSentAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatHistoryFragment;

import java.util.ArrayList;
import java.util.List;

public class RequestSendFragment extends Fragment {
    public static final String TAG= ChatHistoryFragment.class.getName();
    RecyclerView rcvRequestSent;
    ImageButton btnBack;
    private View view;
    private MainActivity mainActivity;
    private RequestSentAdapter requestSentAdapter;
    private ArrayList<FriendItem> friendList;
    private DynamoDBManager dynamoDBManager;
    String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        friendList = new ArrayList<>();
        dynamoDBManager = new DynamoDBManager(getActivity());
        view = inflater.inflate(R.layout.activity_request_sent_fragments, container, false);
        btnBack=view.findViewById(R.id.btnBack);
        Bundle bundleReceive=getArguments();
        uid = bundleReceive.getString("uid");

        mainActivity= (MainActivity) getActivity();
        rcvRequestSent = view.findViewById(R.id.rcvRequestSent);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvRequestSent.setLayoutManager(linearLayoutManager);

        btnBack.setOnClickListener(v->{
            getParentFragmentManager().popBackStack();
            mainActivity.showBottomNavigation(true);
        });
        dynamoDBManager.getIDFriend(uid,"2", new DynamoDBManager.AlreadyFriendListener() {
            @Override
            public void onFriendAlreadyFound(FriendItem data) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("onDATAREquest", "run: "+data.getName());
                        friendList.add(data);
                        requestSentAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFriendAcceptRequestFound(String id, String name, String avatar) {

            }

            @Override
            public void onFriendCreateGroupFound(FriendItem friendItem) {

            }
        });


        requestSentAdapter= new RequestSentAdapter(friendList);
        rcvRequestSent.setAdapter(requestSentAdapter);
//        RecyclerView.ItemDecoration itemDecoration=new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
//        rcvRequestSent.addItemDecoration(itemDecoration);

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
