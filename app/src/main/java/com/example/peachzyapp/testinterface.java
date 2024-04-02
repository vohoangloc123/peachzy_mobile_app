package com.example.peachzyapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.peachzyapp.dynamoDB.DynamoDBManager;

public class testinterface extends AppCompatActivity {
    EditText etEmail;
    Button btnFind;
    TextView tvView;
    private DynamoDBManager dynamoDBManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_testinterface);
        dynamoDBManager=new DynamoDBManager(this);
        etEmail=findViewById(R.id.etEmail);
        btnFind=findViewById(R.id.btnFind);
        tvView=findViewById(R.id.tvView);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.etFind), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnFind.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            dynamoDBManager.findFriend(email, new DynamoDBManager.FriendFoundListener() {
                @Override
                public void onFriendFound(String friendResult) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvView.setText(friendResult);
                            Log.d("FriendResult", friendResult);
                            Toast.makeText(testinterface.this, "Friend found!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFriendNotFound() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvView.setText("Friend not found");
                            Toast.makeText(testinterface.this, "Friend not found", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    Log.e("Error", "Exception occurred: ", e);
                    Toast.makeText(testinterface.this, "Error occurred: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

}