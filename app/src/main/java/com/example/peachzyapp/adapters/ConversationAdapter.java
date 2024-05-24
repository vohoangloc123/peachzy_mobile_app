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
import com.example.peachzyapp.entities.Conversation;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>{
    private List<Conversation> listConversation;
    public ImageView ivAvatar;
    private ConversationAdapter.OnItemClickListener mListener;
    public interface OnItemClickListener {
        void onItemClick(String id, String avatar, String friendName);
    }//
    public void setOnItemClickListener(ConversationAdapter.OnItemClickListener listener) {
        mListener = listener;
    }

    public ConversationAdapter(List<Conversation> mListConversation) {
        this.listConversation = mListConversation;
    }

    @Override
    public int getItemCount() {
        if(listConversation !=null)
        {
            return listConversation.size();
        }
        return 0;
    }

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationAdapter.ConversationViewHolder holder, int position) {
        Conversation conversation= listConversation.get(position);
        if(conversation==null){
            return;
        }
        holder.tvMessage.setText(conversation.getMessage());
        String trimmedTime = conversation.getTime().trim();

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");

        try {
            // Parse chuỗi thời gian từ chuỗi đã loại bỏ khoảng trắng
            Date date = inputFormat.parse(trimmedTime);

            // Format lại thành chuỗi chỉ chứa giờ và phút
            String timeOnly = outputFormat.format(date);

            // Hiển thị chuỗi giờ và phút trong TextView
            holder.tvTime.setText(timeOnly);
        } catch (ParseException e) {
            // Xử lý nếu có lỗi xảy ra khi parse chuỗi thời gian
            e.printStackTrace();
        }
        holder.tvName.setText(conversation.getName());
//        Picasso.get().load(conversation.getAvatar()).into(ivAvatar);
        Glide.with(holder.itemView.getContext())
                .load(conversation.getAvatar())
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(ivAvatar);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(conversation.getFriendID(), conversation.getAvatar(), conversation.getName());
                }
            }
        });
    }



    public class ConversationViewHolder extends RecyclerView.ViewHolder{

        public TextView tvMessage;
        public TextView tvTime;
        public TextView tvName;
        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage= itemView.findViewById(R.id.tvLastchat);
            tvTime= itemView.findViewById(R.id.tvTime);
            tvName= itemView.findViewById(R.id.tvName);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
        }
    }
}
