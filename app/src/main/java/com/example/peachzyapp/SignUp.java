package com.example.peachzyapp;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.peachzyapp.OTPAuthentication.OTPManager;
import com.example.peachzyapp.OTPAuthentication.SendEmailTask;
import com.example.peachzyapp.Regexp.Regexp;
import com.example.peachzyapp.fragments.SignUpFragments.OTPFragment;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.List;

public class SignUp extends AppCompatActivity {
    EditText etEmail;
    EditText etFirstName;
    EditText etLastName;
    RadioButton rMale;
    RadioButton rFemale;
    TextView etDateOfBirth;
    EditText etPassword;
    Button btnSignUp;
    Button btnSignIn;
    private FirebaseAuth mAuth;
    private int generatedOTP;
    EditText etConfirmPassword;
    private Regexp regexp;

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
        rMale=findViewById(R.id.rMale);
        rFemale=findViewById(R.id.rFemale);
        etDateOfBirth=findViewById(R.id.etDateOfBirth);
        etDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        etPassword=findViewById(R.id.etPassword);
        btnSignUp=findViewById(R.id.btnSignUp);
        btnSignIn=findViewById(R.id.btnSignIn);
        etConfirmPassword=findViewById(R.id.etConfirmPassword);
        // Khai báo Regexp
        regexp= new Regexp();
        Context context = this;
        Resources resources = context.getResources();
        //
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.etFind), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUp.this, SignIn.class);
            startActivity(intent);
        });
        btnSignUp.setOnClickListener(v -> {
            final String email = etEmail.getText().toString().trim();
            final String firstName=etFirstName.getText().toString().trim();
            final String lastName=etLastName.getText().toString().trim();
            final String password = etPassword.getText().toString().trim();
            final String cfPassword=etConfirmPassword.getText().toString().trim();
            final String dateOfBirth=etDateOfBirth.getText().toString().trim();
            if (email.isEmpty() || password.isEmpty()) {
                notification(R.string.null_email_or_password);
                return;
            }
            else if (regexp.isValidName(firstName)==false) {
                notification(R.string.invalid_name);
                return;
            }
            else if  (regexp.isValidName(lastName)==false) {
                notification(R.string.invalid_name);
                return;
            }
            else if (regexp.isValidGmailEmail(email)==false){
                notification(R.string.invalid_email);
                return;
            }
            else if (password.length()<8)
            {
                notification(R.string.invalid_password);
                return;
            }
            else if(!password.equals(cfPassword) ){
                notification(R.string.invalid_confirmPassword);
                return;
            }

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
                                bundle.putString("dateOfBirth", dateOfBirth);

                                // Kiểm tra giới tính và đặt giá trị cho bundle tương ứng
                                if (rMale.isChecked()) {
                                    bundle.putBoolean("male", true);
                                    bundle.putBoolean("female", false);
                                } else if (rFemale.isChecked()) {
                                    bundle.putBoolean("male", false);
                                    bundle.putBoolean("female", true);
                                } else {
                                    // Xử lý trường hợp không chọn giới tính
                                    Toast.makeText(SignUp.this, "Please select gender.", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                OTPFragment otpFragment = new OTPFragment();
                                otpFragment.setArguments(bundle);
                                getSupportFragmentManager().beginTransaction()
                                        .replace(R.id.etFind, otpFragment)
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
    private void notification(int stringId) {
        regexp= new Regexp();
        Context context = this;
        Resources resources = context.getResources();

        String myNotification = resources.getString(stringId);
        Toast.makeText(SignUp.this, myNotification, Toast.LENGTH_SHORT).show();
    }
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                        // Xử lý khi người dùng chọn ngày
                        String selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        etDateOfBirth.setText(selectedDate);
                    }
                }, year, month, day);

        datePickerDialog.show();
    }

}

