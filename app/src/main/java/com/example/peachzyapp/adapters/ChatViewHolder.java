package com.example.peachzyapp.adapters;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.R;


public class ChatViewHolder extends RecyclerView.ViewHolder {

    ImageView ivAvatar;
    TextView tvTime, tvMessage, tvLink;
    ImageView ivMessage; // Thêm ImageView mới để hiển thị ảnh trong tin nhắn (nếu có)
    SeekBar seekBar;
    VideoView vvMessage;
    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);
        tvTime = itemView.findViewById(R.id.tvTime);
        tvMessage = itemView.findViewById(R.id.tvMessage);
        tvLink = itemView.findViewById(R.id.tvLink);
        ivAvatar = itemView.findViewById(R.id.ivAvatar);
        ivMessage = itemView.findViewById(R.id.ivMessage); // Ánh xạ ImageView mới
        seekBar = itemView.findViewById(R.id.seekBar);
        vvMessage = itemView.findViewById(R.id.vvMessage);
        seekBar = itemView.findViewById(R.id.seekBar);
    }
}