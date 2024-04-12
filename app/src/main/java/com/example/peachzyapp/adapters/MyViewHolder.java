package com.example.peachzyapp.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.R;


public class MyViewHolder extends RecyclerView.ViewHolder {

    ImageView ivAvatar;
    TextView tvTime, tvMessage, tvLink;
    ImageView ivMessage; // Thêm ImageView mới để hiển thị ảnh trong tin nhắn (nếu có)

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        tvTime = itemView.findViewById(R.id.tvTime);
        tvMessage = itemView.findViewById(R.id.tvMessage);
        tvLink = itemView.findViewById(R.id.tvLink);
        ivAvatar = itemView.findViewById(R.id.ivAvatar);
        ivMessage = itemView.findViewById(R.id.ivMessage); // Ánh xạ ImageView mới
    }
}