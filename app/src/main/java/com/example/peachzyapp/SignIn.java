package com.example.peachzyapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.peachzyapp.Regexp.Regexp;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
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
    private DynamoDBManager dynamoDBManager;
    private Regexp regexp;
    //for test
    private FirebaseUser user;
    Button testButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        DynamoDBManager dynamoDBManager=new DynamoDBManager(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        FirebaseApp.initializeApp(this);
        etEmail=findViewById(R.id.etEmail);
        etPassword=findViewById(R.id.etPassword);
        signUpButton = findViewById(R.id.btnSignUp);
        signInButton=findViewById(R.id.btnSignIn);
        forgetPasswordButton=findViewById(R.id.btnForgetPassword);
        testButton=findViewById(R.id.testButton);
        dynamoDBManager = new DynamoDBManager(this);
        // Khai báo Regexp
        regexp= new Regexp();
        Context context = this;
        Resources resources = context.getResources();
        //
        if (dynamoDBManager.checkDynamoDBConnection()) {
            Toast.makeText(this, "DynamoDB connection successful.", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(this, "DynamoDB connection failed.", Toast.LENGTH_SHORT).show();
        }
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.etFind), (v, insets) -> {
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
                notification(R.string.null_email_or_password);
                // Không thực hiện đăng nhập nếu trường email hoặc mật khẩu rỗng
                return;
            }
            //Kiểm tra gmail có hợp lệ hay không
            else if(regexp.isValidGmailEmail(email)==false){
                notification(R.string.invalid_email);
                return;
            }

                // Thực hiện đăng nhập
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            if (task.isSuccessful()) {
                                // Log in success, update UI with the signed-in user's information
                                user = mAuth.getCurrentUser();

//                                Intent intent = new Intent(this, MainActivity.class);
//                                intent.putExtra("uid", String.valueOf(user.getUid()));
//                                startActivity(intent);
//                                Toast.makeText(SignIn.this, "Log in successful.", Toast.LENGTH_SHORT).show();
                                new CountDownTask().execute();
                            } else {
                                // If log in fails, display a message to the user.
                                Toast.makeText(SignIn.this, "Log in failed.", Toast.LENGTH_SHORT).show();
                            }
                        });

        });


        testButton.setOnClickListener(v -> {
            Intent intent=new Intent(this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(SignIn.this, "Log in successful.", Toast.LENGTH_SHORT).show();
        });

    }
    private class CountDownTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Đợi 3 giây
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            // Hiển thị thông báo Toast với giá trị % đếm được
            Toast.makeText(SignIn.this, "Logging in: " + values[0] + "%", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // Chuyển sang MainActivity
            Intent intent = new Intent(SignIn.this, MainActivity.class);
            intent.putExtra("uid", String.valueOf(user.getUid()));
            startActivity(intent);
            // Hiển thị thông báo Toast khi đăng nhập thành công
            Toast.makeText(SignIn.this, "Log in successful.", Toast.LENGTH_SHORT).show();
        }
    }
    //Hiện thông báo regext
    private void notification(int stringId) {
        regexp= new Regexp();
        Context context = this;
        Resources resources = context.getResources();

        String myNotification = resources.getString(stringId);
        Toast.makeText(SignIn.this, myNotification, Toast.LENGTH_SHORT).show();
    }
}