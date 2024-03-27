package com.example.peachzyapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.peachzyapp.OTPAuthentication.OTPManager;
import com.example.peachzyapp.OTPAuthentication.SendEmailTask;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ForgetPassword extends AppCompatActivity {
    EditText etEmail;
    Button forgetPasswordButton;

    EditText etOTP;
    Button verifyOTPButton;

    EditText etPassword;
    EditText etRePassword;
    Button changePasswordButton;
    private FirebaseAuth mAuth;
    private int generatedOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_forget_password);
        OTPManager otpManager=new OTPManager();
        etEmail=findViewById(R.id.etEmail);
        forgetPasswordButton=findViewById(R.id.btnForgetPassword);
        etOTP=findViewById(R.id.etOTP);
        verifyOTPButton=findViewById(R.id.btnVerifyOTP);
        etPassword=findViewById(R.id.etPassword);
        etRePassword=findViewById(R.id.etRePassword);
        changePasswordButton=findViewById(R.id.btnChangePassword);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        forgetPasswordButton.setOnClickListener(v -> {
            String email=etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                // Xử lý trường hợp email trống
                Toast.makeText(getApplicationContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Nếu email tồn tại và gửi thành công
                                etEmail.setVisibility(View.INVISIBLE);
                                forgetPasswordButton.setVisibility(View.INVISIBLE);
                                etOTP.setVisibility(View.VISIBLE);
                                verifyOTPButton.setVisibility(View.VISIBLE);
                                generatedOTP = otpManager.generateOTP();
                                new SendEmailTask(email, String.valueOf(generatedOTP)).execute();
                                Toast.makeText(getApplicationContext(), String.valueOf(generatedOTP), Toast.LENGTH_SHORT).show();
                            } else {
                                // Nếu có lỗi xảy ra
                                Toast.makeText(getApplicationContext(), "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        verifyOTPButton.setOnClickListener(v -> {
            String otpEntered=etOTP.getText().toString().trim();

            if(otpManager.verifyOTP(Integer.valueOf(generatedOTP), Integer.valueOf(otpEntered))){
                etOTP.setVisibility(View.INVISIBLE);
                verifyOTPButton.setVisibility(View.INVISIBLE);
                etPassword.setVisibility(View.VISIBLE);
                etRePassword.setVisibility(View.VISIBLE);
                changePasswordButton.setVisibility(View.VISIBLE);
                Toast.makeText(getApplicationContext(), "Xác  thực OTP thành công", Toast.LENGTH_SHORT).show();
            }else
            {
                Toast.makeText(getApplicationContext(), "Xác  thực OTP thất bại", Toast.LENGTH_SHORT).show();
            }
        });
        changePasswordButton.setOnClickListener(v->{
            String newPassword=etPassword.getText().toString().trim();
            String rePassword=etRePassword.getText().toString().trim();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if(newPassword.equals(rePassword))
            {
                if (user != null) {
                    // Thực hiện thay đổi mật khẩu
                    user.updatePassword(newPassword).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Mật khẩu đã được thay đổi thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Đã xảy ra lỗi khi thay đổi mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    // Người dùng chưa đăng nhập
                    Toast.makeText(getApplicationContext(), "Vui lòng đăng nhập để thay đổi mật khẩu", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}