package com.example.peachzyapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.R;
import com.example.peachzyapp.entities.ChatBox;

import java.util.List;

public class ChatBoxAdapter extends RecyclerView.Adapter<ChatBoxAdapter.ChatBoxHolder> {
    private List<ChatBox> listChatBox;
    private IClickItemListener iClickItemListener;
    public interface IClickItemListener{
        void onClickItemChatBox(ChatBox chatBox);
    }
    public ChatBoxAdapter(List<ChatBox> listChatBox, IClickItemListener listener)
    {
        this.listChatBox=listChatBox;
        this.iClickItemListener=listener;
    }

    public ChatBoxAdapter(List<ChatBox> listChatBox) {
        this.listChatBox=listChatBox;
    }

    @NonNull
    @Override
    public ChatBoxHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatbox, parent, false);
        return new ChatBoxHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatBoxHolder holder, int position) {
        final ChatBox chatBox=listChatBox.get(position);
        //nếu null k làm gì cả nếu not null thì lấy dữ liệu
        if(chatBox==null)
        {
            return;
        }
        //bắt sự kiện
        holder.tvName.setText(chatBox.getName());
        holder.tvName.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                iClickItemListener.onClickItemChatBox(chatBox);
            }
        });
    }

    @Override
    public int getItemCount() {
        if(listChatBox!=null)
        {
            return listChatBox.size();
        }
        return 0;
    }

    public class ChatBoxHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        public ChatBoxHolder(@NonNull View itemView) {
            super(itemView);
            tvName=itemView.findViewById(R.id.tv_name);
        }
    }
}
