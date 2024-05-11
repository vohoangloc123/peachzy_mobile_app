package com.example.peachzyapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.LiveData.MyViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.fragments.MainFragments.Users.FriendsFragment;

import java.util.List;

public class FriendAlreadyAdapter extends RecyclerView.Adapter<FriendAlreadyAdapter.FriendViewHolder>{
    private List<FriendItem> listFriend;
    public ImageView avatarImageView;
    private OnItemClickListener mListener;
    private static final String TAG = "FriendAlreadyAdapter";
    private Context context;
    private String uid;
    private DynamoDBManager dynamoDBManager;
    private MainActivity mainActivity;
    private FragmentManager fragmentManager;



    public interface OnItemClickListener {
        void onItemClick(String id , String urlAvatar, String friendName);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    public FriendAlreadyAdapter(List<FriendItem> mListFriend, Context context,  DynamoDBManager dynamoDBManager, MainActivity mainActivity, FragmentManager fragmentManager,String uid) {
        this.listFriend = mListFriend;
        this.context=context;
        this.dynamoDBManager=dynamoDBManager;
        this.mainActivity=mainActivity;
        this.fragmentManager=fragmentManager;
        this.uid=uid;
    }

    @Override
    public int getItemCount() {
        if(listFriend!=null)
        {
            return listFriend.size();
        }
        return 0;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendItem friends= listFriend.get(position);
        if(friends==null){
            return;
        }

        holder.tvFriend.setText(friends.getName());
        Glide.with(holder.itemView.getContext())
                .load(friends.getAvatar())
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(holder.avatarImageView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClick(friends.getId(),friends.getAvatar(), friends.getName());
                }
            }
        });
        holder.setFriendUid(friends.getId());
        holder.setDynamoDBManager(dynamoDBManager);
        holder.setMainActivity(mainActivity);
    }

    public class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public ImageView avatarImageView;
        public TextView tvFriend;
        public ImageButton btnMore;
        private int position;
        private DynamoDBManager dynamoDBManager;
        private MainActivity mainActivity;
        private Context context;
        private String friendID; // Thêm trường uid
      

        public void setFriendUid(String friendID) {
            this.friendID= friendID;
        }


        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriend= itemView.findViewById(R.id.tv_friend);
            avatarImageView = itemView.findViewById(R.id.ivFriendAvatar);
            btnMore = itemView.findViewById(R.id.btnMore);
            btnMore.setOnClickListener(this);
        }
        private void showPopupMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.popup_listfriend_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                // Xử lý sự kiện khi chọn một item trên popup menu
                int itemId = item.getItemId();
                if (itemId == R.id.action_popup_unfriend) {



                    // Xử lý khi chọn Delete item ở đây
                    Log.d(TAG, "ProfileUnfriend: "+"my uid: "+uid+" friendID: "+friendID);
                    dynamoDBManager.deleteAFriendFromUser(uid, friendID);
                    dynamoDBManager.deleteAFriendFromUser(friendID, uid);
                } else if (itemId == R.id.action_popup_view_profile) {
                    Log.d(TAG, "ViewProfile: "+uid);
                    //gọi hàm dynamoDB lấy dữ liệu ng dùng friendID
                    Bundle bundle=new Bundle();
                    bundle.putString("friendID",friendID);
                    mainActivity.goToViewProfileFragment(bundle);
                }
                return true;
            });
            popupMenu.show();
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "onClick: " + position);
            showPopupMenu(v);
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