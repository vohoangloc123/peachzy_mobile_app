package com.example.peachzyapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.peachzyapp.OTPAuthentication.OTPManager;
import com.example.peachzyapp.OTPAuthentication.SendEmailTask;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

public class SignUp extends AppCompatActivity {
    EditText etEmail;
    EditText etPassword;
    Button btnSignUp;

    EditText etOTP;
    Button verifyOTPButton;
    private FirebaseAuth mAuth;
    private int generatedOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        OTPManager otpManager=new OTPManager();
        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
        btnSignUp=findViewById(R.id.btnSignUp);

        etOTP=findViewById(R.id.etOTP);
        verifyOTPButton=findViewById(R.id.btnVerifyOTP);
        // Khởi tạo FirebaseApp

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

//        btnSignUp.setOnClickListener(v -> {
//            final String email = etEmail.getText().toString().trim();
//            final String password = etPassword.getText().toString().trim();
//
//            // Kiểm tra email có tồn tại không
//            mAuth.fetchSignInMethodsForEmail(email)
//                    .addOnCompleteListener(task -> {
//                        if (task.isSuccessful()) {
//                            // Lấy danh sách các phương thức đăng nhập với email
//                            List<String> signInMethods = task.getResult().getSignInMethods();
//                            if (signInMethods != null && signInMethods.size() > 0) {
//                                // Email đã tồn tại, hiển thị thông báo
//                                Toast.makeText(SignUp.this, "Email already exists.", Toast.LENGTH_SHORT).show();
//                            } else {
//                                // Email chưa tồn tại, hiển thị trường nhập OTP
//                                etOTP.setVisibility(View.VISIBLE);
//                                verifyOTPButton.setVisibility(View.VISIBLE);
//                                btnSignUp.setVisibility(View.INVISIBLE);
//                                etEmail.setVisibility(View.INVISIBLE);
//                                etPassword.setVisibility(View.INVISIBLE);
//                                generatedOTP = otpManager.generateOTP();
//                                otpManager.sendEmail(email, String.valueOf(generatedOTP));
//                                Toast.makeText(SignUp.this, generatedOTP+"", Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//                            // Xảy ra lỗi khi kiểm tra email
//                            Toast.makeText(SignUp.this, "Error checking email existence.", Toast.LENGTH_SHORT).show();
//                        }
//                    });
//
//
//
//
//        });
        btnSignUp.setOnClickListener(v -> {
            final String email = etEmail.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();

            // Kiểm tra email có tồn tại không
            mAuth.fetchSignInMethodsForEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Lấy danh sách các phương thức đăng nhập với email
                            List<String> signInMethods = task.getResult().getSignInMethods();
                            if (signInMethods != null && signInMethods.size() > 0) {
                                // Email đã tồn tại, hiển thị thông báo
                                Toast.makeText(SignUp.this, "Email already exists.", Toast.LENGTH_SHORT).show();
                            } else {
                                // Email chưa tồn tại, hiển thị trường nhập OTP
                                etOTP.setVisibility(View.VISIBLE);
                                verifyOTPButton.setVisibility(View.VISIBLE);
                                btnSignUp.setVisibility(View.INVISIBLE);
                                etEmail.setVisibility(View.INVISIBLE);
                                etPassword.setVisibility(View.INVISIBLE);
                                generatedOTP = otpManager.generateOTP();

                                // Gửi email trong một luồng riêng biệt
                                new SendEmailTask(email, String.valueOf(generatedOTP)).execute();
                                Toast.makeText(SignUp.this, generatedOTP+"", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Xảy ra lỗi khi kiểm tra email
                            Toast.makeText(SignUp.this, "Error checking email existence.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });




        verifyOTPButton.setOnClickListener(v -> {
            final String email = etEmail.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();
            String otpEntered = etOTP.getText().toString().trim();
            if(otpManager.verifyOTP(Integer.valueOf(generatedOTP), Integer.valueOf(otpEntered)))
            {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Sign up success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                Toast.makeText(SignUp.this, "Sign up successful.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                // If sign up fails, display a message to the user.
                                Toast.makeText(SignUp.this, "Sign up failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                Toast.makeText(getApplicationContext(), "Đăng ký thành công", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getApplicationContext(), "Xác  thực OTP thất bại", Toast.LENGTH_SHORT).show();
            }

        });



    }
    }

