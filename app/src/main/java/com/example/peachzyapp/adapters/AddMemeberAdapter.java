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

public class AddMemeberAdapter extends RecyclerView.Adapter<AddMemeberAdapter.AddMembersViewHolder>{
    private List<FriendItem> listFriend;
    public ImageView ivFriendAvatarAddMembers;
    private List<String> selectedMemberIds = new ArrayList<>();
    public List<String> getSelectedMemberIds() {
        return selectedMemberIds;
    }

    public AddMemeberAdapter(List<FriendItem> mListFriend) {
        this.listFriend = mListFriend;
    }

    @NonNull
    @Override
    public AddMembersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_member, parent, false);
        return new AddMembersViewHolder(view);
    }


    @Override
    public int getItemCount() {
        if(listFriend!=null)
        {
            return listFriend.size();
        }
        return 0;
    }
    @Override
    public void onBindViewHolder(@NonNull AddMembersViewHolder holder, int position) {
        FriendItem friends = listFriend.get(position);
        if (friends == null) {
            return;
        }
        holder.tvFriendNameAddMember.setText(friends.getName());
//        Glide.with(holder.itemView.getContext())
//                .load(friends.getAvatar())
//                .placeholder(R.drawable.logo)
//                .transform(new MultiTransformation<>(new CircleCrop()))
//                .into(ivFriendAvatarAddMembers);

        // Gán ID vào tag của checkbox
        holder.cbAddMemberToGroup.setTag(friends.getId());

        holder.cbAddMemberToGroup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // Lấy ID từ tag của checkbox
                    String friendId = (String) buttonView.getTag();
                    if (friendId != null) {
                        Log.d("CheckFriendID", friendId);
                        // Thêm ID vào danh sách nếu chưa tồn tại
                        if (!selectedMemberIds.contains(friendId)) {
                            selectedMemberIds.add(friendId);
                        }
                    } else {
                        Log.d("CheckFriendID", "FriendId is null");
                    }
                } else {
                    // Nếu checkbox không được chọn, loại bỏ ID khỏi danh sách
                    String friendId = (String) buttonView.getTag();
                    if (friendId != null) {
                        selectedMemberIds.remove(friendId);
                    }
                }
            }
        });

    }
    public class AddMembersViewHolder extends RecyclerView.ViewHolder {
        public TextView tvFriendNameAddMember;
        public CheckBox cbAddMemberToGroup;
        public AddMembersViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendNameAddMember = itemView.findViewById(R.id.tvMemberName);
            cbAddMemberToGroup = itemView.findViewById(R.id.cbAddMember);
            ivFriendAvatarAddMembers = itemView.findViewById(R.id.ivMemberAvatar);
        }
    }
}
