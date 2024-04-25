package com.example.peachzyapp.fragments.MainFragments.GroupChat.Option;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatBoxFragment;

public class EditGroupNameFragment extends Fragment {
    public static final String TAG= EditGroupNameFragment.class.getName();
    private EditText etGroupName;
    private Button btnSave;
    private Button btnCancel;
    private MainActivity mainActivity;
    private DynamoDBManager dynamoDBManager;
    private String groupID;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_edit_group_name, container, false);
        etGroupName=view.findViewById(R.id.etGroupName);
        btnSave=view.findViewById(R.id.btnSave);
        btnCancel=view.findViewById(R.id.btnCancel);
        mainActivity=new MainActivity();
        dynamoDBManager=new DynamoDBManager(getContext());
        //get data
        Bundle bundle = getArguments();
        if (bundle != null) {
            groupID = bundle.getString("groupID");
        }
        btnSave.setOnClickListener(v->{
            String groupName=etGroupName.getText().toString().trim();
            dynamoDBManager.updateGroupName(groupID, groupName);
            GroupOptionFragment groupOptionFragment = (GroupOptionFragment) getActivity().getSupportFragmentManager().findFragmentByTag(GroupOptionFragment.TAG);
            if (groupOptionFragment != null) {
                groupOptionFragment.setGroupName(groupName);
            }
            ChatBoxFragment chatBoxFragment = (ChatBoxFragment)  getActivity().getSupportFragmentManager().findFragmentByTag(ChatBoxFragment.TAG);
            if (chatBoxFragment != null) {
                chatBoxFragment.setGroupName(groupName);
            }
            getActivity().getSupportFragmentManager().popBackStack();
        });
        btnCancel.setOnClickListener(v->{

            getActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }
}