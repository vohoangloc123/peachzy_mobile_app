package com.example.peachzyapp.fragments.MainFragments.Profiles;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.Profile;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class ViewProfileFragment extends Fragment {
    public static final String TAG= ViewProfileFragment.class.getName();
    private ImageView ivAvatar;
    private ImageView ivBackGround;
    private TextView tvName;
    private TextView tvDateOfBirth;
    private TextView tvGender;
    private TextView tvEmail;
    private String urlAvatar;
    private String friendID;
    private MainActivity mainActivity;
    private DynamoDBManager dynamoDBManager;
    private ImageButton btnBack;
    private Boolean isParent;
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
        ivAvatar=view.findViewById(R.id.ivAvatar);
        ivBackGround= view.findViewById(R.id.ivBackGround);
        tvName = view.findViewById(R.id.tvName);
        tvDateOfBirth = view.findViewById(R.id.tvDateOfBirth);
        tvGender=view.findViewById(R.id.tvGender);
        tvEmail = view.findViewById(R.id.tvEmail);
        btnBack=view.findViewById(R.id.btnBack);

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
        if (friendID != null) {
            Log.d("checkUID1900", friendID);
            // Sử dụng "uid" ở đây cho các mục đích của bạn
        } else {
            Log.e("checkUID1900", "UID is null");
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
        return view;
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
}

