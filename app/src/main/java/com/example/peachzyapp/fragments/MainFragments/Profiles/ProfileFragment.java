package com.example.peachzyapp.fragments.MainFragments.Profiles;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.Profile;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class ProfileFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String BUCKET_NAME = "chat-app-image-cnm";
    EditText etName;
    EditText etDateOfBirth;
    TextView tvEmail;
    DynamoDBManager dynamoDBManager;
    String uid;
    ImageButton btnSave;
    ImageButton btnChangePassword;
    ImageView ivAvatar;
    MainActivity mainActivity;
    Button btnChangeAvatar;
    TransferUtility s3TransferUtility;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        etName = view.findViewById(R.id.etName);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        tvEmail = view.findViewById(R.id.etEmail);
        ivAvatar=view.findViewById(R.id.ivAvatar);
        btnSave=view.findViewById(R.id.btnSave);
        etDateOfBirth=view.findViewById(R.id.etDateOfBirth);
        btnChangePassword=view.findViewById(R.id.btnChangePassword);
        btnChangeAvatar=view.findViewById(R.id.btnChangeAvatar);
        dynamoDBManager = new DynamoDBManager(getActivity());
        mainActivity= (MainActivity) getActivity();
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);
        if (uid != null) {
            Log.d("checkUID", uid);
            // Sử dụng "uid" ở đây cho các mục đích của bạn
        } else {
            Log.e("checkUID", "UID is null");
        }



        // Khởi tạo AWSCredentials
//        AWSCredentials credentials = new BasicAWSCredentials("AKIAZI2LEH5QNBAXEUHP", "krI7P46llTA2kLj+AZQGSr9lEviTlS4bwQzBXSSi");

        // Khởi tạo AmazonS3Client
//        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(credentials))
//                .withRegion(Regions.DEFAULT_REGION) // Thay DEFAULT_REGION bằng region của bạn
//                .build();
//
//        // Khởi tạo TransferUtility
//        s3TransferUtility = TransferUtility.builder()
//                .context(getActivity())
//                .s3Client(s3Client)
//                .build();

        btnChangeAvatar.setOnClickListener(v->{
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
        }
        );
        btnChangePassword.setOnClickListener(v->{
            mainActivity.goToRequestChangePasswordFragment();
        });
        dynamoDBManager.getProfileByUID(uid, new DynamoDBManager.FriendFoundForGetUIDByEmailListener() {
            @Override
            public void onFriendFound(String id, String name, String email, String avatar, Boolean sex, String dateOfBirth) {
                SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                uid = preferences.getString("uid", null);
                if (uid != null) {
                    Log.d("checkUID1", uid);
                    // Sử dụng "uid" ở đây cho các mục đích của bạn
                } else {
                    Log.e("checkUID1", "UID is null");
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Profile profile=new Profile(id, name, email, avatar, sex, dateOfBirth);
                        etName.setText(profile.getName());
                        tvEmail.setText(profile.getEmail());
                        Picasso.get().load(avatar).placeholder(R.drawable.logo).into(ivAvatar);
                        if (sex != null) {
                            RadioButton maleRadioButton = getActivity().findViewById(R.id.rMale);
                            RadioButton femaleRadioButton = getActivity().findViewById(R.id.rFemale);

                            if (sex) {
                                maleRadioButton.setChecked(true); // Male
                                femaleRadioButton.setChecked(false); // Uncheck Female
                            } else {
                                maleRadioButton.setChecked(false); // Uncheck Male
                                femaleRadioButton.setChecked(true); // Female
                            }
                        } else {
                            // Handle null case if needed
                        }
                        etDateOfBirth.setText(profile.getDateOfBirth());
                        Toast.makeText(getActivity(), "Profile!", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onFriendNotFound() {
                // Người dùng không được tìm thấy, bạn có thể xử lý tại đây (nếu cần).
            }

            @Override
            public void onError(Exception e) {
                // Xử lý lỗi nếu có.
                Log.e("ProfileFragment", "Error: " + e.getMessage());
            }
        });


        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                ivAvatar.setImageBitmap(bitmap);

                // Upload ảnh lên S3
//                uploadImageToS3(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
//    private void uploadImageToS3(Bitmap bitmap) {
//        // Khởi tạo một AmazonS3Client
//        AmazonS3Client s3 = new AmazonS3Client(/* khai báo các thông tin xác thực của bạn ở đây */);
//
//        // Tạo tên file mới kết hợp giữa ngày giờ hiện tại và dãy số random
//        String fileName = generateFileName();
//
//        // Tạo một yêu cầu PutObjectRequest
//        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, fileName, bitmapToInputStream(bitmap), null)
//                .withCannedAcl(CannedAccessControlList.PublicRead);
//
//        // Gửi yêu cầu lên AWS S3
//        TransferObserver transferObserver = s3TransferUtility.upload(BUCKET_NAME, fileName, bitmapToInputStream(bitmap), null);
//        // Đăng ký một TransferListener để theo dõi tiến trình tải lên
//        transferObserver.setTransferListener(new TransferListener() {
//            @Override
//            public void onStateChanged(int id, TransferState state) {
//                if (state == TransferState.COMPLETED) {
//                    // Upload thành công
//                    Toast.makeText(getActivity(), "Upload completed", Toast.LENGTH_SHORT).show();
//                } else if (state == TransferState.FAILED) {
//                    // Upload thất bại
//                    Toast.makeText(getActivity(), "Upload failed", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
//                // Cập nhật tiến trình nếu cần
//            }
//
//            @Override
//            public void onError(int id, Exception ex) {
//                // Xử lý lỗi nếu có
//                Log.e("ProfileFragment", "Error uploading image to S3: " + ex.getMessage());
//            }
//        });
//    }
//
//    private String generateFileName() {
//        // Lấy ngày giờ hiện tại
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
//
//        // Tạo dãy số random
//        int randomNumber = new Random().nextInt(10000);
//
//        // Kết hợp ngày giờ và dãy số random để tạo tên file
//        return "avatar_" + timeStamp + "_" + randomNumber + ".jpg";
//    }
//    private ByteArrayInputStream bitmapToInputStream(Bitmap bitmap) {
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
//        return new ByteArrayInputStream(outputStream.toByteArray());
//    }
}
