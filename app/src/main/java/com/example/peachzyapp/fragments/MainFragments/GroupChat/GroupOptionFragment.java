package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.media.Image;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatHistoryFragment;

public class GroupOptionFragment extends Fragment {
    public static final String TAG= ChatHistoryFragment.class.getName();
    String groupID;
    String groupName;
    String groupAvatar;
    ImageButton btnBack;
    ImageButton btnDeleteMember;
    ImageButton btnAddMember;
    ImageButton btnOutGroup;
    ImageButton btnDeleteGroup;
    TextView tvDeleteGroup;
    ImageView ivGroupAvatar;
    TextView tvGroupName;
    MainActivity mainActivity;
    String userID;
    DynamoDBManager dynamoDBManager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_group_option, container, false);
        btnBack=view.findViewById(R.id.btnBack);
        btnDeleteMember=view.findViewById(R.id.btnDeleteMember);
        btnAddMember=view.findViewById(R.id.btnAddMember);
        btnOutGroup=view.findViewById(R.id.btnOutGroup);
        btnDeleteGroup=view.findViewById(R.id.btnDeleteGroup);
        ivGroupAvatar=view.findViewById(R.id.ivGroupAvatar);
        tvGroupName=view.findViewById(R.id.tvGroupName);
        tvDeleteGroup=view.findViewById(R.id.tvDeleteGroup);
        //initial
        mainActivity = (MainActivity) getActivity();
        dynamoDBManager=new DynamoDBManager(getContext());
        //bundle
        Bundle bundleReceive=getArguments();
        groupID = bundleReceive.getString("groupID");
        Log.d("CheckBundleOfGroupChat", "onCreateView: "+groupID);
        groupName= bundleReceive.getString("groupName");
        Log.d("CheckBundleOfGroupChat", "onCreateView: "+groupName);
        groupAvatar= bundleReceive.getString("groupAvatar");
        Log.d("CheckBundleOfGroupChat", "onCreateView: "+groupAvatar);
        Glide.with(getContext())
                .load(groupAvatar)
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(ivGroupAvatar);
        tvGroupName.setText(groupName);
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userID = preferences.getString("uid", null);
        if (userID != null) {
            Log.d("FriendcheckUID", userID);
            // Sử dụng "uid" ở đây cho các mục đích của bạn
        } else {
            Log.e("FriendcheckUID", "UID is null");
        }
        //button
        btnBack.setOnClickListener(v->{
            getActivity().getSupportFragmentManager().popBackStack();
        });
        btnDeleteMember.setOnClickListener(v->{
            Bundle bundle = new Bundle();
            bundle.putString("groupID", groupID);
            mainActivity.goToDeleteMember(bundle);
            Log.d("CheckButton", "WORK");
        });
        btnAddMember.setOnClickListener(v->{
            Bundle bundle = new Bundle();
            bundle.putString("groupID", groupID);

            mainActivity.goToAddMembersToGroup(bundle);
        });
        btnOutGroup.setOnClickListener(v->{
            dynamoDBManager.deleteUserFromGroup(groupID, userID);
            dynamoDBManager.deleteGroupFromUser(userID, groupID);
            dynamoDBManager.countMembersInGroup(groupID, new DynamoDBManager.CountMembersCallback() {
                @Override
                public void onCountComplete(int countMember) {
                    if (countMember <= 1) {
                        dynamoDBManager.deleteGroup(groupID);
                        dynamoDBManager.deleteGroupConversation(groupID);
                        getActivity().getSupportFragmentManager().popBackStack();
                    }else
                    {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                }
            });
        });
        dynamoDBManager.getGroupInfoByUser(userID, new DynamoDBManager.LoadGroupInfoListener() {
            @Override
            public void onGroupInfoFound(String groupID, String role) {
                if(groupID.equals(groupID)&&role.equals("leader"))
                {
                    btnDeleteGroup.setEnabled(true);
                }
                else
                {
                    btnDeleteGroup.setAlpha(0.5f);
                    int grayColor = Color.argb(255, 128, 128, 128); // Màu xám (RGB: 128, 128, 128)
                    tvDeleteGroup.setTextColor(grayColor);
                    btnDeleteGroup.setEnabled(false);
                }
            }
        });
        btnDeleteGroup.setOnClickListener(v->{
            dynamoDBManager.deleteGroupConversation(groupID);
            dynamoDBManager.deleteGroup(groupID);
            //xóa groupID trong group của bảng Users của mình
            dynamoDBManager.deleteUserFromGroup(groupID, userID);
            dynamoDBManager.deleteGroupFromUser(userID, groupID);
            //xóa groupID trong group của bảng Users của những member khác
            dynamoDBManager.deleteUserFromGroup(groupID, userID);
            dynamoDBManager.deleteGroupFromUser(userID, groupID);
        });

       return view;
    }
}