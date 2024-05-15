package com.example.peachzyapp.fragments.MainFragments.Profiles;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.LiveData.MyViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.SignIn;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.entities.Profile;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

public class ViewProfileFragment extends Fragment {
    public static final String TAG= ViewProfileFragment.class.getName();
    private ImageView ivAvatar;
    private ImageView ivBackGround;
    private TextView tvEmail, tvAddfriend, tvGender, tvDateOfBirth, tvName, tvRefuseFriendRequest;
    private ImageButton btnAddfriend, btnRefuseFriendRequest;
    private RelativeLayout relativeLayout;
    private String urlAvatar;
    private String friendID;
    private MainActivity mainActivity;
    private DynamoDBManager dynamoDBManager;
    private ImageButton btnBack;
    private Boolean isParent;
    private String uid;
    private String status;
    private MyViewModel viewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyViewModel.class);
    }
    public static void loadCircularImageUrl(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(imageView);
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_profile, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);
        ivAvatar=view.findViewById(R.id.ivAvatar);
        ivBackGround= view.findViewById(R.id.ivBackGround);
        tvName = view.findViewById(R.id.tvName);
        tvDateOfBirth = view.findViewById(R.id.tvDateOfBirth);
        tvGender=view.findViewById(R.id.tvGender);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnRefuseFriendRequest= view.findViewById(R.id.btnRefuseFriendRequest);
        btnBack=view.findViewById(R.id.btnBack);
        btnAddfriend=view.findViewById(R.id.btnAddfriend);
        tvAddfriend=view.findViewById(R.id.tvAddFriend);
        tvRefuseFriendRequest=view.findViewById(R.id.tvRefuseFriendRequest);
        dynamoDBManager = new DynamoDBManager(getActivity());
        mainActivity= (MainActivity) getActivity();
        btnBack.setOnClickListener(v->{
            getFragmentManager().popBackStack();
            if(isParent){
                mainActivity.showBottomNavigation(true);
            }
        });

        randomBackGround();
        //get data
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        friendID = preferences.getString("friendID", null);
        uid=preferences.getString("uid", null);
        if (friendID != null||uid!=null) {
        } else {
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            friendID = bundle.getString("friendID");
            String parent=bundle.getString("parent");
            if(parent!=null)
            {
                isParent=true;
            }else
            {
                isParent=false;
                Log.e("checkUID1900", String.valueOf(isParent));
            }
        }
        loadProfile(friendID);

        //resize
        Picasso.get()
                .load(urlAvatar)
                .resize(200, 200) // Điều chỉnh kích thước theo yêu cầu
                .centerCrop()
                .into(ivAvatar);
        dynamoDBManager.getStatusByFriendId(uid, friendID, new DynamoDBManager.StatusListener() {
            @Override
            public void onStatusFetched(String fetchedStatus) {
                status=fetchedStatus;
                if (status == null) {
                    // Xử lý khi không có status được trả về
                    tvAddfriend.setText("Add friend");
                } else {
                    // Xử lý khi có status được trả về
                    switch (status) {
                        case "1":
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvAddfriend.setText("Unfriend");
                                }
                            });
                            break;
                        case "2":
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvAddfriend.setText("Remove friend request");
                                }
                            });
                            break;
                        case "3":
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvAddfriend.setText("Accept friend request");
                                    tvRefuseFriendRequest.setVisibility(View.VISIBLE);
                                    btnRefuseFriendRequest.setVisibility(View.VISIBLE);
                                }
                            });
                            break;
                        default:
                            // Xử lý khi status không phù hợp với các trường hợp trên
                            tvAddfriend.setText("Add friend");
                            break;
                    }
                }

            }
        });
        btnAddfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status == null) {
                    // Chưa kết bạn, gửi lời mời kết bạn
                    sendFriendRequest();
                } else {
                    switch (status) {
                        case "1":
                            //Xóa kết bạn
                            unfriend();
                            //changeData();
                            break;
                        case "2":
                            //Thu hồi lời mời
                            removeFriendRequest();
                            changeData();
                            break;
                        case "3":
                            //Nhận đc lời mời chấp nhận kết bạn
                            acceptFriendRequest();
                            changeData();
                            break;
                        case "null":
                            //Chưa là bạn nên k có trong mảng friends vì thế kết bạn
                            sendFriendRequest();
                            changeData();
                        default:
                            // Xử lý trường hợp không xác định
                            break;
                    }
                }
            }
        });
        //từ chối lời mời kết bạn khi nhận đc lời mời
        btnRefuseFriendRequest.setOnClickListener(v->{
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dynamoDBManager.deleteAFriendFromUser(uid, friendID);
                    dynamoDBManager.deleteAFriendFromUser(friendID, uid);
                    status="null";
                    tvAddfriend.setText("Add friend");
                    tvRefuseFriendRequest.setVisibility(View.GONE);
                    btnRefuseFriendRequest.setVisibility(View.GONE);
                }
            });

        });
        return view;
    }
    private void sendFriendRequest() {
        Log.d(TAG, "gửi kết bạn");
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dynamoDBManager.addFriend(uid, friendID, "2",uid+"-"+friendID);
                dynamoDBManager.addFriend(friendID, uid, "3",uid+"-"+friendID);
                status="2";
                tvAddfriend.setText("Remove friend request");
            }
        });

    }

    private void unfriend() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "hủy kết bạn");
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Confirm unfriend");
                builder.setMessage("Are you sure you want to unfriend?");
                builder.setPositiveButton("Yes", (dialog, which) -> {
                    // Nếu người dùng đồng ý, thực hiện chuyển đổi sang activity đăng nhập
                    dynamoDBManager.deleteAFriendFromUser(uid, friendID);
                    dynamoDBManager.deleteAFriendFromUser(friendID, uid);
                    //Xóa Conversation
                    dynamoDBManager.deleteConversation(uid,friendID);
                    dynamoDBManager.deleteConversation(friendID,uid);
                    status="null";
                    tvAddfriend.setText("Add friend");
                    changeData();
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> {
                    // Nếu người dùng hủy bỏ, đóng dialog và không thực hiện hành động gì
                    dialog.dismiss();
                });
                builder.show();
            }
        });
    }
    private void acceptFriendRequest() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Chấp nhận kết bạn");
                dynamoDBManager.addFriend(uid, friendID, "1",uid+"-"+friendID);
                dynamoDBManager.addFriend(friendID, uid, "1",uid+"-"+friendID);
                status="1";
                tvAddfriend.setText("Unfriend");
                if(tvRefuseFriendRequest.getVisibility() == View.VISIBLE || btnRefuseFriendRequest.getVisibility() == View.VISIBLE) {
                    tvRefuseFriendRequest.setVisibility(View.GONE);
                    btnRefuseFriendRequest.setVisibility(View.GONE);
                }
            }
        });

    }
    private void removeFriendRequest() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Thu hồi kết bạn");
                dynamoDBManager.deleteAFriendFromUser(uid, friendID);
                dynamoDBManager.deleteAFriendFromUser(friendID, uid);
                status="null";
                tvAddfriend.setText("Add friend");
            }
        });


    }
    public void randomBackGround() {
        Random random = new Random();

        // Sử dụng nextInt(5) để tạo số ngẫu nhiên từ 0 đến 4, sau đó cộng thêm 1 để dịch chuyển khoảng giá trị lên 1 đơn vị.
        int randomNumber= random.nextInt(5) + 1;

        if(randomNumber==1){
            Glide.with(getActivity())
                    .load("https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/background1.jpg")
                    .into(ivBackGround);
        }
        else if(randomNumber==2){
            Glide.with(getActivity())
                    .load("https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/background2.jpg")
                    .into(ivBackGround);
        }
        else if(randomNumber==3){
            Glide.with(getActivity())
                    .load("https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/background3.jpg")
                    .into(ivBackGround);
        }
        else if(randomNumber==4){
            Glide.with(getActivity())
                    .load("https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/background4.jpg")
                    .into(ivBackGround);
        }
        else if(randomNumber==5){
            Glide.with(getActivity())
                    .load("https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/background5.jpg")
                    .into(ivBackGround);
        }
    }
    public void loadProfile(String friendID){
        dynamoDBManager.getProfileByUID(friendID, new DynamoDBManager.FriendFoundForGetUIDByEmailListener() {
            @Override
            public void onFriendFound(String uid, String name, String email, String avatar, Boolean sex, String dateOfBirth) {
            }
            @Override
            public void onFriendFound(String id, String name, String email, String avatar, Boolean sex, String dateOfBirth, String role) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Profile profile=new Profile(id, name, email, avatar, sex, dateOfBirth);
                        tvName.setText(profile.getName());
                        tvEmail.setText(profile.getEmail());
                        loadCircularImageUrl(getActivity(),avatar,ivAvatar);
                        if (sex != null) {
                            if (sex) {
                                tvGender.setText("Male");
                            } else {
                                tvGender.setText("Female");
                            }
                        } else {
                            // Handle null case if needed
                        }
                        tvDateOfBirth.setText(profile.getDateOfBirth());
                    }
                });
            }
            @Override
            public void onFriendNotFound() {
            }
            @Override
            public void onError(Exception e) {
                // Xử lý lỗi nếu có.
                Log.e("ProfileFragment", "Error: " + e.getMessage());
            }
        });
    }

    private void changeData() {
        viewModel.setData("New data");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        viewModel.setData("Change");
        Log.d("Detach", "onDetach: ");
    }
}

