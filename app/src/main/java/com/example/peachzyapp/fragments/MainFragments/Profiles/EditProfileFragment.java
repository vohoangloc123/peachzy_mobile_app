package com.example.peachzyapp.fragments.MainFragments.Profiles;
import android.app.Activity;
import android.app.DatePickerDialog;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.LiveData.MyGroupViewModel;
import com.example.peachzyapp.MainActivity;
import com.example.peachzyapp.R;
import com.example.peachzyapp.Regexp.Regexp;
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.entities.Profile;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class EditProfileFragment extends Fragment {
    public static final String TAG= EditProfileFragment.class.getName();
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String BUCKET_NAME = "chat-app-image-cnm";
    EditText etName;
    TextView etDateOfBirth;
    TextView tvEmail;
    DynamoDBManager dynamoDBManager;
    String uid;
    ImageButton btnSave;
    ImageButton btnBack;
    ImageButton btnChangePassword;
    ImageView ivAvatar;
    MainActivity mainActivity;
    ImageButton btnChangeAvatar;
    RadioButton rMale;
    RadioButton rFemale;
    TransferUtility s3TransferUtility;
    private AmazonS3 s3Client;
    PutObjectRequest request;
    String urlAvatar;
    Regexp regexp;
    private MyGroupViewModel viewModel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyGroupViewModel.class);
    }

    public static void loadCircularImage(Context context, Bitmap bitmap, ImageView imageView) {
        Glide.with(context)
                .load(bitmap)
                .encodeFormat(Bitmap.CompressFormat.JPEG)
                .encodeQuality(10)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(imageView);
    }

    public static void loadCircularImageUrl(Context context, String url, ImageView imageView) {

        Glide.with(context)
                .load(url)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(imageView);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        regexp= new Regexp();
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MyGroupViewModel.class);
        etName = view.findViewById(R.id.etName);
        etDateOfBirth = view.findViewById(R.id.etDateOfBirth);
        tvEmail = view.findViewById(R.id.etEmail);
        ivAvatar=view.findViewById(R.id.ivAvatar);
        btnSave=view.findViewById(R.id.btnSave);
        btnBack=view.findViewById(R.id.btnBack);
        etDateOfBirth=view.findViewById(R.id.etDateOfBirth);
        btnChangeAvatar=view.findViewById(R.id.btnChangeAvatar);
        dynamoDBManager = new DynamoDBManager(getActivity());
        mainActivity= (MainActivity) getActivity();
        rMale=view.findViewById(R.id.rMale);
        rFemale=view.findViewById(R.id.rFemale);
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        uid = preferences.getString("uid", null);
        if (uid != null) {
            Log.d("checkUID359", uid);
            // Sử dụng "uid" ở đây cho các mục đích của bạn
        } else {
            Log.e("checkUID359", "UID is null");
        }
        etDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAZI2LEH5QHYJMDGHD", "57MJpyB+ZOaL1XHIgjb1fdBsXc4HnH/S2lkEYDQ/");
        // Tạo Amazon S3 client
        s3Client = new AmazonS3Client(credentials);
        s3Client.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
        btnChangeAvatar.setOnClickListener(v->{
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                }
        );

        //resize
        Picasso.get()
                .load(urlAvatar)
                .resize(200, 200) // Điều chỉnh kích thước theo yêu cầu
                .centerCrop()
                .into(ivAvatar);
        //Button
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String dateOfBirth = etDateOfBirth.getText().toString().trim();

            Boolean sex;
            if(regexp.isValidName(name)==false){
                Toast.makeText(getActivity(), "Tên phải là chữ cái và không được trống", Toast.LENGTH_SHORT).show();
                return;
            }
            if (rMale.isChecked()) {
                sex = true; // Nam
            } else if (rFemale.isChecked()) {
                sex = false; // Nữ
            } else {
                Toast.makeText(getActivity(), "Please select gender", Toast.LENGTH_SHORT).show();
                return; // Thoát khỏi phương thức khi không có giới tính nào được chọn
            }
            // Gọi phương thức updateUser từ DynamoDBManager
            if(urlAvatar!=null) {
                dynamoDBManager.updateUser(uid, name, dateOfBirth, urlAvatar, sex, new DynamoDBManager.UpdateUserListener() {
                    @Override
                    public void onUpdateSuccess() {
                        getActivity().getSupportFragmentManager().popBackStack();
                        mainActivity.showBottomNavigation(true);
                        Toast.makeText(getActivity(), "User updated successfully", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onUserNotFound() {
                        // Xử lý khi không tìm thấy người dùng
                        Toast.makeText(getActivity(), "User not found", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onError(Exception e) {
                        // Xử lý khi có lỗi xảy ra
                        e.printStackTrace();
                    }
                });
            }else if(urlAvatar==null)
            {
                dynamoDBManager.findAvatarByUID(uid, new DynamoDBManager.AvatarCallback() {
                    @Override
                    public void onSuccess(String avatarUrl) {
                        // Xử lý đường dẫn avatar ở đây
                        String urlAvatar = avatarUrl;
                        dynamoDBManager.updateUser(uid, name, dateOfBirth, urlAvatar, sex, new DynamoDBManager.UpdateUserListener() {
                            @Override
                            public void onUpdateSuccess() {
                                getActivity().getSupportFragmentManager().popBackStack();
                                mainActivity.showBottomNavigation(true);
                                Toast.makeText(getActivity(), "User updated successfully", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onUserNotFound() {
                                // Xử lý khi không tìm thấy người dùng
                                Toast.makeText(getActivity(), "User not found", Toast.LENGTH_SHORT).show();
                            }
                            @Override
                            public void onError(Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }

                    @Override
                    public void onError(Exception e) {
                        // Xử lý lỗi ở đây
                    }
                });
            }
        });
        btnBack.setOnClickListener(v->{
            getFragmentManager().popBackStack();
            mainActivity.showBottomNavigation(true);
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
                        Log.d("GetProfile400", profile.toString());
                        etName.setText(profile.getName());
                        tvEmail.setText(profile.getEmail());
                        //                       Picasso.get().load(avatar).placeholder(R.drawable.logo).into(ivAvatar);
                        loadCircularImageUrl(getActivity(),avatar,ivAvatar);
                        if (sex != null) {
                            if (sex) {
                                rMale.setChecked(true); // Male
                                rFemale.setChecked(false); // Uncheck Female
                            } else {
                                rMale.setChecked(false); // Uncheck Male
                                rFemale.setChecked(true); // Female
                            }
                        } else {
                            Toast.makeText(getActivity(), "Please choose gender", Toast.LENGTH_SHORT).show();
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

                //crop image circle
                loadCircularImage(getActivity(),bitmap,ivAvatar);

                // Upload ảnh lên S3

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Mở InputStream từ Uri
                            String fileName=generateFileName();
                            InputStream inputStream = getActivity().getContentResolver().openInputStream(uri);

                            // Tạo đối tượng PutObjectRequest và đặt tên bucket và key
                            request = new PutObjectRequest("chat-app-image-cnm", fileName+".jpg", inputStream, new ObjectMetadata());
                            urlAvatar="https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/"+fileName+".jpg";
                            // Upload ảnh lên S3
                            s3Client.putObject(request);

                            // Đóng InputStream sau khi tải lên thành công
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private String generateFileName() {
        // Lấy ngày giờ hiện tại
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        // Tạo dãy số random
        int randomNumber = new Random().nextInt(10000);

        // Kết hợp ngày giờ và dãy số random để tạo tên file
        return "avatar_" + timeStamp + "_" + randomNumber + ".jpg";
    }
    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
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
    private void changeData() {
        viewModel.setData("New data");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        changeData(); // Cập nhật LiveData ở đây
        Log.d("Detach", "onDestroyView: ");
    }

}
