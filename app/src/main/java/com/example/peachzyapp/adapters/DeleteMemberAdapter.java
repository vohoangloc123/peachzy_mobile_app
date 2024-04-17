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

public class DeleteMemberAdapter extends RecyclerView.Adapter<DeleteMemberAdapter.DeleteMemberViewHolder>{
    private List<FriendItem> listMember;
    private List<String> selectedMemberIds = new ArrayList<>();

    public DeleteMemberAdapter(List<FriendItem> listMember) {
        this.listMember = listMember;
    }

    @NonNull
    @Override
    public DeleteMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delete_member, parent, false);
        return new DeleteMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeleteMemberViewHolder holder, int position) {
        FriendItem member = listMember.get(position);
        holder.tvMemberName.setText(member.getName());
        Glide.with(holder.itemView.getContext())
                .load(member.getAvatar())
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(holder.ivMemberAvatar);

        holder.cbDeleteMember.setTag(member.getId());
        holder.cbDeleteMember.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String memberId = (String) buttonView.getTag();
            if (isChecked) {
                if (memberId != null && !selectedMemberIds.contains(memberId)) {
                    selectedMemberIds.add(memberId);
//                    Log.d("CheckIdInMember", memberId);
                }
            } else {
                selectedMemberIds.remove(memberId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listMember.size();
    }

    public List<String> getSelectedMemberIds() {
        return selectedMemberIds;
    }

    public static class DeleteMemberViewHolder extends RecyclerView.ViewHolder {
        public TextView tvMemberName;
        public CheckBox cbDeleteMember;
        public ImageView ivMemberAvatar;

        public DeleteMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            cbDeleteMember = itemView.findViewById(R.id.cbDeleteMember);
            ivMemberAvatar = itemView.findViewById(R.id.ivMemberAvatar);
        }
    }
}
