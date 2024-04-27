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
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.LiveData.MyGroupViewModel;
import com.example.peachzyapp.LiveData.MyProfileViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.Regexp.Regexp;
import com.example.peachzyapp.SignIn;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.Profile;
import com.squareup.picasso.Picasso;
public class ProfileFragment extends Fragment {
    public static final String TAG= ProfileFragment.class.getName();
    TextView tvName;
    TextView tvDateOfBirth;
    TextView tvEmail;
    TextView tvGender;
    DynamoDBManager dynamoDBManager;
    String uid;
    ImageButton btnChangeProfile;
    ImageButton btnChangePassword;
    ImageButton btnSignOut;
    ImageView ivAvatar;
    MainActivity mainActivity;
    String urlAvatar;
    Regexp regexp;
    MyProfileViewModel viewModel;
    public static void loadCircularImageUrl(Context context, String url, ImageView imageView) {

        Glide.with(context)
                .load(url)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(imageView);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        regexp= new Regexp();
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        tvName = view.findViewById(R.id.tvName);
        tvDateOfBirth = view.findViewById(R.id.tvDateOfBirth);
        tvGender=view.findViewById(R.id.tvGender);
        tvEmail = view.findViewById(R.id.tvEmail);
        ivAvatar=view.findViewById(R.id.ivAvatar);
        btnChangeProfile =view.findViewById(R.id.btnChangeProfile);
        tvDateOfBirth=view.findViewById(R.id.tvDateOfBirth);
        btnChangePassword=view.findViewById(R.id.btnChangePassword);
        btnSignOut=view.findViewById(R.id.btnSignOut);
        //initial
        dynamoDBManager = new DynamoDBManager(getActivity());
        mainActivity= (MainActivity) getActivity();

        //get data
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);
        if (uid != null) {
            Log.d("checkUID1", uid);
            // Sử dụng "uid" ở đây cho các mục đích của bạn
        } else {
            Log.e("checkUID1", "UID is null");
        }
        Bundle bundle = getArguments();
        if (bundle != null) {
            uid = bundle.getString("uid");
        }
        viewModel = new ViewModelProvider(requireActivity()).get(MyProfileViewModel.class);
        viewModel.getData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String newData) {
                Log.d("LivedataGroup", "onChanged: Yes");
                // Cập nhật RecyclerView hoặc bất kỳ thành phần UI nào khác ở đây
                // newData chứa dữ liệu mới từ Fragment con
                loadProfile(uid);
            }
        });
        //resize
        Picasso.get()
                .load(urlAvatar)
                .resize(200, 200) // Điều chỉnh kích thước theo yêu cầu
                .centerCrop()
                .into(ivAvatar);
        //load
        loadProfile(uid);
        //button
        btnChangePassword.setOnClickListener(v->{
            mainActivity.goToRequestChangePasswordFragment();
        });

        btnChangeProfile.setOnClickListener(v -> {
            mainActivity.goToEditProfileFragment(bundle);
        });
        btnSignOut.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Confirm Logout");
            builder.setMessage("Are you sure you want to log out?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                // Nếu người dùng đồng ý, thực hiện chuyển đổi sang activity đăng nhập
                Intent intent = new Intent(getActivity(), SignIn.class);
                startActivity(intent);
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> {
                // Nếu người dùng hủy bỏ, đóng dialog và không thực hiện hành động gì
                dialog.dismiss();
            });
            builder.show();
        });
        return view;
    }
    public void loadProfile(String uid){
        dynamoDBManager.getProfileByUID(uid, new DynamoDBManager.FriendFoundForGetUIDByEmailListener() {
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
                        Toast.makeText(getActivity(), "Profile!", Toast.LENGTH_SHORT).show();
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
