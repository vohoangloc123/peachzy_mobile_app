package com.example.peachzyapp.fragments.MainFragments.Profiles;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.peachzyapp.R;
import com.example.peachzyapp.fragments.MainFragments.Users.AddFriendFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordFragment extends Fragment {
    Button btnChangePassword;
    EditText etCurrentPassword;
    EditText etNewPassword;
    EditText etReNewPassword;
    private FirebaseAuth mAuth;
    public static final String TAG= ChangePasswordFragment.class.getName();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_change_password, container, false);
        mAuth = FirebaseAuth.getInstance();
        btnChangePassword=view.findViewById(R.id.btnChangePassword);
        etCurrentPassword=view.findViewById(R.id.etCurrentPassword);
        etNewPassword=view.findViewById(R.id.etNewPassword);
        etReNewPassword=view.findViewById(R.id.etReNewPassword);
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPassword = etCurrentPassword.getText().toString().trim();
                final String newPassword = etNewPassword.getText().toString().trim();
                String reNewPassword = etReNewPassword.getText().toString().trim();

                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    // Reauthenticate user
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(user.getEmail(), currentPassword);

                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Password is reauthenticated, now change it
                                        if (newPassword.equals(etReNewPassword.getText().toString().trim())) {
                                            user.updatePassword(newPassword)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(getActivity(), "Password updated", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(getActivity(), "Password update failed", Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(getActivity(), "New passwords do not match", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(getActivity(), "Authentication failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
        return view;
    }
}