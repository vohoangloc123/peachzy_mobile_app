package com.example.peachzyapp.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.R;
import com.example.peachzyapp.entities.GroupChat;
import com.example.peachzyapp.entities.Item;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GroupChatBoxAdapter extends RecyclerView.Adapter<GroupChatViewHolder>{
    Context context;
    List<GroupChat> groupChatItems;
    String userID; // Thuộc tính mới để lưu uid
    public GroupChatBoxAdapter(Context context, List<GroupChat> groupChatItems, String userID) {
        this.context = context;
        this.groupChatItems = groupChatItems;
        this.userID = userID; // Khởi tạo uid
    }
    public void setItems(List<GroupChat> groupChatItems) {
        this.groupChatItems = groupChatItems;
        notifyDataSetChanged(); // Cập nhật giao diện khi dữ liệu thay đổi
    }

    @NonNull
    @Override
    public GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_group_chat_view, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroupChatViewHolder holder, int position) {
        GroupChat currentItem = groupChatItems.get(position);
        holder.tvGroupTime.setText(currentItem.getTime());
        Glide.with(holder.itemView.getContext())
                .load(currentItem.getAvatar())
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(holder.ivGroupAvatar);
        holder.tvUserName.setText(currentItem.getName());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.tvGroupMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfImage = (RelativeLayout.LayoutParams) holder.ivGroupMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfFile = (RelativeLayout.LayoutParams) holder.tvGroupLink.getLayoutParams();
        if (currentItem.getUserID().equals(userID)) {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            paramsOfImage.addRule(RelativeLayout.ALIGN_PARENT_END);
            paramsOfFile.addRule(RelativeLayout.ALIGN_PARENT_END);
            holder.tvGroupMessage.setTextColor(context.getColor(R.color.white));
            holder.tvGroupMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_message));
            holder.ivGroupAvatar.setVisibility(View.GONE);
            holder.tvGroupLink.setVisibility(View.GONE);
            holder.tvUserName.setVisibility(View.GONE);
            // Nếu tin nhắn chứa đường dẫn của hình ảnh từ S3
            if (isS3ImageUrl(currentItem.getMessage())) {

                holder.tvGroupMessage.setVisibility(View.GONE);
                holder.tvGroupLink.setVisibility(View.GONE);
                holder.ivGroupMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                Picasso.get().load(currentItem.getMessage()).into(holder.ivGroupMessage);
            } else if(isS3Document(currentItem.getMessage())){
                // Hiển thị văn bản tin nhắn
                holder.tvGroupMessage.setVisibility(View.GONE);
                holder.tvGroupLink.setVisibility(View.VISIBLE);
                holder.ivGroupMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                Picasso.get().load(R.drawable.filepicture).into(holder.ivGroupMessage);
                holder.tvGroupLink.setText(currentItem.getMessage());
            }
            else {
                holder.tvGroupMessage.setText(currentItem.getMessage());
                holder.ivGroupMessage.setVisibility(View.GONE);
                holder.tvGroupLink.setVisibility(View.GONE);
                holder.tvGroupMessage.setVisibility(View.VISIBLE);
            }
        } else if(!currentItem.getUserID().equals(userID)){ // Nếu tin nhắn là của người nhận
            holder.tvGroupMessage.setTextColor(context.getColor(R.color.black));

            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsOfImage.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsOfFile.addRule(RelativeLayout.ALIGN_PARENT_START);
            // Nếu tin nhắn chứa đường dẫn của hình ảnh từ S3
            if (isS3ImageUrl(currentItem.getMessage())) {
                Picasso.get().load(currentItem.getMessage()).into(holder.ivGroupMessage);
                holder.ivGroupMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                holder.tvGroupMessage.setVisibility(View.GONE);
                holder.tvGroupLink.setVisibility(View.GONE);
            }else if(isS3Document(currentItem.getMessage())){
                holder.tvGroupMessage.setVisibility(View.GONE);
                holder.tvGroupLink.setVisibility(View.VISIBLE);
                holder.ivGroupMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                Picasso.get().load(R.drawable.filepicture).into(holder.ivGroupMessage);
                holder.tvGroupLink.setText(currentItem.getMessage());
            }else {
                // Hiển thị văn bản tin nhắn
                holder.ivGroupMessage.setVisibility(View.GONE);
                holder.tvGroupMessage.setVisibility(View.VISIBLE);
                holder.tvGroupLink.setVisibility(View.GONE);
                holder.tvGroupMessage.setText(currentItem.getMessage());
            }
 }
    }

    @Override
    public int getItemCount() {
        return groupChatItems.size();
    }
    private boolean isS3ImageUrl(String url) {
        return url != null && url.startsWith("https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/");
    }
    private boolean isS3Document(String url) {
        return url != null && url.startsWith("https://chat-app-document-cnm.s3.ap-southeast-1.amazonaws.com/");
    }
}
