package com.example.peachzyapp.fragments.MainFragments.GroupChat.Option;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
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
import com.example.peachzyapp.dynamoDB.DynamoDBManager;
import com.example.peachzyapp.fragments.MainFragments.Chats.ChatBoxFragment;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class GroupOptionFragment extends Fragment {
    public static final String TAG= GroupOptionFragment.class.getName();
    private String groupID;
    private String groupName;
    private String groupAvatar;
    private ImageButton btnBack;
    private ImageButton btnListMember;
    private ImageButton btnAddMember;
    private ImageButton btnOutGroup;
    private ImageButton btnDeleteGroup;
    private ImageButton btnChangeGroupName;
    private ImageButton btnManageMember;
    private TextView tvDeleteGroup;
    private TextView tvManageMember;
    private ImageButton btnGroupAvatar;
    private TextView tvGroupName;
    private MainActivity mainActivity;
    private String userID;
    private DynamoDBManager dynamoDBManager;
    private MyGroupViewModel viewModel;
    private AmazonS3 s3Client;
    private static final int PICK_IMAGE_REQUEST = 1;
    private PutObjectRequest request;
    private String urlAvatar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MyGroupViewModel.class);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_group_option, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(MyGroupViewModel.class);
        btnBack=view.findViewById(R.id.btnBack);
        btnListMember =view.findViewById(R.id.btnListMember);
        btnAddMember=view.findViewById(R.id.btnAddMember);
        btnOutGroup=view.findViewById(R.id.btnOutGroup);
        btnDeleteGroup =view.findViewById(R.id.btnDeleteGroup);
        btnManageMember =view.findViewById(R.id.btnManageMember);
        btnChangeGroupName=view.findViewById(R.id.btnChangeGroupName);
        btnGroupAvatar =view.findViewById(R.id.ivGroupAvatar);
        tvGroupName=view.findViewById(R.id.tvGroupName);
        tvDeleteGroup=view.findViewById(R.id.tvDeleteGroup);
        tvManageMember =view.findViewById(R.id.tvManageMember);
        //initial
        mainActivity = (MainActivity) getActivity();
        dynamoDBManager=new DynamoDBManager(getContext());
        //bundle
        Bundle bundleReceive=getArguments();
        groupID = bundleReceive.getString("groupID");
        Log.d("CheckBundleOfGroupChat", "onCreateView: "+groupID);
        groupName= bundleReceive.getString("groupName");
        Log.d("CheckBundleOfGroupChat", "onCreateView: "+groupName);
        groupAvatar= bundleReceive.getString("groupAvatar");
        Log.d("CheckBundleOfGroupChat", "onCreateView: "+groupAvatar);
        Glide.with(getContext())
                .load(groupAvatar)
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(btnGroupAvatar);
        tvGroupName.setText(groupName);
        SharedPreferences preferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        userID = preferences.getString("uid", null);
        if (userID != null) {
            Log.d("FriendcheckUID", userID);
            // Sử dụng "uid" ở đây cho các mục đích của bạn
        } else {
            Log.e("FriendcheckUID", "UID is null");
        }
        //button
        btnBack.setOnClickListener(v->{
            Log.d("ChatBoxFragment", "bên group option " + groupName);
            ChatBoxFragment chatBoxFragment = (ChatBoxFragment)  getActivity().getSupportFragmentManager().findFragmentByTag(ChatBoxFragment.TAG);
            if (chatBoxFragment != null) {
                chatBoxFragment.setGroupName(groupName);
            }
          //  changeData();
            getActivity().getSupportFragmentManager().popBackStack();

        });
        btnListMember.setOnClickListener(v->{
            Bundle bundle = new Bundle();
            bundle.putString("groupID", groupID);
            mainActivity.goToListMember(bundle);
            Log.d("CheckButton", "WORK");
        });
        btnManageMember.setOnClickListener(v->{
            Bundle bundle = new Bundle();
            bundle.putString("groupID", groupID);
            mainActivity.goToManageMember(bundle);
            Log.d("CheckButton", "WORK");
        });
        btnAddMember.setOnClickListener(v->{
            Bundle bundle = new Bundle();
            bundle.putString("groupID", groupID);

            mainActivity.goToAddMembersToGroup(bundle);
        });
        btnOutGroup.setOnClickListener(v->{
            dynamoDBManager.deleteUserFromGroup(groupID, userID);
            dynamoDBManager.deleteGroupFromUser(userID, groupID);
            countMembersInGroupWithDelay();
            getActivity().getSupportFragmentManager().popBackStack();
            getActivity().getSupportFragmentManager().popBackStack();
            mainActivity.showBottomNavigation(true);
        });
        btnChangeGroupName.setOnClickListener(v -> {
            Bundle bundle=new Bundle();
            bundle.putString("groupID", groupID);
            mainActivity.goToEditGroupNameFragment(bundle);
        });
        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAZI2LEH5QHYJMDGHD", "57MJpyB+ZOaL1XHIgjb1fdBsXc4HnH/S2lkEYDQ/");
        // Tạo Amazon S3 client
        s3Client = new AmazonS3Client(credentials);
        s3Client.setRegion(Region.getRegion(Regions.AP_SOUTHEAST_1));
        btnGroupAvatar.setOnClickListener(v->{
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

                }
        );
        Log.d("CheckID136", groupID);
        dynamoDBManager.getGroupInfoByUser(userID,groupID, new DynamoDBManager.LoadGroupInfoListener() {
            @Override
            public void onGroupInfoLoaded(String role) {
                Log.d("CheckGroup131", role);
                if(role.equals("leader"))
                {
                    btnDeleteGroup.setEnabled(true);
                    btnManageMember.setEnabled(true);
                }
                else
                {
                    btnDeleteGroup.setAlpha(0.5f);
                    btnManageMember.setAlpha(0.5f);
                    int grayColor = Color.argb(255, 128, 128, 128); // Màu xám (RGB: 128, 128, 128)
                    tvDeleteGroup.setTextColor(grayColor);
                    tvManageMember.setTextColor(grayColor);
                    btnManageMember.setEnabled(false);
                    btnDeleteGroup.setEnabled(false);
                }
            }

            @Override
            public void onGroupInfoNotFound() {

            }

            @Override
            public void onLoadError(Exception e) {

            }
        });
        btnDeleteGroup.setOnClickListener(v->{
            dynamoDBManager.deleteGroupConversation(groupID);
            //xóa groupID trong group của bảng Users của mình
//            dynamoDBManager.deleteUserFromGroup(groupID, userID);
            dynamoDBManager.deleteGroupFromUser(userID, groupID);
            //xóa groupID trong group của bảng Users của những member khác
//            dynamoDBManager.deleteUserFromGroup(groupID, userID);
            dynamoDBManager.findMemberOfGroup(groupID, new DynamoDBManager.ListMemberListener() {
                @Override
                public void ListMemberID(String id) {
                    dynamoDBManager.deleteGroupFromUser(id, groupID);
                }
            });
            // dynamoDBManager.deleteGroupFromUser(userID, groupID);
            //Xóa group
            dynamoDBManager.deleteGroup(groupID);
            getActivity().getSupportFragmentManager().popBackStack();
            getActivity().getSupportFragmentManager().popBackStack();
            mainActivity.showBottomNavigation(true);
        });

        return view;
    }
    public static void loadCircularImage(Context context, Bitmap bitmap, ImageView imageView) {
        Glide.with(context)
                .load(bitmap)
                .encodeFormat(Bitmap.CompressFormat.JPEG)
                .encodeQuality(10)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(imageView);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), uri);
                btnGroupAvatar.setImageBitmap(bitmap);

                //crop image circle
                loadCircularImage(getActivity(),bitmap,btnGroupAvatar);

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
                            dynamoDBManager.updateGroupAvatar(groupID, urlAvatar);
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
    public void setGroupName(String groupName){
        Log.d("GroupOptionFragment", "bên group option: " + groupName);
        if(tvGroupName != null) {
            tvGroupName.setText(groupName);
        } else {
            Log.e("GroupOptionFragment", "tvGroupName is null");
        }
    }
    public void countMembersInGroupWithDelay() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dynamoDBManager.countMembersInGroup(groupID, new DynamoDBManager.CountMembersCallback() {
                    @Override
                    public void onCountComplete(int countMember) {

                        Log.d("onCountComplete", countMember + "" );
                        if (countMember <= 1) {
                            Log.d("onCountComplete1", "ok");
                            dynamoDBManager.deleteGroupConversation(groupID);
                            dynamoDBManager.deleteGroup(groupID);

                        }

                    }
                });
            }

        }, 200); // 0.5 giây (500 mili giây)
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