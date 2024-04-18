package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.LiveData.MyGroupViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.adapters.DeleteMemberAdapter;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;
import java.util.ArrayList;
import java.util.List;

public class DeleteMemberFragment extends Fragment {
    public static final String TAG= DeleteMemberFragment.class.getName();
    private View view;
    private ImageButton btnFindMember;
    private Button btnCancel;
    private EditText etNameOrEmail;
    private MainActivity mainActivity;
    private ArrayList<FriendItem> memberList;
    private RecyclerView rcvDeleteMember;
    private DynamoDBManager dynamoDBManager;
    private String groupID;
    private String uid;
    private DeleteMemberAdapter deleteMemberAdapter;
    public Button btnDeleteMember;

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
        view = inflater.inflate(R.layout.delete_member_fragment, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MyGroupViewModel.class);
        btnFindMember=view.findViewById(R.id.btnFindMember);
        btnCancel=view.findViewById(R.id.btnCancel);
        etNameOrEmail=view.findViewById(R.id.etNameOrEmail);
        dynamoDBManager = new DynamoDBManager(getActivity());
        mainActivity = (MainActivity) getActivity();
        rcvDeleteMember = view.findViewById(R.id.rcvDeleteMember);
        btnDeleteMember=view.findViewById(R.id.btnDeleteMember);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvDeleteMember.setLayoutManager(linearLayoutManager);
//        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
//        rcvDeleteMember.addItemDecoration(itemDecoration);
        Bundle bundleReceive = getArguments();
        uid = bundleReceive.getString("uid");
        groupID = bundleReceive.getString("groupID");
        Log.d("CheckGroupIdHere", groupID+" "+uid);
        memberList = new ArrayList<>();


        dynamoDBManager.findMemberOfGroup(groupID, id -> dynamoDBManager.getProfileByUID(id, new DynamoDBManager.FriendFoundForGetUIDByEmailListener() {
            @Override
            public void onFriendFound(String uid, String name, String email, String avatar, Boolean sex, String dateOfBirth) {
                FriendItem friend = new FriendItem(uid, avatar, name);
                Log.d("CheckMembers", String.valueOf(friend));
                memberList.add(friend);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deleteMemberAdapter.notifyDataSetChanged();
                    }
                });
            }

            @Override
            public void onFriendNotFound() {
            }

            @Override
            public void onError(Exception e) {
            }
        }));

        deleteMemberAdapter = new DeleteMemberAdapter(memberList);
        rcvDeleteMember.setAdapter(deleteMemberAdapter);


        btnDeleteMember.setOnClickListener(v -> {
            List<String> selectedMemberIds = deleteMemberAdapter.getSelectedMemberIds();
            String lastMemberId = null; // Biến để lưu trữ người cuối cùng còn sót lại
            dynamoDBManager.deleteGroupFromUsers(groupID, selectedMemberIds);
            dynamoDBManager.deleteUserFromGroups(groupID, selectedMemberIds);
//            for (String memberId : selectedMemberIds) {
//                Log.d("CheckMemberInDelete", memberId);
//                dynamoDBManager.deleteGroupFromUser(memberId, groupID);
//                dynamoDBManager.deleteUserFromGroup(groupID, memberId);
//                lastMemberId = memberId; // Cập nhật biến lastMemberId với thành viên hiện tại trong vòng lặp
//            }
//
//            // Kiểm tra xem lastMemberId có khác null hay không trước khi thực hiện cập nhật
//            if (lastMemberId != null) {
//                // Thực hiện cập nhật trên người cuối cùng còn sót lại
//                dynamoDBManager.updateGroupForAccount(lastMemberId, groupID, "member");
//                dynamoDBManager.updateGroup(groupID, lastMemberId);
//            }

            Log.d("RemainingMembers", selectedMemberIds.toString());
            countMembersInGroupWithDelay();
        });
        btnFindMember.setOnClickListener(v->{
            String infor = etNameOrEmail.getText().toString().trim();
            // Log.d("Information", infor);
            dynamoDBManager.findFriendByInfor(infor, uid,new DynamoDBManager.FriendFoundListener() {
                @Override
                public void onFriendFound(String id, String name, String avatar) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            friendItem = new FriendItem(id, avatar, name);
                            memberList.clear();
                            memberList.add(friendItem);

                            deleteMemberAdapter.notifyDataSetChanged();
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
        btnCancel.setOnClickListener(v->{
            getActivity().getSupportFragmentManager().popBackStack();
        });


        return view;
    }

    public void countMembersInGroupWithDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dynamoDBManager.countMembersInGroup(groupID, new DynamoDBManager.CountMembersCallback() {
                    @Override
                    public void onCountComplete(int countMember) {

                        Log.d("onCountComplete", countMember + "" );
                        if (countMember <= 1) {
                            Log.d("onCountComplete1", "ok");
                            dynamoDBManager.deleteGroupConversation(groupID);
                            dynamoDBManager.deleteGroup(groupID);


                            getActivity().getSupportFragmentManager().popBackStack();
                            getActivity().getSupportFragmentManager().popBackStack();
                            getActivity().getSupportFragmentManager().popBackStack();


                        }
                        else {
                            getActivity().getSupportFragmentManager().popBackStack();
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                        //  changeData();

                    }
                });
            }

        }, 200); // 0.5 giây (500 mili giây)
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

