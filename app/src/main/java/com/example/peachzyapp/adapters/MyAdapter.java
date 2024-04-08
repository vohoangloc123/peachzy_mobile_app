package com.example.peachzyapp.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.R;
import com.example.peachzyapp.entities.Item;

public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private String test;
    Context context;
    List<Item> items;

    public MyAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }
    public void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged(); // Cập nhật giao diện khi dữ liệu thay đổi
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.item_view,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull  MyViewHolder holder, int position) {
//        holder.tvTime.setText(items.get(position).getTime());
//        holder.tvMessage.setText(items.get(position).getMessage());
        Item currentItem = items.get(position);

        // Kiểm tra xem tin nhắn có phải của người gửi hay không
        boolean isSentByMe = currentItem.isSentByMe(); // Bạn cần cung cấp một cách nào đó để xác định tin nhắn của người gửi
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.tvMessage.getLayoutParams();
        // Hiển thị tin nhắn với màu nền tương ứng
        if (isSentByMe) {
            // Tin nhắn của người gửi (bên phải)
//            holder.itemView.setBackgroundResource(R.color.sentColor); // Đặt background cho tin nhắn của người gửi
            holder.tvMessage.setTextColor(context.getColor(R.color.white)); // Đặt màu cho nội dung tin nhắn của người gửi
            holder.tvMessage.setBackgroundColor(ContextCompat.getColor(context, R.color.sentColor));
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
        } else {
            // Tin nhắn của người nhận (bên trái)
//            holder.itemView.setBackgroundResource(R.color.bgGrey); // Đặt background cho tin nhắn của người nhận
            holder.tvMessage.setTextColor(context.getColor(R.color.black)); // Đặt màu cho nội dung tin nhắn của người nhận
        }

        // Đặt dữ liệu cho các TextView trong ViewHolder
        holder.tvTime.setText(currentItem.getTime());
        holder.tvMessage.setText(currentItem.getMessage());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
