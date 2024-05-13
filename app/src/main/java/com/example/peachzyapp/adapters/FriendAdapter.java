package com.example.peachzyapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;

import java.util.ArrayList;

public class FriendAdapter extends ArrayAdapter<FriendItem> {
    private String test;
    DynamoDBManager dynamoDBManager;
    private Context mContext;
    private int mResource;
    private String uid; // Biến để lưu trữ uid
    String avatarUrl;
    public FriendAdapter(@NonNull Context context, int resource, @NonNull ArrayList<FriendItem> objects) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(mResource, parent, false);
        }
        ImageView avatarImageView = convertView.findViewById(R.id.ivFriendAvatar);
        TextView nameTextView = convertView.findViewById(R.id.tvFriendName);
        Button addFriendButton = convertView.findViewById(R.id.addFriendButton);
        FriendItem friendItem = getItem(position); // Lấy đối tượng FriendItem tương ứng với vị trí
        dynamoDBManager=new DynamoDBManager(getContext());
        if (friendItem != null) {
            String uid = this.uid; // Get uid from instance variable
            String avatarUrl = friendItem.getAvatar();
            String name = friendItem.getName();
            String friendId=friendItem.getId();
            Glide.with(getContext())
                    .load(avatarUrl)
                    .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                    .into(avatarImageView);
            nameTextView.setText(name);

            //Xử lý xem đã có trong danh sách hay chưa
            dynamoDBManager.checkAlreadyFriend(uid, friendId, new DynamoDBManager.CheckAlreadyFriendListener() {
                @Override
                public void onCheckComplete(boolean isFriend) {
                    if(isFriend){
                        ((Activity)mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addFriendButton.setEnabled(false);
                                addFriendButton.setAlpha(0.5f);
                            }
                        });
                    }
                    else {
                        ((Activity)mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addFriendButton.setEnabled(true);
                                addFriendButton.setAlpha(1f);
                            }
                        });
                    }
                }
            });

            // Xử lý sự kiện khi nút được nhấn
            addFriendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addFriendButton.setEnabled(false);
                    addFriendButton.setAlpha(0.5f);
                    dynamoDBManager.addFriend(uid, friendId, "2",uid+"-"+friendId);
                    dynamoDBManager.addFriend(friendId, uid, "3",uid+"-"+friendId);
                    Toast.makeText(mContext, "Add friend button clicked for " + name, Toast.LENGTH_SHORT).show();

                    remove(friendItem);
                    notifyDataSetChanged();
                }
            });


        }

        return convertView;

        }
}

