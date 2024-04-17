package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.graphics.Bitmap;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatHistoryFragment;

public class GroupOptionFragment extends Fragment {
    public static final String TAG= ChatHistoryFragment.class.getName();
    String groupID;
    String groupName;
    String groupAvatar;
    ImageButton btnBack;
    ImageButton btnDeleteMember;
    ImageButton btnAddMember;
    ImageView ivGroupAvatar;
    TextView tvGroupName;
    MainActivity mainActivity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_group_option, container, false);
        btnBack=view.findViewById(R.id.btnBack);
        btnDeleteMember=view.findViewById(R.id.btnDeleteMember);
        btnAddMember=view.findViewById(R.id.btnAddMember);
        ivGroupAvatar=view.findViewById(R.id.ivGroupAvatar);
        tvGroupName=view.findViewById(R.id.tvGroupName);
        //initial
        mainActivity = (MainActivity) getActivity();
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

       return view;
    }
}