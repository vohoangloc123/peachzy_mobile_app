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
    public ImageView ivFriendAvatar;
    private List<FriendItem> listFriend;
    private List<String> selectedFriendIds = new ArrayList<>();
    // Các phương thức và thuộc tính khác

    // Phương thức để trả về danh sách ID đã chọn
    public List<String> getSelectedFriendIds() {
        return selectedFriendIds;
    }
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_friend_to_group_fragment, parent, false);
        return new CreateGroupChatAdapter.CreateGroupChatViewHolder(view);
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
                .into(ivFriendAvatar);

        // Gán ID vào tag của checkbox
        holder.cbAddToGroup.setTag(friend.getId());
        holder.cbAddToGroup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Lấy ID từ tag của checkbox
                    String friendId = (String) buttonView.getTag();
                    if (friendId != null) {
                        Log.d("CheckFriendID", friendId);
                        // Thêm ID vào danh sách nếu chưa tồn tại
                        if (!selectedFriendIds.contains(friendId)) {
                            selectedFriendIds.add(friendId);
                        }
                    } else {
                        Log.d("CheckFriendID", "FriendId is null");
                    }
                } else {
                    // Nếu checkbox không được chọn, loại bỏ ID khỏi danh sách
                    String friendId = (String) buttonView.getTag();
                    if (friendId != null) {
                        selectedFriendIds.remove(friendId);
                    }
                }
            }
        });

    }

    public class CreateGroupChatViewHolder extends RecyclerView.ViewHolder {
        public TextView tvFriendName;
        public CheckBox cbAddToGroup;

        public CreateGroupChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tvFriendName);
            ivFriendAvatar = itemView.findViewById(R.id.ivFriendAvatar);
            cbAddToGroup = itemView.findViewById(R.id.cbAddToGroup);

            // Gán ID vào tag của checkbox trong constructor
            cbAddToGroup.setTag(null);
        }
    }

}