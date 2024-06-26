package com.example.peachzyapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.fragments.MainFragments.Users.RequestReceivedFragment;


import java.util.List;
public class RequestReceivedAdapter extends RecyclerView.Adapter<RequestReceivedAdapter.FriendViewHolder>{
    private String test;
    private List<FriendItem> listFriend;
    public ImageView ivAvatar;
    Button btnAccept;
    Button btnCancel;
    private String uid;
    String name;
    Context mContext;
    DynamoDBManager dynamoDBManager;
    RequestReceivedFragment requestReceivedFragment;
    public RequestReceivedAdapter(List<FriendItem> mListFriend) {
        this.listFriend = mListFriend;
    }
    public RequestReceivedAdapter(@NonNull Context context) {
        mContext = context;
    }
    public void setUid(String uid) {
        this.uid = uid;
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
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_request_received, parent, false);
        ivAvatar = view.findViewById(R.id.ivFriendAvatar); // Khởi tạo avatarImageView ở đây
        btnAccept = view.findViewById(R.id.btnAccept);
        btnCancel=view.findViewById(R.id.btnCancel);
        dynamoDBManager = new DynamoDBManager(view.getContext());
        requestReceivedFragment=new RequestReceivedFragment();
        return new FriendViewHolder(view);
    }



//    @Override
//    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
//        FriendItem friendItem = listFriend.get(position); // Lấy đối tượng FriendItem tương ứng với vị trí
//        if (friendItem != null) {
//            String uid = this.uid; // Get uid from instance variable
//            String friendId = friendItem.getId();
//            String avatarUrl = friendItem.getAvatar();
//            name = friendItem.getName();
//            Log.d("TestFriendItem", friendId+ uid);
//            // Load hình ảnh từ URL bằng thư viện Picasso
////            Picasso.get().load(avatarUrl).placeholder(R.drawable.logo).into(ivAvatar);
//            Glide.with(holder.itemView.getContext())
//                    .load(friendItem.getAvatar())
//                    .placeholder(R.drawable.logo)
//                    .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
//                    .into(ivAvatar);
//            btnAccept.setOnClickListener(v -> {
//                Button btnAccept = (Button) holder.itemView.findViewById(R.id.btnAccept);
//                btnAccept.setEnabled(false);
//                btnAccept.setAlpha(0.5f);
//                // Gửi yêu cầu chấp nhận lời mời kết bạn
//                dynamoDBManager.addFriend(uid, friendId, "1",uid+"-"+friendId);
//                dynamoDBManager.addFriend(friendId, uid, "1",uid+"-"+friendId);
//
//                Toast.makeText(v.getContext(), "Đã chấp nhận lời mời kết bạn từ " + name, Toast.LENGTH_SHORT).show();
//            });
//        }
//        holder.tvFriend.setText(name);
//    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendItem friendItem = listFriend.get(position); // Lấy đối tượng FriendItem tương ứng với vị trí
        if (friendItem != null) {
            String uid = this.uid; // Get uid from instance variable
            String friendId = friendItem.getId();
            String avatarUrl = friendItem.getAvatar();
            name = friendItem.getName();
            Log.d("TestFriendItem", friendId+ uid);
            // Load hình ảnh từ URL bằng thư viện Picasso
//            Picasso.get().load(avatarUrl).placeholder(R.drawable.logo).into(ivAvatar);
            Glide.with(holder.itemView.getContext())
                    .load(friendItem.getAvatar())
                    .placeholder(R.drawable.logo)
                    .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                    .into(holder.ivAvatar);
            btnAccept.setOnClickListener(v -> {
                Button btnAccept = (Button) holder.itemView.findViewById(R.id.btnAccept);
//                btnAccept.setEnabled(false);
//                btnAccept.setAlpha(0.5f);
                // Gửi yêu cầu chấp nhận lời mời kết bạn
                dynamoDBManager.addFriend(uid, friendId, "1",uid+"-"+friendId);
                dynamoDBManager.addFriend(friendId, uid, "1",uid+"-"+friendId);

                Toast.makeText(v.getContext(), "Đã chấp nhận lời mời kết bạn từ " + name, Toast.LENGTH_SHORT).show();
                listFriend.remove(friendItem);
                //notifyDataSetChanged();
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, listFriend.size());

            });
            btnCancel.setOnClickListener(v->{
                Button btnCancel = (Button) holder.itemView.findViewById(R.id.btnCancel);
                dynamoDBManager.unFriend(uid, friendId);
                dynamoDBManager.unFriend( friendId,uid);
                listFriend.remove(friendItem);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, listFriend.size());
            });

        }
        holder.tvFriend.setText(name);

    }



    public class FriendViewHolder extends RecyclerView.ViewHolder{
        public TextView tvFriend;
        public ImageView ivAvatar;
        // public ImageView avatarImageView;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriend= itemView.findViewById(R.id.tvFriend);
            ivAvatar = itemView.findViewById(R.id.ivFriendAvatar);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnCancel=itemView.findViewById(R.id.btnCancel);
        }
    }


}
