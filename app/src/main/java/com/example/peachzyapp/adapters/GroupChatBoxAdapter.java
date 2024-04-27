package com.example.peachzyapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.R;
import com.example.peachzyapp.entities.GroupChat;
import com.squareup.picasso.Picasso;

import java.util.List;

public class GroupChatBoxAdapter extends RecyclerView.Adapter<GroupChatViewHolder>{
    Context context;
    List<GroupChat> groupChatItems;
    String userID; // Thuộc tính mới để lưu uid
    public GroupChatBoxAdapter(Context context, List<GroupChat> groupChatItems, String userID) {
        this.context = context;
        this.groupChatItems = groupChatItems;
        this.userID = userID; // Khởi tạo uid
    }
    public void setItems(List<GroupChat> groupChatItems) {
        this.groupChatItems = groupChatItems;
        notifyDataSetChanged(); // Cập nhật giao diện khi dữ liệu thay đổi
    }
    @NonNull
    @Override
    public GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_group_chat_box, parent,false));
    }
    @Override
    public void onBindViewHolder(@NonNull GroupChatViewHolder holder, int position) {
        GroupChat currentItem = groupChatItems.get(position);
        holder.tvGroupTime.setText(currentItem.getTime());
        Glide.with(holder.itemView.getContext())
                .load(currentItem.getAvatar())
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(holder.ivGroupAvatar);

        holder.tvUserName.setText(currentItem.getName());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.tvGroupMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfImage = (RelativeLayout.LayoutParams) holder.ivGroupMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfVideo = (RelativeLayout.LayoutParams) holder.vvGroupMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfFile = (RelativeLayout.LayoutParams) holder.tvGroupLink.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfSeeker = (RelativeLayout.LayoutParams) holder.seekBar.getLayoutParams();
        if (currentItem.getUserID().equals(userID)) {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            paramsOfImage.addRule(RelativeLayout.ALIGN_PARENT_END);
            paramsOfFile.addRule(RelativeLayout.ALIGN_PARENT_END);
            paramsOfVideo.addRule(RelativeLayout.ALIGN_PARENT_END);
            paramsOfSeeker.addRule(RelativeLayout.ALIGN_PARENT_END);
            holder.tvGroupMessage.setTextColor(context.getColor(R.color.white));
            holder.tvGroupMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_message));
            holder.ivGroupAvatar.setVisibility(View.GONE);
            holder.tvGroupLink.setVisibility(View.GONE);
            holder.tvUserName.setVisibility(View.GONE);
            // Nếu tin nhắn chứa đường dẫn của hình ảnh từ S3
            if (isS3ImageUrl(currentItem.getMessage())) {
                holder.tvGroupMessage.setVisibility(View.GONE);
                holder.tvGroupLink.setVisibility(View.GONE);
                holder.vvGroupMessage.setVisibility(View.GONE);
                holder.seekBar.setVisibility(View.GONE);
                holder.ivGroupMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                Picasso.get().load(currentItem.getMessage()).into(holder.ivGroupMessage);
            } else if(isS3Document(currentItem.getMessage())){
                // Hiển thị văn bản tin nhắn
                holder.tvGroupMessage.setVisibility(View.GONE);
                holder.vvGroupMessage.setVisibility(View.GONE);
                holder.tvGroupLink.setVisibility(View.VISIBLE);
                holder.seekBar.setVisibility(View.GONE);
                holder.ivGroupMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                Picasso.get().load(R.drawable.filepicture).into(holder.ivGroupMessage);
                holder.tvGroupLink.setText(currentItem.getMessage());
            }
            else if(isS3Video(currentItem.getMessage())) {
                holder.tvGroupMessage.setVisibility(View.GONE);
                holder.tvGroupLink.setVisibility(View.GONE);
                holder.ivGroupMessage.setVisibility(View.GONE); // Hiển thị ivMessage
                holder.vvGroupMessage.setVisibility(View.VISIBLE);
                String videoUrl = currentItem.getMessage();

                // Tải video từ URL và đặt nó vào VideoView
                try {
                    Uri videoUri = Uri.parse(videoUrl);
                    holder.vvGroupMessage.setVideoURI(videoUri);
                    MediaController mediaController = new MediaController(context);
                    mediaController.setMediaPlayer(holder.vvGroupMessage);
                    holder.vvGroupMessage.setMediaController(mediaController);
                    holder.vvGroupMessage.setOnClickListener(v -> {holder.vvGroupMessage.setOnPreparedListener(mp -> {
                        // Bắt đầu phát video khi đã chuẩn bị sẵn
                        mp.start();
                        // Set up SeekBar for tracking progress
                        holder.seekBar.setMax(mp.getDuration());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (holder.vvGroupMessage.isPlaying()) {
                                    int currentPosition = holder.vvGroupMessage.getCurrentPosition();
                                    holder.seekBar.setProgress(currentPosition);
                                    new Handler().postDelayed(this, 1000); // Update seekbar every second
                                }
                            }
                        }, 1000);
                    });
                        holder.vvGroupMessage.setOnCompletionListener(mp -> {
                            // Tắt VideoView khi video phát xong
                            mp.stop();
                        });

                        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    holder.vvGroupMessage.seekTo(progress);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {}

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                holder.tvGroupMessage.setText(currentItem.getMessage());
                holder.ivGroupMessage.setVisibility(View.GONE);
                holder.vvGroupMessage.setVisibility(View.GONE);
                holder.tvGroupLink.setVisibility(View.GONE);
                holder.seekBar.setVisibility(View.GONE);
                holder.tvGroupMessage.setVisibility(View.VISIBLE);
            }
        } else if(!currentItem.getUserID().equals(userID)) { // Nếu tin nhắn là của người nhận
            holder.tvGroupMessage.setTextColor(context.getColor(R.color.black));

            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsOfImage.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsOfFile.addRule(RelativeLayout.ALIGN_PARENT_START);
            // Nếu tin nhắn chứa đường dẫn của hình ảnh từ S3
            if (isS3ImageUrl(currentItem.getMessage())) {
                Picasso.get().load(currentItem.getMessage()).into(holder.ivGroupMessage);
                holder.ivGroupMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                holder.tvGroupMessage.setVisibility(View.GONE);
                holder.vvGroupMessage.setVisibility(View.GONE);
                holder.seekBar.setVisibility(View.GONE);
                holder.tvGroupLink.setVisibility(View.GONE);
            } else if (isS3Document(currentItem.getMessage())) {
                holder.tvGroupMessage.setVisibility(View.GONE);
                holder.vvGroupMessage.setVisibility(View.GONE);
                holder.tvGroupLink.setVisibility(View.VISIBLE);
                holder.seekBar.setVisibility(View.GONE);
                holder.ivGroupMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                Picasso.get().load(R.drawable.filepicture).into(holder.ivGroupMessage);
                holder.tvGroupLink.setText(currentItem.getMessage());
            } else if (isS3Video(currentItem.getMessage())) {
                holder.tvGroupMessage.setVisibility(View.GONE);
                holder.tvGroupLink.setVisibility(View.GONE);
                holder.ivGroupMessage.setVisibility(View.GONE); // Hiển thị ivMessage
                holder.vvGroupMessage.setVisibility(View.VISIBLE);
                String videoUrl = currentItem.getMessage();
                // Tải video từ URL và đặt nó vào VideoView

                try {
                    Uri videoUri = Uri.parse(videoUrl);
                    holder.vvGroupMessage.setVideoURI(videoUri);
                    MediaController mediaController = new MediaController(context);
                    mediaController.setMediaPlayer(holder.vvGroupMessage);
                    holder.vvGroupMessage.setMediaController(mediaController);
                    holder.vvGroupMessage.setOnClickListener(v -> {holder.vvGroupMessage.setOnPreparedListener(mp -> {
                            // Bắt đầu phát video khi đã chuẩn bị sẵn
                        mp.start();
                            // Set up SeekBar for tracking progress
                        holder.seekBar.setMax(mp.getDuration());
                        new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (holder.vvGroupMessage.isPlaying()) {
                                        int currentPosition = holder.vvGroupMessage.getCurrentPosition();
                                        holder.seekBar.setProgress(currentPosition);
                                        new Handler().postDelayed(this, 1000); // Update seekbar every second
                                    }
                                }
                            }, 1000);
                        });
                        holder.vvGroupMessage.setOnCompletionListener(mp -> {
                            // Tắt VideoView khi video phát xong
                            mp.stop();
                        });

                        holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                if (fromUser) {
                                    holder.vvGroupMessage.seekTo(progress);
                                }
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {}

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {}
                        });
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

            } else {
                holder.tvGroupMessage.setText(currentItem.getMessage());
                holder.ivGroupMessage.setVisibility(View.GONE);
                holder.vvGroupMessage.setVisibility(View.GONE);
                holder.tvGroupLink.setVisibility(View.GONE);
                holder.seekBar.setVisibility(View.GONE);
                holder.tvGroupMessage.setVisibility(View.VISIBLE);
            }
        }
        holder.vvGroupMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Bắt đầu phát video khi người dùng ấn vào VideoView
                if (!holder.vvGroupMessage.isPlaying()) {
                    holder.vvGroupMessage.start();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return groupChatItems.size();
    }
    private boolean isS3ImageUrl(String url) {
        return url != null && url.startsWith("https://chat-app-image-cnm.s3.ap-southeast-1.amazonaws.com/");
    }
    private boolean isS3Document(String url) {
        return url != null && url.startsWith("https://chat-app-document-cnm.s3.ap-southeast-1.amazonaws.com/");
    }
    private boolean isS3Video(String url) {
        return url != null && url.startsWith("https://chat-app-video-cnm.s3.ap-southeast-1.amazonaws.com/");
    }
}
