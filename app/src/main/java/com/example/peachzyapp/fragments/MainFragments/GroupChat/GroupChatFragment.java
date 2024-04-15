package com.example.peachzyapp.fragments.MainFragments.GroupChat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.peachzyapp.R;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatHistoryFragment;

public class GroupChatFragment extends Fragment {
    public static final String TAG= ChatHistoryFragment.class.getName();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_group_chat, container, false);
    }
}
