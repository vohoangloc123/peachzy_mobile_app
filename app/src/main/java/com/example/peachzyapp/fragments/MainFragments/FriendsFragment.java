package com.example.peachzyapp.fragments.MainFragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.peachzyapp.AddFriendFragment;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
public class FriendsFragment extends Fragment {
    Button btnAddfriend;
    RecyclerView rcvFriendList;
    private MainActivity mainActivity;
    public static final String TAG= AddFriendFragment.class.getName();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view= inflater.inflate(R.layout.fragment_friends, container, false);
        mainActivity= (MainActivity) getActivity();
        btnAddfriend=view.findViewById(R.id.btnAddFriend);
        btnAddfriend.setOnClickListener(v->{
            mainActivity.goToDetailFragmentAddFriend();
        });
        return view;
    }
}