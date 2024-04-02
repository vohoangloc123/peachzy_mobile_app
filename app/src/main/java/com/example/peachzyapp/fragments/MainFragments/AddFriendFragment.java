package com.example.peachzyapp.fragments.MainFragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.peachzyapp.adapters.FriendAdapter;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;

import java.util.ArrayList;

//public class AddFriendFragment extends Fragment {
//    EditText etEmail;
//    Button btnFind;
//    private DynamoDBManager dynamoDBManager;
//    private TextView tvView;
//    public static final String TAG1= AddFriendFragment.class.getName();
//    private ListView listView; // Assume you're using ListView
//    private ArrayList<FriendItem> friendItems;
//    private FriendAdapter friendAdapter;
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.activity_add_friend_fragments, container, false);
//        etEmail=view.findViewById(R.id.etEmail);
//        btnFind=view.findViewById(R.id.btnFind);
//        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//        dynamoDBManager=new DynamoDBManager(getActivity());
//        listView = view.findViewById(R.id.list_item);
//        friendItems = new ArrayList<>();
//        friendAdapter = new FriendAdapter(getActivity(), R.layout.activity_friend_adapter, friendItems);
//        listView.setAdapter(friendAdapter);
//        btnFind.setOnClickListener(v -> {
//            String email = etEmail.getText().toString().trim();
//            dynamoDBManager.findFriend(email, new DynamoDBManager.FriendFoundListener() {
//                @Override
//                public void onFriendFound(String friendResult, String avatar) {
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            tvView.setText(friendResult);
//                            Log.d("FriendResult", friendResult);
//                            Toast.makeText(getActivity(), "Friend found!", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//
//                @Override
//                public void onFriendNotFound() {
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            tvView.setText("Friend not found");
//                            Toast.makeText(getActivity(), "Friend not found", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//
//                @Override
//                public void onError(Exception e) {
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Log.e("Error", "Exception occurred: ", e);
//                            Toast.makeText(getActivity(), "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                }
//            });
//        });
//        return view;
//    }
//}
public class AddFriendFragment extends Fragment {
    EditText etEmail;
    Button btnFind;
    private DynamoDBManager dynamoDBManager;
    public static final String TAG1= AddFriendFragment.class.getName();
    private ListView listView; // Assume you're using ListView
    private ArrayList<FriendItem> friendItems;
    private FriendAdapter friendAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_add_friend_fragments, container, false);
        etEmail = view.findViewById(R.id.etEmail);
        btnFind = view.findViewById(R.id.btnFind);
        dynamoDBManager = new DynamoDBManager(getActivity());
        listView = view.findViewById(R.id.list_item);
        friendItems = new ArrayList<>();
        friendAdapter = new FriendAdapter(getActivity(), R.layout.activity_friend_adapter, friendItems);
        listView.setAdapter(friendAdapter);

        btnFind.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            dynamoDBManager.findFriend(email, new DynamoDBManager.FriendFoundListener() {
                @Override
                public void onFriendFound(String name, String avatar) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            FriendItem friendItem = new FriendItem(avatar, name);
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