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
import com.example.peachzyapp.entities.GroupChat;
import com.example.peachzyapp.entities.GroupConversation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GroupChatListAdapter extends RecyclerView.Adapter<GroupChatListAdapter.GroupChatListViewHolder>{
    private List<GroupConversation> listGroupChats;
    public ImageView ivAvatarGroup;
    private GroupChatListAdapter.OnItemClickListener mListener;


    public interface OnItemClickListener {
        void onItemClick(String id, String groupName, String avatar);
    }
    public void setOnItemClickListener(GroupChatListAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public GroupChatListAdapter(List<GroupConversation> mListGroupChats) {
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
        GroupConversation groupChat =listGroupChats.get(position);
        if(groupChat==null){
            return;
        }
        holder.tvNameGroup.setText(groupChat.getGroupName());
        holder.tvLastChatGroup.setText(groupChat.getName()+": "+groupChat.getMessage());
        String trimmedTime = groupChat.getTime().trim();
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
        try {
            // Parse chuỗi thời gian từ chuỗi đã loại bỏ khoảng trắng
            Date date = inputFormat.parse(trimmedTime);

            // Format lại thành chuỗi chỉ chứa giờ và phút
            String timeOnly = outputFormat.format(date);

            // Hiển thị chuỗi giờ và phút trong TextView
            holder.tvTimeGroup.setText(timeOnly);
        } catch (ParseException e) {
            // Xử lý nếu có lỗi xảy ra khi parse chuỗi thời gian
            e.printStackTrace();
        }

        Glide.with(holder.itemView.getContext())
                .load(groupChat.getAvatar())
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(ivAvatarGroup);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(groupChat.getGroupConversationID(), groupChat.getGroupName(), groupChat.getAvatar());
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
