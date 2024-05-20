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

    Context context;
    List<Item> items;

    public ChatBoxAdapter(Context context, List<Item> items) {
        this.context = context;
        this.items = items;
    }
    public void setItems(List<Item> items) {
        this.items = items;
        notifyDataSetChanged(); // Cập nhật giao diện khi dữ liệu thay đổi
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {
        return new ChatViewHolder(LayoutInflater.from(context).inflate(R.layout.item_chat_box,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Item currentItem = items.get(position);
        // Kiểm tra xem tin nhắn có phải của người gửi hay không
        boolean isSentByMe = currentItem.isSentByMe();

        // Hiển thị avatar
        Glide.with(holder.itemView.getContext())
                .load(currentItem.getAvatar())
                .placeholder(R.drawable.logo)
                .transform(new MultiTransformation<Bitmap>(new CircleCrop()))
                .into(holder.ivAvatar);
        // Hiển thị thời gian
        holder.tvTime.setText(currentItem.getTime());
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.tvMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfImage = (RelativeLayout.LayoutParams) holder.ivMessage.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfFile = (RelativeLayout.LayoutParams) holder.tvLink.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfSeeker = (RelativeLayout.LayoutParams) holder.seekBar.getLayoutParams();
        RelativeLayout.LayoutParams paramsOfVideo = (RelativeLayout.LayoutParams) holder.vvMessage.getLayoutParams();

        // Nếu tin nhắn là của người gửi
        if (isSentByMe) {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            paramsOfImage.addRule(RelativeLayout.ALIGN_PARENT_END);
            paramsOfFile.addRule(RelativeLayout.ALIGN_PARENT_END);
            paramsOfSeeker.addRule(RelativeLayout.ALIGN_PARENT_END);
            paramsOfVideo.addRule(RelativeLayout.ALIGN_PARENT_END);
            holder.tvMessage.setTextColor(context.getColor(R.color.white));
            holder.tvMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.bg_message));
            holder.ivAvatar.setVisibility(View.GONE);
            holder.tvLink.setVisibility(View.GONE);
            // Nếu tin nhắn chứa đường dẫn của hình ảnh từ S3
            if (isS3ImageUrl(currentItem.getType())) {
                Picasso.get().load(currentItem.getMessage()).into(holder.ivMessage);
                holder.ivMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                //text
                holder.tvMessage.setVisibility(View.GONE);
                //file
                holder.tvLink.setVisibility(View.GONE);
                //video
                holder.seekBar.setVisibility(View.GONE);
                holder.vvMessage.setVisibility(View.GONE);
            } else if(isS3Document(currentItem.getType())){
                // Hiển thị văn bản tin nhắn
                //text
                holder.tvMessage.setVisibility(View.GONE);
                //video
                holder.seekBar.setVisibility(View.GONE);
                holder.vvMessage.setVisibility(View.GONE);
                //file
                holder.tvLink.setVisibility(View.VISIBLE);
                holder.ivMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                checkFileTypeAndDisplay(holder.ivMessage, currentItem.getMessage());
                holder.tvLink.setText(currentItem.getMessage());
            }
            else if(isS3Video(currentItem.getType())) {
                holder.tvMessage.setVisibility(View.GONE);
                holder.tvLink.setVisibility(View.GONE);
                holder.ivMessage.setVisibility(View.GONE); // Hiển thị ivMessage
                holder.vvMessage.setVisibility(View.VISIBLE);
                String videoUrl = currentItem.getMessage();

                // Tải video từ URL và đặt nó vào VideoView
                try {
                    Uri videoUri = Uri.parse(videoUrl);
                    holder.vvMessage.setVideoURI(videoUri);
                    MediaController mediaController = new MediaController(context);
                    mediaController.setMediaPlayer(holder.vvMessage);
                    holder.vvMessage.setMediaController(mediaController);
                    holder.vvMessage.setOnClickListener(v -> {holder.vvMessage.setOnPreparedListener(mp -> {
                        // Bắt đầu phát video khi đã chuẩn bị sẵn
                        mp.start();
                        // Set up SeekBar for tracking progress
                        holder.seekBar.setMax(mp.getDuration());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (holder.vvMessage.isPlaying()) {
                                    int currentPosition = holder.vvMessage.getCurrentPosition();
                                    holder.seekBar.setProgress(currentPosition);
                                    new Handler().postDelayed(this, 1000); // Update seekbar every second
                                }
                            }
                        }, 1000);
                    });
                        holder.vvMessage.setOnCompletionListener(mp -> {
                            // Tắt VideoView khi video phát xong
                            mp.stop();
                        });

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
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                holder.tvMessage.setText(currentItem.getMessage());
                holder.ivMessage.setVisibility(View.GONE);
                holder.tvLink.setVisibility(View.GONE);
                holder.tvMessage.setVisibility(View.VISIBLE);
                //video
                holder.seekBar.setVisibility(View.GONE);
                holder.vvMessage.setVisibility(View.GONE);
            }
        } else if(!isSentByMe) { // Nếu tin nhắn là của người nhận
            holder.ivAvatar.setVisibility(View.VISIBLE);
            holder.tvMessage.setTextColor(context.getColor(R.color.black));
            holder.tvMessage.setBackground(ContextCompat.getDrawable(context, R.drawable.rounded_rectangle_secondary));
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsOfImage.addRule(RelativeLayout.ALIGN_PARENT_START);
            paramsOfFile.addRule(RelativeLayout.ALIGN_PARENT_START);
            // Nếu tin nhắn chứa đường dẫn của hình ảnh từ S3
            if (isS3ImageUrl(currentItem.getType())) {
                Picasso.get().load(currentItem.getMessage()).into(holder.ivMessage);
                holder.ivMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                //text
                holder.tvMessage.setVisibility(View.GONE);
                //file
                holder.tvLink.setVisibility(View.GONE);
                //video
                holder.seekBar.setVisibility(View.GONE);
                holder.vvMessage.setVisibility(View.GONE);
            }else if(isS3Document(currentItem.getType())){

                //text
                holder.tvMessage.setVisibility(View.GONE);
                //video
                holder.seekBar.setVisibility(View.GONE);
                holder.vvMessage.setVisibility(View.GONE);
                //file
                holder.tvLink.setVisibility(View.VISIBLE);
                holder.ivMessage.setVisibility(View.VISIBLE); // Hiển thị ivMessage
                checkFileTypeAndDisplay(holder.ivMessage, currentItem.getMessage());
                holder.tvLink.setText(currentItem.getMessage());
            } else if(isS3Video(currentItem.getType())) {
                holder.tvMessage.setVisibility(View.GONE);
                holder.tvLink.setVisibility(View.GONE);
                holder.ivMessage.setVisibility(View.GONE); // Hiển thị ivMessage
                holder.vvMessage.setVisibility(View.VISIBLE);
                String videoUrl = currentItem.getMessage();

                // Tải video từ URL và đặt nó vào VideoView
                try {
                    Uri videoUri = Uri.parse(videoUrl);
                    holder.vvMessage.setVideoURI(videoUri);
                    MediaController mediaController = new MediaController(context);
                    mediaController.setMediaPlayer(holder.vvMessage);
                    holder.vvMessage.setMediaController(mediaController);
                    holder.vvMessage.setOnClickListener(v -> {holder.vvMessage.setOnPreparedListener(mp -> {
                        // Bắt đầu phát video khi đã chuẩn bị sẵn
                        mp.start();
                        // Set up SeekBar for tracking progress
                        holder.seekBar.setMax(mp.getDuration());
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (holder.vvMessage.isPlaying()) {
                                    int currentPosition = holder.vvMessage.getCurrentPosition();
                                    holder.seekBar.setProgress(currentPosition);
                                    new Handler().postDelayed(this, 1000); // Update seekbar every second
                                }
                            }
                        }, 1000);
                    });
                        holder.vvMessage.setOnCompletionListener(mp -> {
                            // Tắt VideoView khi video phát xong
                            mp.stop();
                        });

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
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                // Hiển thị văn bản tin nhắn
                //text
                holder.tvMessage.setText(currentItem.getMessage());
                holder.tvMessage.setVisibility(View.VISIBLE);
                //image, file
                holder.ivMessage.setVisibility(View.GONE);
                //file
                holder.tvLink.setVisibility(View.GONE);
                //video
                holder.seekBar.setVisibility(View.GONE);
                holder.vvMessage.setVisibility(View.GONE);
            }
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
