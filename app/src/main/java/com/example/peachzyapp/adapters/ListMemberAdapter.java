package com.example.peachzyapp.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;

import java.util.List;

public class ListMemberAdapter extends RecyclerView.Adapter<ListMemberAdapter.ListMemberViewHolder> {
    private List<FriendItem> listMember;
    private static final String TAG = "ListMemberAdapter";
    private ImageButton btnViewProfile;
    private String groupID;
    private String uid;
    private DynamoDBManager dynamoDBManager;
    private MainActivity mainActivity;
    public ListMemberAdapter(List<FriendItem> listMember, String groupID, String uid, DynamoDBManager dynamoDBManager, MainActivity mainActivity) {
        this.listMember = listMember;
        this.groupID = groupID;
        this.uid = uid;
        this.dynamoDBManager = dynamoDBManager;
        this.mainActivity=mainActivity;
    }

    @NonNull
    @Override
    public ListMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_member, parent, false);
        return new ListMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListMemberViewHolder holder, int position) {
        FriendItem member = listMember.get(position);
        holder.bind(member, position);
        holder.setDynamoDBManager(dynamoDBManager);
        holder.setFriendUid(member.getId());
        holder.setMainActivity(mainActivity);
    }

    @Override
    public int getItemCount() {
        return listMember.size();
    }


    public class ListMemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvMemberName;
        public TextView tvRole;
        public ImageView ivMemberAvatar;
        private int position;
        private DynamoDBManager dynamoDBManager;
        private MainActivity mainActivity;
        private Context context;
        private String friendID;
        public void setFriendUid(String friendID) {
            this.friendID= friendID;
        }
        public ListMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvRole = itemView.findViewById(R.id.tvRole);
            ivMemberAvatar = itemView.findViewById(R.id.ivMemberAvatar);
            btnViewProfile = itemView.findViewById(R.id.btnViewProfile);
            btnViewProfile.setOnClickListener(this);
        }

        public void bind(FriendItem member, int position) {
            this.position = position;
            tvMemberName.setText(member.getName());
            tvRole.setText(member.getRole());
            Glide.with(itemView.getContext())
                    .load(member.getAvatar())
                    .placeholder(R.drawable.logo)
                    .transform(new MultiTransformation<>(new CircleCrop()))
                    .into(ivMemberAvatar);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: " + position+friendID);
            Bundle bundle=new Bundle();
            bundle.putString("friendID", friendID);
            mainActivity.goToViewProfileFragment(bundle);
        }
        public void setDynamoDBManager(DynamoDBManager dynamoDBManager) {
            this.dynamoDBManager = dynamoDBManager;
        }
        public void setActivity(Context context)
        {
            this.context=context;
        }
        public void setMainActivity(MainActivity mainActivity) {
            this.mainActivity=mainActivity;
        }
    }
}
