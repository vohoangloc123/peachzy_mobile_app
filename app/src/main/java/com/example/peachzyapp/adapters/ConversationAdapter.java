package com.example.peachzyapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.peachzyapp.R;
import com.example.peachzyapp.entities.Conversation;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationViewHolder>{
    private List<Conversation> listConversation;

    private ConversationAdapter.OnItemClickListener mListener;
    public interface OnItemClickListener {
        void onItemClick(String id);
    }
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
        holder.tvTime.setText(conversation.getTime());
        holder.tvName.setText(conversation.getName());
        Picasso.get().load(conversation.getAvatar()).into(holder.ivAvatar);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(conversation.getConversationID());
                }
            }
        });
    }



    public class ConversationViewHolder extends RecyclerView.ViewHolder{
        public ImageView ivAvatar;
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
