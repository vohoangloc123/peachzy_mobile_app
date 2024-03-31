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
import com.example.peachzyapp.fragments.SignUpFragments.OTPFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;

import java.util.List;

public class SignUp extends AppCompatActivity {
    EditText etEmail;
    EditText etFirstName;
    EditText etLastName;
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
        etFirstName=findViewById(R.id.etFirstName);
        etLastName=findViewById(R.id.etLastName);
        etPassword=findViewById(R.id.etPassword);
        btnSignUp=findViewById(R.id.btnSignUp);
        // Khởi tạo FirebaseApp

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnSignUp.setOnClickListener(v -> {
            final String email = etEmail.getText().toString().trim();
            final String firstName=etFirstName.getText().toString().trim();
            final String lastName=etLastName.getText().toString().trim();
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
                                btnSignUp.setVisibility(View.INVISIBLE);
                                etEmail.setVisibility(View.INVISIBLE);
                                etPassword.setVisibility(View.INVISIBLE);
                                generatedOTP = otpManager.generateOTP();
                                // Gửi email trong một luồng riêng biệt
                                Bundle bundle = new Bundle();
                                bundle.putString("generatedOTP", String.valueOf(generatedOTP));
                                bundle.putString("email", email);
                                bundle.putString("firstName", firstName);
                                bundle.putString("lastName", lastName);
                                bundle.putString("password", password);
                                OTPFragment otpFragment = new OTPFragment();
                                otpFragment.setArguments(bundle);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.main, otpFragment)
                                        .commit();
                                new SendEmailTask(email, String.valueOf(generatedOTP)).execute();
                                Toast.makeText(SignUp.this, generatedOTP+"", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Xảy ra lỗi khi kiểm tra email
                            Toast.makeText(SignUp.this, "Error checking email existence.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
    }

