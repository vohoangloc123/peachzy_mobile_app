package com.example.peachzyapp.fragments.MainFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.peachzyapp.R;
import com.example.peachzyapp.entities.ChatBox;


public class ChatHistoryFragment extends Fragment {


    Button btnBack;
    TextView tvName;
    View view;
    public static final String TAG=ChatHistoryFragment.class.getName();
    public ChatHistoryFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view=inflater.inflate(R.layout.fragment_chat_history, container, false);
        tvName = view.findViewById(R.id.tv_name); // Sửa đổi ở đây
        btnBack=view.findViewById(R.id.backButton);

        Bundle bundleReceive=getArguments();
        if(bundleReceive!=null){
            ChatBox chatBox= (ChatBox) bundleReceive.get("object_chatbox");
            if(chatBox!=null&& tvName != null)
            {
                tvName.setText(chatBox.getName());
            }
        }
        btnBack.setOnClickListener(v->{
           if(getFragmentManager()!=null)
           {
               getFragmentManager().popBackStack();
           }
        });

        return view;
    }
}