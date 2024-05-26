package com.example.peachzyapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.example.peachzyapp.entities.Item;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class GroupChatBoxAdapter extends RecyclerView.Adapter<GroupChatViewHolder>{
    private Context context;
    private List<GroupChat> groupChatItems;
    private String userID; // Thuộc tính mới để lưu uid
    public GroupChatBoxAdapter(Context context, List<GroupChat> groupChatItems, String userID) {
        this.context = context;
        this.groupChatItems = groupChatItems;
        this.userID = userID; // Khởi tạo uid
    }
    public void setItems(List<GroupChat> groupChatItems) {
        this.groupChatItems = groupChatItems;
        notifyDataSetChanged(); // Cập nhật giao diện khi dữ liệu thay đổi
    }

    //++
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }
    private ChatBoxAdapter.OnItemLongClickListener mLongListener;

    public void setOnItemLongClickListener(ChatBoxAdapter.OnItemLongClickListener listener) {
        mLongListener = listener;
    }
    public GroupChat getItem(int position) {
        return groupChatItems.get(position);
    }
    //++
    @NonNull
    @Override
    public GroupChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_group_chat_box, parent,false));
    }
    @Override
    public void onBindViewHolder(@NonNull GroupChatViewHolder holder, int position) {
        GroupChat currentItem = groupChatItems.get(position);

        String trimmedTime = currentItem.getTime().trim();
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
        try {
            // Parse chuỗi thời gian từ chuỗi đã loại bỏ khoảng trắng
            Date date = inputFormat.parse(trimmedTime);

            // Format lại thành chuỗi chỉ chứa giờ và phút
            String timeOnly = outputFormat.format(date);

            // Hiển thị chuỗi giờ và phút trong TextView
            holder.tvGroupTime.setText(timeOnly);
        } catch (ParseException e) {
            // Xử lý nếu có lỗi xảy ra khi parse chuỗi thời gian
            e.printStackTrace();
        }

        // Hiển thị avatar
        Glide.with(holder.itemView.getContext())
                .load(currentItem.getAvatar())
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<>(new CircleCrop()))
                .into(holder.ivGroupAvatar);

        holder.tvUserName.setText(currentItem.getName());

        // Kiểm tra xem tin nhắn có phải của người gửi hay không
        boolean isSentByMe = Objects.equals(currentItem.getUserID(), userID);

        // Thiết lập căn chỉnh và nội dung dựa trên người gửi
        if (isSentByMe) {
            setAlignmentForSender(holder);
            handleContentForSender(holder, currentItem);
        } else {
            setAlignmentForReceiver(holder);
            handleContentForReceiver(holder, currentItem);
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mLongListener != null) {
                    mLongListener.onItemLongClick(position);
                    return true;
                }
                return false;
            }
        });
    }

    private void setAlignmentForSender(GroupChatViewHolder holder) {
        setLayoutParamsAlignments(holder, RelativeLayout.ALIGN_PARENT_END);
        holder.tvGroupMessage.setTextColor(context.getColor(R.color.white));
        holder.tvGroupMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_message));
        holder.ivGroupAvatar.setVisibility(View.GONE);
        holder.tvGroupLink.setVisibility(View.GONE);
        holder.tvUserName.setVisibility(View.GONE);
    }

    private void handleContentForSender(GroupChatViewHolder holder, GroupChat currentItem) {
        handleContent(holder, currentItem);
    }

    private void setAlignmentForReceiver(GroupChatViewHolder holder) {
        setLayoutParamsAlignments(holder, RelativeLayout.ALIGN_PARENT_START);
        holder.tvGroupMessage.setTextColor(context.getColor(R.color.black));
        holder.tvGroupMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_rectangle_secondary));
    }

    private void handleContentForReceiver(GroupChatViewHolder holder, GroupChat currentItem) {
        handleContent(holder, currentItem);
    }

    private void setLayoutParamsAlignments(GroupChatViewHolder holder, int alignmentRule) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.tvGroupMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfImage = (RelativeLayout.LayoutParams) holder.ivGroupMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfVideo = (RelativeLayout.LayoutParams) holder.vvGroupMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfFile = (RelativeLayout.LayoutParams) holder.tvGroupLink.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfSeeker = (RelativeLayout.LayoutParams) holder.seekBar.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfAudio = (RelativeLayout.LayoutParams) holder.btnPlayPause.getLayoutParams();

        params.addRule(alignmentRule);
        paramsOfImage.addRule(alignmentRule);
        paramsOfFile.addRule(alignmentRule);
        paramsOfVideo.addRule(alignmentRule);
        paramsOfSeeker.addRule(alignmentRule);
        paramsOfAudio.addRule(alignmentRule);
    }

    private void handleContent(GroupChatViewHolder holder, GroupChat currentItem) {
        if (isS3ImageUrl(currentItem.getType())) {
            Picasso.get().load(currentItem.getMessage()).into(holder.ivGroupMessage);
            setVisibility(holder, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
        } else if (isS3Document(currentItem.getType())) {
            checkFileTypeAndDisplay(holder.ivGroupMessage, currentItem.getMessage());
            String message=filterAndDisplayFile(currentItem.getMessage());
            holder.tvGroupLink.setText(message);
            setVisibility(holder, View.GONE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE, View.GONE);
        }
        else if(isS3Audio(currentItem.getType()))   {
            setupAudio(holder, currentItem.getMessage());
        }else if (isS3Video(currentItem.getType())) {
            setupVideo(holder, currentItem.getMessage());
        } else if(isText(currentItem.getType())) {
            holder.tvGroupMessage.setText(currentItem.getMessage());
            setVisibility(holder, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE);
        }
        else{


            holder.tvGroupTime.setVisibility(View.GONE);
            setVisibility(holder, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE, View.GONE);
        }
    }

    private void setVisibility(GroupChatViewHolder holder, int textVisibility, int imageVisibility, int linkVisibility, int seekerVisibility, int videoVisibility, int audioVisibility) {
        holder.tvGroupMessage.setVisibility(textVisibility);
        holder.ivGroupMessage.setVisibility(imageVisibility);
        holder.tvGroupLink.setVisibility(linkVisibility);
        holder.seekBar.setVisibility(seekerVisibility);
        holder.vvGroupMessage.setVisibility(videoVisibility);
        holder.btnPlayPause.setVisibility(audioVisibility);
    }
    private void setupAudio(GroupChatViewHolder holder, String audioUrl) {
        holder.btnPlayPause.setVisibility(View.VISIBLE); // Hiển thị thanh trượt
        holder.tvGroupMessage.setVisibility(View.GONE); // Ẩn nội dung văn bản
        holder.tvGroupLink.setVisibility(View.GONE); // Ẩn liên kết
        holder.ivGroupMessage.setVisibility(View.GONE); // Ẩn hình ảnh
        holder.vvGroupMessage.setVisibility(View.GONE);
        holder.seekBar.setVisibility(View.GONE);
        // Thiết lập các thuộc tính của thanh trượt

        // Thiết lập nguồn âm thanh cho đối tượng MediaPlayer
        try {
            holder.mediaPlayer.setDataSource(audioUrl);
            holder.mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        holder.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                holder.btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24); // Đổi biểu tượng của nút thành biểu tượng phát
            }
        });
        holder.btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.mediaPlayer.isPlaying()) {
                    holder.mediaPlayer.pause();
                    holder.btnPlayPause.setImageResource(R.drawable.baseline_play_arrow_24); // Đổi hình ảnh của nút thành biểu tượng phát
                } else {
                    holder.mediaPlayer.start();
                    holder.btnPlayPause.setImageResource(R.drawable.baseline_stop_circle_24); // Đổi hình ảnh của nút thành biểu tượng tạm dừng
                }
            }
        });
    }
    private void setupVideo(GroupChatViewHolder holder, String videoUrl) {
        holder.vvGroupMessage.setVisibility(View.VISIBLE);
        holder.tvGroupMessage.setVisibility(View.GONE);
        holder.tvGroupLink.setVisibility(View.GONE);
        holder.ivGroupMessage.setVisibility(View.GONE);

        try {
            Uri videoUri = Uri.parse(videoUrl);
            holder.vvGroupMessage.setVideoURI(videoUri);
            MediaController mediaController = new MediaController(context);
            mediaController.setMediaPlayer(holder.vvGroupMessage);
            holder.vvGroupMessage.setMediaController(mediaController);
            holder.vvGroupMessage.setOnClickListener(v -> {
                holder.vvGroupMessage.setOnPreparedListener(mp -> {
                    mp.start();
                    holder.seekBar.setMax(mp.getDuration());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (holder.vvGroupMessage.isPlaying()) {
                                holder.seekBar.setProgress(holder.vvGroupMessage.getCurrentPosition());
                                new Handler().postDelayed(this, 1000);
                            }
                        }
                    }, 1000);
                });
            });
            holder.vvGroupMessage.setOnCompletionListener(mp -> mp.stop());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void checkFileTypeAndDisplay(ImageView holder, String url) {
        Log.d("CheckTypeOfPicture", url);
        if(url != null) {
            if (url.endsWith("pdf")) {
                Picasso.get().load(R.drawable.pdf).into(holder);
            } else if(url.endsWith("txt")) {
                Picasso.get().load(R.drawable.txt).into(holder);
            }else if(url.endsWith("docx"))
            {
                Picasso.get().load(R.drawable.docx).into(holder);
            }else
            {
                Picasso.get().load(R.drawable.filepicture).into(holder);
            }
        }
    }

    @Override
    public int getItemCount() {
        return groupChatItems.size();
    }
    private boolean isS3ImageUrl(String url) {
        return url != null && url.equals("image");
    }
    private boolean isS3Document(String url) {
        return url != null && url.equals("doc");
    }
    private boolean isS3Video(String url) {
        return url != null && url.equals("video");
    }
    private boolean isS3Audio(String url) {
        return url != null && url.equals("voice");
    }
    private boolean isText(String url) {return url != null && url.equals("text");}
    private String filterAndDisplayFile(String url) {
        // Tách tên tệp từ URL
        String[] parts = url.split("/");
        String filename = parts[parts.length - 1];
        return filename;
    }
}