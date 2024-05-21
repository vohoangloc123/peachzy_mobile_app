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

public class GroupChatViewHolder  extends RecyclerView.ViewHolder{
    ImageView ivGroupAvatar;
    TextView tvGroupTime, tvGroupMessage, tvGroupLink, tvUserName;
    ImageView ivGroupMessage; // Thêm ImageView mới để hiển thị ảnh trong tin nhắn (nếu có)
    VideoView vvGroupMessage;
    SeekBar seekBar;
    public GroupChatViewHolder(@NonNull View itemView) {
        super(itemView);
        tvGroupTime = itemView.findViewById(R.id.tvGroupTime);
        tvGroupMessage = itemView.findViewById(R.id.tvGroupMessage);
        tvGroupLink = itemView.findViewById(R.id.tvGroupLink);
        ivGroupAvatar= itemView.findViewById(R.id.ivGroupAvatar);
        ivGroupMessage = itemView.findViewById(R.id.ivGroupMessage); // Ánh xạ ImageView mới
        tvUserName=itemView.findViewById(R.id.tvUserName);
        vvGroupMessage = itemView.findViewById(R.id.vvGroupMessage);
        seekBar = itemView.findViewById(R.id.seekBar);
    }
}
