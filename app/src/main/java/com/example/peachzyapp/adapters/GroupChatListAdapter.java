package com.example.peachzyapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.R;
import com.example.peachzyapp.entities.GroupChat;

import java.util.List;

public class GroupChatListAdapter extends RecyclerView.Adapter<GroupChatListAdapter.GroupChatListViewHolder>{
    private List<GroupChat> listGroupChats;
    public ImageView ivAvatarGroup;
    private GroupChatListAdapter.OnItemClickListener mListener;


    public interface OnItemClickListener {
        void onItemClick();
    }
    public void setOnItemClickListener(GroupChatListAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public GroupChatListAdapter(List<GroupChat> mListGroupChats) {
        this.listGroupChats = mListGroupChats;
    }


    @NonNull
    @Override
    public GroupChatListAdapter.GroupChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_chat, parent, false);
        return new GroupChatListAdapter.GroupChatListViewHolder(view);
    }
    @Override
    public int getItemCount() {
        if(listGroupChats!=null)
        {
            return listGroupChats.size();
        }
        return 0;
    }
    @Override
    public void onBindViewHolder(@NonNull GroupChatListAdapter.GroupChatListViewHolder holder, int position) {
        GroupChat groupChat =listGroupChats.get(position);
        if(groupChat==null){
            return;
        }
//        holder.tvLastChatGroup.setText(get);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick();
                }
            }
        });

    }



    public class GroupChatListViewHolder extends RecyclerView.ViewHolder {

        public TextView tvNameGroup;
        public TextView tvTimeGroup;
        public TextView tvLastChatGroup;

        public GroupChatListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameGroup= itemView.findViewById(R.id.tvNameGroup);
            tvTimeGroup = itemView.findViewById(R.id.tvTimeGroup);
            tvLastChatGroup=itemView.findViewById(R.id.tvLastChatGroup);
            ivAvatarGroup = itemView.findViewById(R.id.ivAvatarGroup);
        }
    }

}
