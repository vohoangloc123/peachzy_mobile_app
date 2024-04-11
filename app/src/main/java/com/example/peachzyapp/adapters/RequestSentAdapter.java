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
import com.squareup.picasso.Picasso;

import java.util.List;

public class RequestSentAdapter extends RecyclerView.Adapter<RequestSentAdapter.FriendViewHolder>{
    private List<FriendItem> listFriend;
    public ImageView avatarImageView;

    public RequestSentAdapter(List<FriendItem> mListFriend) {
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
    public RequestSentAdapter.FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_request_sent_adapter, parent, false);
        return new RequestSentAdapter.FriendViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull RequestSentAdapter.FriendViewHolder holder, int position) {
        FriendItem friends= listFriend.get(position);
        if(friends==null){
            return;
        }
        holder.tvFriend.setText(friends.getName());
//        Picasso.get().load(friends.getAvatar()).into(avatarImageView);
        Glide.with(holder.itemView.getContext())
                .load(friends.getAvatar())
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(avatarImageView);

    }


    public class FriendViewHolder extends RecyclerView.ViewHolder{
        public TextView tvFriend;


        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriend= itemView.findViewById(R.id.tv_friend);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
        }
    }
}
