package com.example.peachzyapp.adapters;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;

import java.util.ArrayList;
import java.util.List;

public class DeleteMemberAdapter extends RecyclerView.Adapter<DeleteMemberAdapter.DeleteMemberViewHolder> {
    private List<FriendItem> listMember;
    private List<String> selectedMemberIds = new ArrayList<>();
    private static final String TAG = "DeleteMemberAdapter";
    private Context context;
    private String groupID;
    private String uid;
    private DynamoDBManager dynamoDBManager;
    private MainActivity mainActivity;
    private FragmentManager fragmentManager;
    public DeleteMemberAdapter(List<FriendItem> listMember, Context context, String groupID, String uid, DynamoDBManager dynamoDBManager, MainActivity mainActivity, FragmentManager fragmentManager) {
        this.listMember = listMember;
        this.context = context;
        this.groupID = groupID;
        this.uid = uid;
        this.dynamoDBManager = dynamoDBManager;
        this.mainActivity = mainActivity;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public DeleteMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_delete_member, parent, false);
        return new DeleteMemberViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull DeleteMemberViewHolder holder, int position) {
        FriendItem member = listMember.get(position);
        holder.bind(member, position);
        holder.setDynamoDBManager(dynamoDBManager);
    }

    @Override
    public int getItemCount() {
        return listMember.size();
    }

    public List<String> getSelectedMemberIds() {
        return selectedMemberIds;
    }

    public class DeleteMemberViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvMemberName;
        public TextView tvRole;
        public CheckBox cbDeleteMember;
        public ImageView ivMemberAvatar;
        public ImageButton btnMore;
        private int position;
        private DynamoDBManager dynamoDBManager;
        private MainActivity mainActivity;
        private Context context;
        public DeleteMemberViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvRole = itemView.findViewById(R.id.tvRole);
            cbDeleteMember = itemView.findViewById(R.id.cbDeleteMember);
            ivMemberAvatar = itemView.findViewById(R.id.ivMemberAvatar);
            btnMore = itemView.findViewById(R.id.btnMore);
            btnMore.setOnClickListener(this);
            this.context = context;
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
            cbDeleteMember.setTag(member.getId());
            cbDeleteMember.setOnCheckedChangeListener((buttonView, isChecked) -> {
                String memberId = (String) buttonView.getTag();
                if (isChecked) {
                    if (memberId != null && !selectedMemberIds.contains(memberId)) {
                        selectedMemberIds.add(memberId);
                        Log.d(TAG, "CheckIdInMember: " + memberId);
                    }
                } else {
                    selectedMemberIds.remove(memberId);
                }
            });
        }

        private void showPopupMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.popup_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                // Xử lý sự kiện khi chọn một item trên popup menu
                int itemId = item.getItemId();
                if (itemId == R.id.action_popup_delete) {
                    Log.d(TAG, "Delete item selected for member ID: " + cbDeleteMember.getTag()+"GroupID"+groupID+" uid: "+uid);
                    dynamoDBManager.deleteGroupFromUser(String.valueOf(cbDeleteMember.getTag()), groupID);
                    dynamoDBManager.deleteUserFromGroup(groupID,String.valueOf(cbDeleteMember.getTag()));
                    countMembersInGroupWithDelay();
                    // Xử lý khi chọn Delete item ở đây
                } else if (itemId == R.id.action_popup_change_leader) {
                    Log.d(TAG, "Member: " + cbDeleteMember.getTag()+ " to member");
                    Log.d(TAG, "Leader: " + uid+ " to leader");
                    dynamoDBManager.updateGroupForAccountVer2((String) cbDeleteMember.getTag(), groupID, "leader");
                    dynamoDBManager.updateGroupForAccountVer2(uid, groupID, "member");
                    fragmentManager.popBackStack();
                    fragmentManager.popBackStack();
                    // Xử lý khi chọn Change Leader item ở đây
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
        public void countMembersInGroupWithDelay() {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dynamoDBManager.countMembersInGroup(groupID, new DynamoDBManager.CountMembersCallback() {
                        @Override
                        public void onCountComplete(int countMember) {

                            Log.d("onCountComplete", countMember + "" );
                            if (countMember <= 1) {
                                Log.d("onCountComplete1", "ok");
                                dynamoDBManager.deleteGroupConversation(groupID);
                                dynamoDBManager.deleteGroup(groupID);

                                // changeData();
                                fragmentManager.popBackStack();
                                fragmentManager.popBackStack();
                                fragmentManager.popBackStack();
                                mainActivity.showBottomNavigation(true);

                            }
                            else {
                                fragmentManager.popBackStack();
                                fragmentManager.popBackStack();
                            }

                        }
                    });
                }

            }, 200); // 0.5 giây (500 mili giây)
        }

    }
}
