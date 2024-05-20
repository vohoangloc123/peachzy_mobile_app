package com.example.peachzyapp.adapters;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.fragments.MainFragments.Users.RequestSendFragment;

import java.util.List;

public class RequestSentAdapter extends RecyclerView.Adapter<RequestSentAdapter.FriendViewHolder>{
    private List<FriendItem> listFriend;
    public ImageView avatarImageView;
    private DynamoDBManager dynamoDBManager;
    private RequestSendFragment requestSendFragment;

    private Button btnRemoveRequest;

    private String uid;

    public void setUid(String uid) {
        this.uid = uid;
    }

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

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_sent, parent, false);
        btnRemoveRequest = view.findViewById(R.id.btnRemoveRequest);
        dynamoDBManager = new DynamoDBManager(view.getContext());
        requestSendFragment = new RequestSendFragment();
        return new RequestSentAdapter.FriendViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull RequestSentAdapter.FriendViewHolder holder, int position) {
        FriendItem friends= listFriend.get(position);
        if(friends!=null){

            String uid = this.uid; // Get uid from instance variable
            String friendId = friends.getId();
            btnRemoveRequest.setOnClickListener(v->{
                Log.d("onBindViewHolder358", uid+":"+friendId);
                Button btnRemoveRequest = (Button) holder.itemView.findViewById(R.id.btnCancel);
                dynamoDBManager.unFriend(uid, friendId);
                dynamoDBManager.unFriend( friendId,uid);
                listFriend.remove(friends);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, listFriend.size());
            });

        }
        holder.tvFriend.setText(friends.getName());
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
            avatarImageView = itemView.findViewById(R.id.ivFriendAvatar);
        }
    }
}
