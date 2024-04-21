package com.example.peachzyapp.adapters;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.R;
import com.example.peachzyapp.entities.FriendItem;

import java.util.ArrayList;
import java.util.List;

public class CreateGroupChatAdapter extends RecyclerView.Adapter<CreateGroupChatAdapter.CreateGroupChatViewHolder>{
    private List<FriendItem> listFriend;
    private List<FriendItem> selectedFriends = new ArrayList<>();

    // Các phương thức và thuộc tính khác

    // Phương thức để trả về danh sách bạn bè đã chọn
    public List<FriendItem> getSelectedFriends() {
        return selectedFriends;
    }

    public CreateGroupChatAdapter(List<FriendItem> mListFriend) {
        this.listFriend = mListFriend;
    }

    @Override
    public int getItemCount() {
        return listFriend != null ? listFriend.size() : 0;
    }

    @NonNull
    @Override
    public CreateGroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_add_friend_to_group_fragments, parent, false);
        return new CreateGroupChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CreateGroupChatViewHolder holder, int position) {
        FriendItem friend = listFriend.get(position);
        if (friend == null) {
            return;
        }
        holder.tvFriendName.setText(friend.getName());
        Glide.with(holder.itemView.getContext())
                .load(friend.getAvatar())
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(holder.ivFriendAvatar);

        holder.cbAddToGroup.setOnCheckedChangeListener(null); // Tránh lắng nghe sự kiện lặp lại khi recycling view
        holder.cbAddToGroup.setChecked(selectedFriends.contains(friend)); // Kiểm tra nếu friend đã được chọn trước đó

        holder.cbAddToGroup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Nếu checkbox được chọn, thêm đối tượng friend vào danh sách đã chọn
                    selectedFriends.add(new FriendItem(friend.getId(),  friend.getAvatar(),friend.getName()));
                } else {
                    // Nếu checkbox không được chọn, loại bỏ đối tượng friend khỏi danh sách đã chọn
                    selectedFriends.remove(friend);
                }
            }
        });
    }

    public class CreateGroupChatViewHolder extends RecyclerView.ViewHolder {
        public TextView tvFriendName;
        public ImageView ivFriendAvatar;
        public CheckBox cbAddToGroup;

        public CreateGroupChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
            ivFriendAvatar = itemView.findViewById(R.id.ivFriendAvatar);
            cbAddToGroup = itemView.findViewById(R.id.cbAddToGroup);
        }
    }
}
