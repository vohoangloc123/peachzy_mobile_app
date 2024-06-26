package com.example.peachzyapp.fragments.MainFragments.Users;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.adapters.FriendAdapter;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;

import java.util.ArrayList;

public class AddFriendFragment extends Fragment {
    EditText etEmail;
    ImageButton btnFind;
    ImageButton btnBack;
    private DynamoDBManager dynamoDBManager;
    private MainActivity mainActivity;
    public static final String TAG1= AddFriendFragment.class.getName();
    private ListView listView; // Assume you're using ListView
    private ArrayList<FriendItem> friendItems;
    private FriendAdapter friendAdapter;
    String uid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.add_friend_fragment, container, false);
        etEmail = view.findViewById(R.id.etEmail);
        btnFind = view.findViewById(R.id.btnFind);
        btnBack = view.findViewById(R.id.btnBack);
        dynamoDBManager = new DynamoDBManager(getActivity());
        mainActivity = (MainActivity) getActivity();
        listView = view.findViewById(R.id.list_item);
        friendItems = new ArrayList<>();
        friendAdapter = new FriendAdapter(getActivity(), R.layout.item_add_friend, friendItems);
        listView.setAdapter(friendAdapter);
        uid=getArguments().getString("uid");
        friendAdapter.setUid(uid);
        btnBack.setOnClickListener(v->{
            getParentFragmentManager().popBackStack();
            mainActivity.showBottomNavigation(true);
        });
        btnFind.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            dynamoDBManager.findFriend(email, new DynamoDBManager.FriendFoundListener() {
                @Override
                public void onFriendFound(String id, String name, String avatar) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FriendItem friendItem = new FriendItem(id, avatar, name);
                            friendItems.clear();
                            friendItems.add(friendItem);

                            friendAdapter.notifyDataSetChanged();
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
        return view;
    }
}