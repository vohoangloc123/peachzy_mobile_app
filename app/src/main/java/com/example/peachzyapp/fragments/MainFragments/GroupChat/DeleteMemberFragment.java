package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private MainActivity mainActivity;
    private ArrayList<FriendItem> memberList;
    private RecyclerView rcvDeleteMember;
    private DynamoDBManager dynamoDBManager;
    private String groupID;
    private DeleteMemberAdapter deleteMemberAdapter;
    public Button btnDeleteMember;

    private MyGroupViewModel viewModel;

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
        dynamoDBManager = new DynamoDBManager(getActivity());
        mainActivity = (MainActivity) getActivity();
        rcvDeleteMember = view.findViewById(R.id.rcvDeleteMember);
        btnDeleteMember=view.findViewById(R.id.btnDeleteMember);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainActivity);
        rcvDeleteMember.setLayoutManager(linearLayoutManager);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
        rcvDeleteMember.addItemDecoration(itemDecoration);
        Bundle bundleReceive = getArguments();
        groupID = bundleReceive.getString("groupID");
        Log.d("CheckGroupIdHere", groupID);
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


        btnDeleteMember.setOnClickListener(v->{
            List<String> selectedMemberIds = deleteMemberAdapter.getSelectedMemberIds();
            for (String memberId : selectedMemberIds) {
                Log.d("CheckMemberInDelete", memberId);
                dynamoDBManager.deleteGroupFromUser(memberId, groupID);
                dynamoDBManager.deleteUserFromGroup(groupID, memberId);
            }


            countMembersInGroupWithDelay();
            changeData();

            ////////////////
            getActivity().getSupportFragmentManager().popBackStack();
            getActivity().getSupportFragmentManager().popBackStack();
            getActivity().getSupportFragmentManager().popBackStack();

//            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
//            transaction.replace(R.id.fragment_container, new GroupChatListFragment());
//            transaction.addToBackStack("GroupChatListFragment");
//            transaction.commit();
//            getParentFragmentManager().popBackStackImmediate("GroupChatListFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);

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

                            changeData();



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

}

