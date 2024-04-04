package com.example.peachzyapp.fragments.MainFragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.FriendItem;
import com.example.peachzyapp.entities.Profile;

import java.util.ArrayList;
import java.util.List;


public class ProfileFragment extends Fragment {
    EditText etName;
    EditText etDateOfBirth;
    TextView tvEmail;
    DynamoDBManager dynamoDBManager;
    String uid;
    ImageButton btnSave;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        etName = view.findViewById(R.id.etName);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        tvEmail = view.findViewById(R.id.etEmail);
        btnSave=view.findViewById(R.id.btnSave);
        dynamoDBManager = new DynamoDBManager(getActivity());
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);
        if (uid != null) {
            Log.d("checkUID", uid);
            // Sử dụng "uid" ở đây cho các mục đích của bạn
        } else {
            Log.e("checkUID", "UID is null");
        }
            dynamoDBManager.getProfileByUID(uid, new DynamoDBManager.FriendFoundForGetUIDByEmailListener() {
                @Override
                public void onFriendFound(String id, String name, String email) {
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
                            Profile profile=new Profile(id, name, email);
                            etName.setText(profile.getName());
                            tvEmail.setText(profile.getEmail());
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