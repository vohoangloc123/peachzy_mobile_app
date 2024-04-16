package com.example.peachzyapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.R;
import com.example.peachzyapp.entities.Item;
import com.squareup.picasso.Picasso;

public class ChatBoxAdapter extends RecyclerView.Adapter<ChatViewHolder> {

    Context context;
    List<Item> items;

    public ChatBoxAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }
    public void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged(); // Cập nhật giao diện khi dữ liệu thay đổi
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Item currentItem = items.get(position);

        // Kiểm tra xem tin nhắn có phải của người gửi hay không
        boolean isSentByMe = currentItem.isSentByMe();

        // Hiển thị avatar
//        Picasso.get().load(currentItem.getAvatar()).into(holder.ivAvatar);
        Glide.with(holder.itemView.getContext())
                .load(currentItem.getAvatar())
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(holder.ivAvatar);

        // Hiển thị thời gian
        holder.tvTime.setText(currentItem.getTime());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.tvMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfImage = (RelativeLayout.LayoutParams) holder.ivMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfFile = (RelativeLayout.LayoutParams) holder.tvLink.getLayoutParams();
        // Nếu tin nhắn là của người gửi
        if (isSentByMe) {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            paramsOfImage.addRule(RelativeLayout.ALIGN_PARENT_END);
            paramsOfFile.addRule(RelativeLayout.ALIGN_PARENT_END);
            holder.tvMessage.setTextColor(context.getColor(R.color.white));
//            holder.tvMessage.setBackgroundColor(ContextCompat.getColor(context, R.color.sentColor));
            holder.tvMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_message));
            holder.ivAvatar.setVisibility(View.GONE);
            holder.tvLink.setVisibility(View.GONE);
            // Nếu tin nhắn chứa đường dẫn của hình ảnh từ S3
            if (isS3ImageUrl(currentItem.getMessage())) {

                holder.tvMessage.setVisibility(View.GONE);
                holder.tvLink.setVisibility(View.GONE);
                holder.ivMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                Picasso.get().load(currentItem.getMessage()).into(holder.ivMessage);
            } else if(isS3Document(currentItem.getMessage())){
                // Hiển thị văn bản tin nhắn
                holder.tvMessage.setVisibility(View.GONE);
                holder.tvLink.setVisibility(View.VISIBLE);
                holder.ivMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                Picasso.get().load(R.drawable.filepicture).into(holder.ivMessage);
                holder.tvLink.setText(currentItem.getMessage());
            }
            else {
                holder.tvMessage.setText(currentItem.getMessage());
                holder.ivMessage.setVisibility(View.GONE);
                holder.tvLink.setVisibility(View.GONE);
                holder.tvMessage.setVisibility(View.VISIBLE);
            }
        } else if(!isSentByMe) { // Nếu tin nhắn là của người nhận
            holder.tvMessage.setTextColor(context.getColor(R.color.black));
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsOfImage.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsOfFile.addRule(RelativeLayout.ALIGN_PARENT_START);
            // Nếu tin nhắn chứa đường dẫn của hình ảnh từ S3
            if (isS3ImageUrl(currentItem.getMessage())) {
                Picasso.get().load(currentItem.getMessage()).into(holder.ivMessage);
                holder.ivMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                holder.tvMessage.setVisibility(View.GONE);
                holder.tvLink.setVisibility(View.GONE);
            }else if(isS3Document(currentItem.getMessage())){

                holder.tvMessage.setVisibility(View.GONE);
                holder.tvLink.setVisibility(View.VISIBLE);
                holder.ivMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                Picasso.get().load(R.drawable.filepicture).into(holder.ivMessage);
                holder.tvLink.setText(currentItem.getMessage());
            }else {
                // Hiển thị văn bản tin nhắn
                holder.ivMessage.setVisibility(View.GONE);
                holder.tvMessage.setVisibility(View.VISIBLE);
                holder.tvLink.setVisibility(View.GONE);
                holder.tvMessage.setText(currentItem.getMessage());
            }
        }
    }

    private boolean isS3ImageUrl(String url) {
        return url != null && url.startsWith("https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/");
    }
    private boolean isS3Document(String url) {
        return url != null && url.startsWith("https://chat-app-document-cnm.s3.ap-southeast-1.amazonaws.com/");
    }
    @Override
    public int getItemCount() {
        return items.size();
    }
}
