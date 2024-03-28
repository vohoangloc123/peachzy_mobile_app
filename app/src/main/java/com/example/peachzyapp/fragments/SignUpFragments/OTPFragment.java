package com.example.peachzyapp.fragments.SignUpFragments;

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
import com.google.firebase.auth.FirebaseUser;

public class OTPFragment extends Fragment {
    EditText etOTP;
    Button btnVerifyOTP;
    OTPManager otpManager;
    private FirebaseAuth mAuth;
    private String generatedOTP;
    private String email;
    private String password;

    public OTPFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_o_t_p, container, false);
        etOTP = view.findViewById(R.id.etOTP);
        btnVerifyOTP = view.findViewById(R.id.btnVerifyOTP);

        otpManager = new OTPManager();
        mAuth = FirebaseAuth.getInstance();
        Bundle bundle = getArguments();
        if (bundle != null) {
            generatedOTP = bundle.getString("generatedOTP");
            email = bundle.getString("email");
            password = bundle.getString("password");

            Log.d("OTPFragment", "generatedOTP: " + generatedOTP);
            Log.d("OTPFragment", "email: " + email);
            Log.d("OTPFragment", "password: " + password);
        } else {
            Log.d("OTPFragment", "Bundle is null");
        }

        btnVerifyOTP.setOnClickListener(v -> {
            String otpEntered = etOTP.getText().toString().trim();
            Log.d("CheckOTP", "OTP entered"+otpEntered);
            Log.d("CheckOTP", "OTP received"+generatedOTP);
            if (generatedOTP != null && otpManager.verifyOTP(Integer.valueOf(generatedOTP), Integer.valueOf(otpEntered))) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity(), task -> {
                            if (task.isSuccessful()) {
                                // Sign up success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(getActivity(), "Sign up successful.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // If sign up fails, display a message to the user.
                                Toast.makeText(getActivity(), "Sign up failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                Toast.makeText(requireContext(), "Đăng ký thành công", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Xác thực OTP thất bại", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }
}
