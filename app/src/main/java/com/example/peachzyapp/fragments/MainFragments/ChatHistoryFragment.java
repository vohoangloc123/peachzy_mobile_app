package com.example.peachzyapp.fragments.MainFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.peachzyapp.R;
import com.example.peachzyapp.adapters.ChatAdapter;
import com.example.peachzyapp.entities.ChatBox;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import okhttp3.WebSocket;


public class ChatHistoryFragment extends Fragment {


    Button btnBack;
    Button btnSend;
    TextView tvName;
    EditText etMessage;
    View view;
    private BottomNavigationView bottomNavigationView;
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<String> chatMessages;
    private WebSocket webSocket;

    public static final String TAG=ChatHistoryFragment.class.getName();
    public ChatHistoryFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        view=inflater.inflate(R.layout.fragment_chat_history, container, false);
        tvName = view.findViewById(R.id.tv_name);
        btnBack=view.findViewById(R.id.backButton);
        btnSend=view.findViewById(R.id.btnSend);
        etMessage=view.findViewById(R.id.etMessage);
        bottomNavigationView=getActivity().findViewById(R.id.bottom_navigation);
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
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });
        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                webSocket.send(message);
                etMessage.setText("");
            }
        });
        return view;
    }

}