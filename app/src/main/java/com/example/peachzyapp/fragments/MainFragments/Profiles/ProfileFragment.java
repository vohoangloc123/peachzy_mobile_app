package com.example.peachzyapp.fragments.MainFragments.Profiles;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.Profile;
import com.squareup.picasso.Picasso;


public class ProfileFragment extends Fragment {
    EditText etName;
    EditText etDateOfBirth;
    TextView tvEmail;
    DynamoDBManager dynamoDBManager;
    String uid;
    ImageButton btnSave;
    ImageButton btnChangePassword;
    ImageView ivAvatar;
    MainActivity mainActivity;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        etName = view.findViewById(R.id.etName);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        tvEmail = view.findViewById(R.id.etEmail);
        ivAvatar=view.findViewById(R.id.ivAvatar);
        btnSave=view.findViewById(R.id.btnSave);
        etDateOfBirth=view.findViewById(R.id.etDateOfBirth);
        btnChangePassword=view.findViewById(R.id.btnChangePassword);
        dynamoDBManager = new DynamoDBManager(getActivity());
        mainActivity= (MainActivity) getActivity();
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);
        if (uid != null) {
            Log.d("checkUID", uid);
            // Sử dụng "uid" ở đây cho các mục đích của bạn
        } else {
            Log.e("checkUID", "UID is null");
        }
        btnChangePassword.setOnClickListener(v->{
            mainActivity.goToRequestChangePasswordFragment();
        });
        dynamoDBManager.getProfileByUID(uid, new DynamoDBManager.FriendFoundForGetUIDByEmailListener() {
            @Override
            public void onFriendFound(String id, String name, String email, String avatar, Boolean sex, String dateOfBirth) {
                SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                uid = preferences.getString("uid", null);
                if (uid != null) {
                    Log.d("checkUID1", uid);
                    // Sử dụng "uid" ở đây cho các mục đích của bạn
                } else {
                    Log.e("checkUID1", "UID is null");
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Profile profile=new Profile(id, name, email, avatar, sex, dateOfBirth);
                        etName.setText(profile.getName());
                        tvEmail.setText(profile.getEmail());
                        Picasso.get().load(avatar).placeholder(R.drawable.logo).into(ivAvatar);
                        if (sex != null) {
                            RadioButton maleRadioButton = getActivity().findViewById(R.id.rMale);
                            RadioButton femaleRadioButton = getActivity().findViewById(R.id.rFemale);

                            if (sex) {
                                maleRadioButton.setChecked(true); // Male
                                femaleRadioButton.setChecked(false); // Uncheck Female
                            } else {
                                maleRadioButton.setChecked(false); // Uncheck Male
                                femaleRadioButton.setChecked(true); // Female
                            }
                        } else {
                            // Handle null case if needed
                        }
                        etDateOfBirth.setText(profile.getDateOfBirth());
                        Toast.makeText(getActivity(), "Profile!", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onFriendNotFound() {
                // Người dùng không được tìm thấy, bạn có thể xử lý tại đây (nếu cần).
            }

            @Override
            public void onError(Exception e) {
                // Xử lý lỗi nếu có.
                Log.e("ProfileFragment", "Error: " + e.getMessage());
            }
        });


        return view;
    }
}