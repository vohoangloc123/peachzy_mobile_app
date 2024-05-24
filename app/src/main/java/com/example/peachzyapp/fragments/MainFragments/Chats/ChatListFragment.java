package com.example.peachzyapp.fragments.MainFragments.Chats;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.peachzyapp.LiveData.MyViewChatModel;
import com.example.peachzyapp.adapters.ConversationAdapter;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.Conversation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class ChatListFragment extends Fragment {
    private MainActivity mainActivity;
    private RecyclerView rcvChatList;
    private View view;
    private ArrayList<Conversation> conversationsList;
    private DynamoDBManager dynamoDBManager;
    private String uid;
    private ConversationAdapter conversationAdapter;
    private MyViewChatModel viewModel;
    private SimpleDateFormat dateFormat;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mainActivity = (MainActivity) getActivity();
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        dynamoDBManager = new DynamoDBManager(getActivity());
        rcvChatList = view.findViewById(R.id.rcvConversation);
        rcvChatList.setLayoutManager(new LinearLayoutManager(mainActivity));
        // Initialize conversationsList before calling loadConversations()
        conversationsList = new ArrayList<>();
        conversationAdapter = new ConversationAdapter(conversationsList);
        // Set adapter to RecyclerView
        rcvChatList.setAdapter(conversationAdapter);
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);
        if (uid != null) {
            Log.d("FriendcheckUIDInChatListFragment", uid);
            // Sử dụng "uid" ở đây cho các mục đích của bạn
        } else {
            Log.e("FriendcheckUID", "UID is null");
        }
        // Define the format for parsing the date and time strings
        dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        dynamoDBManager.loadConversation(uid, new DynamoDBManager.LoadConversationListener() {
            @Override
            public void onConversationFound(String conversationID, String friendID, String message, String time, String avatar, String name) {
                try {
                    // Parse the time string into a Date object
                    Date conversationTime = dateFormat.parse(time);

                    // Create the conversation object
                    Conversation conversation = new Conversation(conversationID, friendID, message, time, avatar, name);

                    // Add the conversation to the list
                    conversationsList.add(conversation);

                    // Sort the list based on the conversationTime in descending order
                    Collections.sort(conversationsList, new Comparator<Conversation>() {
                        @Override
                        public int compare(Conversation c1, Conversation c2) {
                            Date date1 = null;
                            Date date2 = null;
                            try {
                                date1 = dateFormat.parse(c1.getTime());
                                date2 = dateFormat.parse(c2.getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            return date2.compareTo(date1); // Sort in descending order
                        }
                    });

                    // Notify the adapter of the dataset change
                    conversationAdapter.notifyDataSetChanged();
                } catch (ParseException e) {
                    // Handle parsing exceptions if any
                    e.printStackTrace();
                }
            }

            @Override
            public void onLoadConversationError(Exception e) {
                e.printStackTrace();
            }
        });

        conversationAdapter.setOnItemClickListener(new ConversationAdapter.OnItemClickListener(){

            @Override
            public void onItemClick(String id, String urlAvatar, String friendName) {
                Bundle bundle = new Bundle();
                bundle.putString("friend_id", id);
                bundle.putString("urlAvatar",urlAvatar);
                bundle.putString("friendName", friendName);
                Log.d("urlAvatarhere", urlAvatar);
                mainActivity.goToChatBoxFragment(bundle);
            }
        });
        //Live data
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewChatModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String newData) {
                Log.d("Livedata1", "onChanged: Yes");
                resetRecycleView();
            }
        });

        return view;
    }

    private void resetRecycleView() {
        conversationsList.clear();
        Log.d("batdauchay", "yes: ");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                dynamoDBManager.loadConversation(uid, new DynamoDBManager.LoadConversationListener() {
                    @Override
                    public void onConversationFound(String conversationID, String friendID, String message, String time, String avatar, String name) {
                        try {
                            // Parse the time string into a Date object
                            Date conversationTime = dateFormat.parse(time);

                            // Create the conversation object
                            Conversation conversation = new Conversation(conversationID, friendID, message, time, avatar, name);

                            // Add the conversation to the list
                            conversationsList.add(conversation);

                            // Sort the list based on the conversationTime in descending order
                            Collections.sort(conversationsList, new Comparator<Conversation>() {
                                @Override
                                public int compare(Conversation c1, Conversation c2) {
                                    Date date1 = null;
                                    Date date2 = null;
                                    try {
                                        date1 = dateFormat.parse(c1.getTime());
                                        date2 = dateFormat.parse(c2.getTime());
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    return date2.compareTo(date1); // Sort in descending order
                                }
                            });

                            // Notify the adapter of the dataset change
                            conversationAdapter.notifyDataSetChanged();
                        } catch (ParseException e) {
                            // Handle parsing exceptions if any
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onLoadConversationError(Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        }, 1000); // 2000 milliseconds = 2 seconds
    }
}
