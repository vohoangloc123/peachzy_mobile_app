package com.example.peachzyapp.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.peachzyapp.R;
import com.example.peachzyapp.entities.Item;
import com.squareup.picasso.Picasso;

public class ChatBoxAdapter extends RecyclerView.Adapter<ChatViewHolder> {
    private Context context;
    private List<Item> items;
    public ChatBoxAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }
    public void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged(); // Cập nhật giao diện khi dữ liệu thay đổi
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }
    private OnItemLongClickListener mLongListener;

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongListener = listener;
    }
    public Item getItem(int position) {
        return items.get(position);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat_box,parent,false));
    }


    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Item currentItem = items.get(position);
        // Loại bỏ khoảng trắng không mong muốn từ chuỗi thời gian
        String trimmedTime = currentItem.getTime().trim();
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm");
        try {
            // Parse chuỗi thời gian từ chuỗi đã loại bỏ khoảng trắng
            Date date = inputFormat.parse(trimmedTime);

            // Format lại thành chuỗi chỉ chứa giờ và phút
            String timeOnly = outputFormat.format(date);

            // Hiển thị chuỗi giờ và phút trong TextView
            holder.tvTime.setText(timeOnly);
        } catch (ParseException e) {
            // Xử lý nếu có lỗi xảy ra khi parse chuỗi thời gian
            e.printStackTrace();
        }
        // Kiểm tra xem tin nhắn có phải của người gửi hay không
        boolean isSentByMe = currentItem.isSentByMe();

        // Hiển thị avatar
        Glide.with(holder.itemView.getContext())
                .load(currentItem.getAvatar())
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<>(new CircleCrop()))
                .into(holder.ivAvatar);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.tvMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfImage = (RelativeLayout.LayoutParams) holder.ivMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfFile = (RelativeLayout.LayoutParams) holder.tvLink.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfSeeker = (RelativeLayout.LayoutParams) holder.seekBar.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfVideo = (RelativeLayout.LayoutParams) holder.vvMessage.getLayoutParams();

        if (isSentByMe) {
            setAlignmentForSender(holder, params, paramsOfImage, paramsOfFile, paramsOfSeeker, paramsOfVideo);
            handleContentForSender(holder, currentItem);
        } else {
            setAlignmentForReceiver(holder, params, paramsOfImage, paramsOfFile);
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

    private void setAlignmentForSender(ChatViewHolder holder, RelativeLayout.LayoutParams... params) {
        for (RelativeLayout.LayoutParams param : params) {
            param.addRule(RelativeLayout.ALIGN_PARENT_END);
        }
        holder.tvMessage.setTextColor(context.getColor(R.color.white));
        holder.tvMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_message));
        holder.ivAvatar.setVisibility(View.GONE);
    }

    private void handleContentForSender(ChatViewHolder holder, Item currentItem) {
        if (isS3ImageUrl(currentItem.getType())) {
            Picasso.get().load(currentItem.getMessage()).into(holder.ivMessage);
            setVisibility(holder, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE);
        } else if (isS3Document(currentItem.getType())) {
            checkFileTypeAndDisplay(holder.ivMessage, currentItem.getMessage());
            holder.tvLink.setText(currentItem.getMessage());
            setVisibility(holder, View.GONE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE);
        } else if (isS3Video(currentItem.getType())) {
            setupVideo(holder, currentItem.getMessage());
        } else {
            holder.tvMessage.setText(currentItem.getMessage());
            setVisibility(holder, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
        }
    }

    private void setAlignmentForReceiver(ChatViewHolder holder, RelativeLayout.LayoutParams... params) {
        for (RelativeLayout.LayoutParams param : params) {
            param.addRule(RelativeLayout.ALIGN_PARENT_START);
        }
        holder.tvMessage.setTextColor(context.getColor(R.color.black));
        holder.tvMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_rectangle_secondary));
        holder.ivAvatar.setVisibility(View.VISIBLE);
    }

    private void handleContentForReceiver(ChatViewHolder holder, Item currentItem) {
        if (isS3ImageUrl(currentItem.getType())) {
            Picasso.get().load(currentItem.getMessage()).into(holder.ivMessage);
            setVisibility(holder, View.GONE, View.VISIBLE, View.GONE, View.GONE, View.GONE);
        } else if (isS3Document(currentItem.getType())) {
            checkFileTypeAndDisplay(holder.ivMessage, currentItem.getMessage());
            holder.tvLink.setText(currentItem.getMessage());
            setVisibility(holder, View.GONE, View.VISIBLE, View.VISIBLE, View.GONE, View.GONE);
        } else if (isS3Video(currentItem.getType())) {
            setupVideo(holder, currentItem.getMessage());
        } else {
            holder.tvMessage.setText(currentItem.getMessage());
            setVisibility(holder, View.VISIBLE, View.GONE, View.GONE, View.GONE, View.GONE);
        }
    }

    private void setVisibility(ChatViewHolder holder, int textVisibility, int imageVisibility, int linkVisibility, int seekerVisibility, int videoVisibility) {
        holder.tvMessage.setVisibility(textVisibility);
        holder.ivMessage.setVisibility(imageVisibility);
        holder.tvLink.setVisibility(linkVisibility);
        holder.seekBar.setVisibility(seekerVisibility);
        holder.vvMessage.setVisibility(videoVisibility);
    }

    private void setupVideo(ChatViewHolder holder, String videoUrl) {
        holder.vvMessage.setVisibility(View.VISIBLE);
        holder.tvMessage.setVisibility(View.GONE);
        holder.tvLink.setVisibility(View.GONE);
        holder.ivMessage.setVisibility(View.GONE);

        try {
            Uri videoUri = Uri.parse(videoUrl);
            holder.vvMessage.setVideoURI(videoUri);
            MediaController mediaController = new MediaController(context);
            mediaController.setMediaPlayer(holder.vvMessage);
            holder.vvMessage.setMediaController(mediaController);
            holder.vvMessage.setOnClickListener(v -> holder.vvMessage.setOnPreparedListener(mp -> {
                mp.start();
                holder.seekBar.setMax(mp.getDuration());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (holder.vvMessage.isPlaying()) {
                            holder.seekBar.setProgress(holder.vvMessage.getCurrentPosition());
                            new Handler().postDelayed(this, 1000);
                        }
                    }
                }, 1000);
            }));
            holder.vvMessage.setOnCompletionListener(mp -> mp.stop());
            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        holder.vvMessage.seekTo(progress);
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
    private boolean isS3ImageUrl(String url) {
        return url != null && url.equals("image");
    }
    private boolean isS3Document(String url) {
        return url != null && url.equals("document");
    }
    private boolean isS3Video(String url) {
        return url != null && url.equals("video");
    }
    @Override
    public int getItemCount() {
        return items.size();
    }
}
