package com.example.peachzyapp.fragments.MainFragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.peachzyapp.adapters.ChatBoxAdapter;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.entities.ChatBox;

import java.util.ArrayList;
import java.util.List;

public class ChatListsFragment extends Fragment {


    private MainActivity mainActivity;
    private RecyclerView rcvChatList;
    private View view;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.e("Loc", "Fragment 1");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("Loc", "Fragment 1");

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity= (MainActivity) getActivity();
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_chat_lists, container, false);

        rcvChatList = view.findViewById(R.id.rcv_user);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(mainActivity);
        rcvChatList.setLayoutManager(linearLayoutManager);

        ChatBoxAdapter chatBoxAdapter=new ChatBoxAdapter(getListChatBox(), new ChatBoxAdapter.IClickItemListener() {
            @Override
            public void onClickItemChatBox(ChatBox chatBox) {
                    mainActivity.goToDetailFragment(chatBox);
            }
        });
        rcvChatList.setAdapter(chatBoxAdapter);
        //tạo đường kẻ giữa các chatbox
        RecyclerView.ItemDecoration itemDecoration=new DividerItemDecoration(mainActivity, DividerItemDecoration.VERTICAL);
        rcvChatList.addItemDecoration(itemDecoration);


        return view;
    }

    private List<ChatBox> getListChatBox() {
        List<ChatBox> listChatBox=new ArrayList<>();
        for(int i=1;i<=20;i++)
        {
            listChatBox.add(new ChatBox("this is user "+i));
        }
        return listChatBox;
    }

    public void reloadData() {
    // Làm mới dữ liệu ở đây (ví dụ: load lại danh sách chatbox)
    ChatBoxAdapter chatBoxAdapter = new ChatBoxAdapter(getListChatBox(), new ChatBoxAdapter.IClickItemListener() {
        @Override
        public void onClickItemChatBox(ChatBox chatBox) {
            mainActivity.goToDetailFragment(chatBox);
        }
    });
    rcvChatList.setAdapter(chatBoxAdapter);
    Toast.makeText(getActivity(), "Reload fragment 1", Toast.LENGTH_SHORT).show();
    }
}