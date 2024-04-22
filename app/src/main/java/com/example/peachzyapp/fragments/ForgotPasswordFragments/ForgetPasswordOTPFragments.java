package com.example.peachzyapp.fragments.ForgotPasswordFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.peachzyapp.OTPAuthentication.OTPManager;
import com.example.peachzyapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPasswordOTPFragments extends Fragment {
    private EditText etOTPEntered;
    private Button btnVerifyOTP;
    private String generatedOTP;
    private String email;
    private  OTPManager otpManager;
    public ForgetPasswordOTPFragments() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_forget_password_otp_fragments, container, false);
        etOTPEntered=view.findViewById(R.id.etOTP);
        btnVerifyOTP=view.findViewById(R.id.btnVerifyOTP);
        otpManager= new OTPManager();
        Bundle bundle = getArguments();
        if (bundle != null) {
            generatedOTP = bundle.getString("generatedOTP");
            email=bundle.getString("email");
            Log.d("OTPFragment", "generatedOTP: " + generatedOTP);
        } else {
            Log.d("OTPFragment", "Bundle is null");
        }
        btnVerifyOTP.setOnClickListener(v -> {
            String otpEntered = etOTPEntered.getText().toString().trim();
            Log.d("CheckOTP", "OTP entered"+otpEntered);
            Log.d("CheckOTP", "OTP received"+generatedOTP);
            if (generatedOTP != null && otpManager.verifyOTP(Integer.valueOf(generatedOTP), Integer.valueOf(otpEntered))) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Gửi email xác minh thành công
                                Toast.makeText(getActivity(), "Chúng tôi đã gửi một email xác minh để bạn có thể thiết lập lại mật khẩu.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Gửi email xác minh thất bại
                                Toast.makeText(getActivity(), "Đã xảy ra lỗi khi gửi email xác minh.", Toast.LENGTH_SHORT).show();
                            }
                        });
                Toast.makeText(requireContext(), "Xác thực OTP thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Xác thực OTP thất bại", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }
}