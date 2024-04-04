package com.example.peachzyapp.fragments.SignUpFragments;

import android.content.Intent;
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
import com.example.peachzyapp.SignIn;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OTPFragment extends Fragment {
    EditText etOTP;
    Button btnVerifyOTP;
    OTPManager otpManager;
    private FirebaseAuth mAuth;
    private String generatedOTP;
    private String firstName;
    private String lastName;
    private String email;
    String dateOfBirth;
    private String password;
    public DynamoDBManager dynamoDBManager;
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
        dynamoDBManager=new DynamoDBManager(getActivity());
        Bundle bundle = getArguments();
        if (bundle != null) {
            generatedOTP = bundle.getString("generatedOTP");
            firstName=bundle.getString("firstName");
            lastName=bundle.getString("lastName");
            email = bundle.getString("email");
            password = bundle.getString("password");
            dateOfBirth=bundle.getString("dateOfBirth");
            boolean isMale = bundle.getBoolean("male");
            boolean isFemale = bundle.getBoolean("female");
            Log.d("OTPFragment", "generatedOTP: " + generatedOTP);
            Log.d("OTPFragment", "email: " + email);
            Log.d("OTPFragment", "password: " + password);
            Log.d("OTPFragment", "date of birth: " + dateOfBirth);
            Log.d("OTPFragment", "male: " + isMale);
            Log.d("OTPFragment", "female: " + isFemale);
        } else {
            Log.d("OTPFragment", "Bundle is null");
        }
        btnVerifyOTP.setOnClickListener(v -> {
            String otpEntered = etOTP.getText().toString().trim();
            Log.d("CheckOTP", "OTP entered" + otpEntered);
            Log.d("CheckOTP", "OTP received" + generatedOTP);

            if (generatedOTP != null && otpManager.verifyOTP(Integer.valueOf(generatedOTP), Integer.valueOf(otpEntered))) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(requireActivity(), task -> {
                            if (task.isSuccessful()) {
                                // Sign up success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    Log.d("CheckUserID", user.getUid());
                                    String id = user.getUid();

                                    // Kiểm tra giới tính và gửi giá trị đúng lên DynamoDB
                                    boolean isMale = bundle.getBoolean("male");
                                    boolean isFemale = bundle.getBoolean("female");

                                    if (isMale) {
                                        dynamoDBManager.createAccountWithFirebaseUID(id, firstName, lastName, email, dateOfBirth, true);
                                    } else if (isFemale) {
                                        dynamoDBManager.createAccountWithFirebaseUID(id, firstName, lastName, email, dateOfBirth, false);
                                    } else {
                                        // Xử lý trường hợp không xác định được giới tính
                                    }

                                    Toast.makeText(getActivity(), "Sign up successful.",
                                            Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(getActivity(), SignIn.class);
                                    startActivity(intent);
                                } else {
                                    Log.e("CheckUserID", "FirebaseUser is null");
                                    Toast.makeText(getActivity(), "Failed to get user ID.",
                                            Toast.LENGTH_SHORT).show();
                                }
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
