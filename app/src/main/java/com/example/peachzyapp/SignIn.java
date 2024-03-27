package com.example.peachzyapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignIn extends AppCompatActivity {
    Button signUpButton;
    Button signInButton;
    EditText etEmail;
    EditText etPassword;
    Button forgetPasswordButton;

    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        FirebaseApp.initializeApp(this);
        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
        signUpButton = findViewById(R.id.btnSignUp);
        signInButton=findViewById(R.id.btnSignIn);
        forgetPasswordButton=findViewById(R.id.btnForgetPassword);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Chuyển trang
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(SignIn.this, SignUp.class);
            startActivity(intent);
        });
        forgetPasswordButton.setOnClickListener(v->{
            Intent intent = new Intent(SignIn.this, ForgetPassword.class);
            startActivity(intent);
        });
        //Xử lý button
        signInButton.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            // Kiểm tra xem trường email và mật khẩu có rỗng không
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(SignIn.this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
                return; // Không thực hiện đăng nhập nếu trường email hoặc mật khẩu rỗng
            }

            // Thực hiện đăng nhập
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Log in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignIn.this, "Log in successful.", Toast.LENGTH_SHORT).show();
                        } else {
                            // If log in fails, display a message to the user.
                            Toast.makeText(SignIn.this, "Log in failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }
}