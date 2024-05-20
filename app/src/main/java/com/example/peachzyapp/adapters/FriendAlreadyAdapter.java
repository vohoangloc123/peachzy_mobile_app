package com.example.peachzyapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import com.example.peachzyapp.SignIn;
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

    private MyViewModel viewModel;

    public void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
    }



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
        public int position;
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
        private void showPopupMenu(View view, int position) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.popup_listfriend_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                // Xử lý sự kiện khi chọn một item trên popup menu
                int itemId = item.getItemId();
                if (itemId == R.id.action_popup_unfriend) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Confirm unfriend");
                    builder.setMessage("Are you sure you want to unfriend?");
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        // Nếu người dùng đồng ý, thực hiện chuyển đổi sang activity đăng nhập
                        Log.d(TAG, "ProfileUnfriend: "+"my uid: "+uid+" friendID: "+friendID);
                        dynamoDBManager.deleteAFriendFromUser(uid, friendID);
                        dynamoDBManager.deleteAFriendFromUser(friendID, uid);
                        //Xóa Conversation
                        dynamoDBManager.deleteConversation(uid,friendID);
                        dynamoDBManager.deleteConversation(friendID,uid);
                        // Xóa mục khỏi danh sách và thông báo cho Adapter biết
                        listFriend.remove(position);
                        notifyItemRemoved(position);
                    });
                    builder.setNegativeButton("Cancel", (dialog, which) -> {
                        // Nếu người dùng hủy bỏ, đóng dialog và không thực hiện hành động gì
                        dialog.dismiss();
                    });
                    builder.show();
                } else if (itemId == R.id.action_popup_view_profile) {
                    Log.d(TAG, "ViewProfile: "+uid);
                    //gọi hàm dynamoDB lấy dữ liệu ng dùng friendID
                    Bundle bundle=new Bundle();
                    bundle.putString("friendID",friendID);
                    bundle.putString("parent", "this is parent fragment");
                    mainActivity.goToViewProfileFragment(bundle);
                }
                return true;
            });
            popupMenu.show();
        }
@Override
public void onClick(View v) {
    int clickedPosition = getAdapterPosition(); // Lấy vị trí của item được click
    if (clickedPosition != RecyclerView.NO_POSITION) { // Kiểm tra vị trí có hợp lệ không
        Log.d(TAG, "onClick: " + clickedPosition);
        showPopupMenu(v, clickedPosition); // Chuyển vị trí đã click vào phương thức showPopupMenu
    }
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

    private void changeData() {
        viewModel.setData("New data");
    }
}