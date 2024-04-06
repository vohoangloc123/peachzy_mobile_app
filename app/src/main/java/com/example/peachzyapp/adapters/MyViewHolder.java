package com.example.peachzyapp.adapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.R;

public class MyViewHolder extends RecyclerView.ViewHolder {

    ImageView imageView;
    TextView tvTime, tvMessage;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        tvTime = itemView.findViewById(R.id.tvTime);
        tvMessage = itemView.findViewById(R.id.tvMessage);
    }
}