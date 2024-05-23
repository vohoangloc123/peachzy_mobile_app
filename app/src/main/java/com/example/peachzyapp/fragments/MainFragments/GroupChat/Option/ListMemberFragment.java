package com.example.peachzyapp.fragments.MainFragments.GroupChat.Option;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.LiveData.MyGroupViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.adapters.ListMemberAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;

import java.util.ArrayList;
import java.util.Iterator;

public class ListMemberFragment extends Fragment {
    public static final String TAG= ListMemberFragment.class.getName();
    private View view;
    private ImageButton btnFindMember;
    private ImageButton btnBack;
    private EditText etNameOrEmail;
    private MainActivity mainActivity;
    private ArrayList<FriendItem> memberList;
    private RecyclerView rcvDeleteMember;
    private DynamoDBManager dynamoDBManager;
    private String groupID;
    private String uid;
    private ListMemberAdapter listMemberAdapter;
    private MyGroupViewModel viewModel;
    private FriendItem friendItem;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyGroupViewModel.class);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.list_member_fragment, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MyGroupViewModel.class);
        btnFindMember=view.findViewById(R.id.btnFindMember);
        btnBack=view.findViewById(R.id.btnBack);
        etNameOrEmail=view.findViewById(R.id.etNameOrEmail);
        rcvDeleteMember = view.findViewById(R.id.rcvListMember);
        //initial
        dynamoDBManager = new DynamoDBManager(getActivity());
        mainActivity = (MainActivity) getActivity();
        //set layout
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvDeleteMember.setLayoutManager(linearLayoutManager);
        //get data
        Bundle bundleReceive = getArguments();
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);
        groupID = bundleReceive.getString("groupID");
        Log.d("CheckGroupIdHere", groupID+" "+uid);
        memberList = new ArrayList<>();
        dynamoDBManager.findMemberOfGroup(groupID, id -> dynamoDBManager.getProfileByUID(id, groupID, new DynamoDBManager.FriendFoundForGetUIDByEmailListener() {
            @Override
            public void onFriendFound(String uid, String name, String email, String avatar, Boolean sex, String dateOfBirth) {

            }

            @Override
            public void onFriendFound(String id, String name, String email, String avatar, Boolean sex, String dateOfBirth, String role) {
                // Tạo FriendItem từ thông tin đã nhận được
                FriendItem friend = new FriendItem(id, avatar, name, role);
                memberList.add(friend);
                Iterator<FriendItem> iterator = memberList.iterator();
                while (iterator.hasNext()) {
                    FriendItem currentFriend = iterator.next();
                    // Kiểm tra nếu ID của bạn hiện tại là một chuỗi và nếu nó bằng với uid
                    if (currentFriend.getId().equals(String.valueOf(uid))) {
                        iterator.remove(); // Xóa đối tượng khỏi danh sách
                        break; // Đã xóa, không cần lặp tiếp
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listMemberAdapter.notifyDataSetChanged();
                    }
                });
            }
            @Override
            public void onFriendNotFound() {
                // Xử lý trường hợp không tìm thấy bạn bè
            }
            @Override
            public void onError(Exception e) {
                // Xử lý trường hợp lỗi
            }
        }));
        listMemberAdapter = new ListMemberAdapter(memberList, groupID, uid, dynamoDBManager, mainActivity);
        rcvDeleteMember.setAdapter(listMemberAdapter);
        btnBack.setOnClickListener(v->{
            getActivity().getSupportFragmentManager().popBackStack();
        });
        btnFindMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String info = etNameOrEmail.getText().toString().trim();
                searchForMember(info);
            }
        });


        return view;
    }

    private void searchForMember(String info) {
        dynamoDBManager.findFriendByInfor(info, uid, new DynamoDBManager.FriendFoundListener() {
            @Override
            public void onFriendFound(String id, String name, String avatar) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        friendItem = new FriendItem(id, avatar, name);
                        memberList.clear();
                        memberList.add(friendItem);

                        listMemberAdapter.notifyDataSetChanged();
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
    }
    private void changeData() {
        viewModel.setData("New data");
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Ngắt kết nối khi Fragment bị hủy
        mainActivity.showBottomNavigation(false);

    }
}
