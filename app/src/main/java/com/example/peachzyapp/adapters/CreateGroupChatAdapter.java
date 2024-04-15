package com.example.peachzyapp.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.R;
import com.example.peachzyapp.entities.FriendItem;

import java.util.List;

public class CreateGroupChatAdapter extends RecyclerView.Adapter<CreateGroupChatAdapter.CreateGroupChatViewHolder>{
    public ImageView ivFriendAvatar;
    private List<FriendItem> listFriend;

    public CreateGroupChatAdapter(List<FriendItem> mListFriend) {
        this.listFriend = mListFriend;
    }

    @Override
    public int getItemCount() {
        if(listFriend!=null)
        {
            return listFriend.size();
        }
        return 0;
    }
    @NonNull
    @Override
    public CreateGroupChatAdapter.CreateGroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_add_friend_to_group_fragments, parent, false);
        return new CreateGroupChatAdapter.CreateGroupChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CreateGroupChatAdapter.CreateGroupChatViewHolder holder, int position) {
        FriendItem friends= listFriend.get(position);
        if(friends==null){
            return;
        }
        holder.tvFriendName.setText(friends.getName());
        Glide.with(holder.itemView.getContext())
                .load(friends.getAvatar())
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(ivFriendAvatar);
    }



    public class CreateGroupChatViewHolder extends RecyclerView.ViewHolder {
        public TextView tvFriendName;

        public CreateGroupChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendName= itemView.findViewById(R.id.tvFriendName);
            ivFriendAvatar = itemView.findViewById(R.id.ivFriendAvatar);

        }
    }
}
