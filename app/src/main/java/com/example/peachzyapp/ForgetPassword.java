package com.example.peachzyapp;

import android.content.Context;
import android.content.res.Resources;
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
import com.example.peachzyapp.Regexp.Regexp;
import com.google.firebase.auth.FirebaseAuth;

public class ForgetPassword extends AppCompatActivity {
    EditText etEmail;
    Button forgetPasswordButton;
    private Regexp regexp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forget_password);
        OTPManager otpManager = new OTPManager();
        etEmail = findViewById(R.id.etEmail);
        forgetPasswordButton = findViewById(R.id.btnForgetPassword);
        // Khai báo Regexp
        regexp= new Regexp();
        Context context = this;
        Resources resources = context.getResources();
        //
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        forgetPasswordButton.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                // Xử lý trường hợp email trống
                notification(R.string.null_email);
                return;
            }
            else if (regexp.isValidGmailEmail(email)==false){
                notification(R.string.invalid_email);
                return;
            }


                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Nếu email tồn tại và gửi thành công
                                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                        .addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                // Gửi email xác minh thành công
                                                Toast.makeText(getApplicationContext(), "Chúng tôi đã gửi một email xác minh để bạn có thể thiết lập lại mật khẩu.", Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Gửi email xác minh thất bại
                                                Toast.makeText(getApplicationContext(), "Đã xảy ra lỗi khi gửi email xác minh.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // Nếu có lỗi xảy ra
                                Toast.makeText(getApplicationContext(), "Failed to send password reset email", Toast.LENGTH_SHORT).show();
                            }
                        });

        });
    }
    private void notification(int stringId) {
        regexp= new Regexp();
        Context context = this;
        Resources resources = context.getResources();

        String myNotification = resources.getString(stringId);
        Toast.makeText(getApplicationContext(), myNotification, Toast.LENGTH_SHORT).show();
    }
}
