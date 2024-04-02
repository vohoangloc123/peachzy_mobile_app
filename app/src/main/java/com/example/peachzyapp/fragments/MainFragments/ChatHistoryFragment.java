package com.example.peachzyapp.fragments.MainFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.peachzyapp.R;
import com.example.peachzyapp.WebSocket.MainViewModel;
import com.example.peachzyapp.entities.ChatBox;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ChatHistoryFragment extends Fragment {
    Button btnBack;
    Button btnSend;
    TextView tvName;
    EditText etMessage;
    Button btnConnect;
    Button btnDisconnect;
    private MainViewModel viewModel;
    TextView tvMessage;
    View view;
    private boolean isConnected = false;
    private WebSocket webSocket;
    private BottomNavigationView bottomNavigationView;

    public static final String TAG=ChatHistoryFragment.class.getName();

    public ChatHistoryFragment() {
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.fragment_chat_history, container, false);
        tvName = view.findViewById(R.id.tv_name);
        btnBack=view.findViewById(R.id.backButton);
        btnSend=view.findViewById(R.id.btnSend);
        etMessage=view.findViewById(R.id.etMessage);
        tvMessage=view.findViewById(R.id.tvMessage);
        btnConnect=view.findViewById(R.id.connectButton);
        btnDisconnect=view.findViewById(R.id.disconnectButton);
        bottomNavigationView=getActivity().findViewById(R.id.bottom_navigation);
        Bundle bundleReceive=getArguments();
        if(bundleReceive!=null){
            ChatBox chatBox= (ChatBox) bundleReceive.get("object_chatbox");
            if(chatBox!=null&& tvName != null)
            {
                tvName.setText(chatBox.getName());
            }
        }

        tvMessage=view.findViewById(R.id.tvMessage);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.socketStatus.observe(getActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                tvMessage.setText(isConnected ? "Connected" : "Disconnected");
            }
        });
        btnBack.setOnClickListener(v->{
            if(getFragmentManager()!=null)
            {
                getFragmentManager().popBackStack();
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectWebSocket();
                viewModel.setStatus(true);
            }
        });

        // Khi nhấn vào nút Send
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message=etMessage.getText().toString();
                sendMessage();
//                viewModel.setStatus(false);
                tvMessage.append("You: " + message + "\n");
                // Xóa nội dung trong EditText
                etMessage.getText().clear();
            }
        });

        // Khi nhấn vào nút Disconnect
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeWebSocket();
            }
        });

        return view;
    }
    private void connectWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("wss://s12275.nyc1.piesocket.com/v3/1?api_key=CIL9dbE6489dDCZhDUngwMm43Btfp4J9bdnxEK4m&notify_self=1")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                super.onOpen(webSocket, response);
                // Khi kết nối được thiết lập
                isConnected = true;
                Log.d("WebSocket", "Connected to WebSocket server");
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                // Khi nhận được tin nhắn văn bản từ server
                Log.d("WebSocket", "Received message: " + text);
                // Hiển thị tin nhắn trên TextView hoặc làm gì đó với dữ liệu nhận được
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMessage.append("Sender: " + text + "\n");
                    }
                });
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                // Khi kết nối đóng
                isConnected = false;
                Log.d("WebSocket", "WebSocket connection closed");
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                super.onFailure(webSocket, t, response);
                // Khi có lỗi xảy ra
                isConnected = false;
                Log.e("WebSocket", "WebSocket connection failed: " + t.getMessage());
            }
        });
    }
    // Phương thức gửi tin nhắn qua WebSocket
    private void sendMessage() {
        if (webSocket != null && isConnected) {
            String message = etMessage.getText().toString().trim();
            webSocket.send(message);
            etMessage.setText(""); // Xóa nội dung trong EditText sau khi gửi
        } else {
            Toast.makeText(getActivity(), "WebSocket is not connected", Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức đóng kết nối WebSocket
    private void closeWebSocket() {
        if (webSocket != null) {
            webSocket.close(1000, null);
            isConnected = false;
            Log.d("WebSocket", "WebSocket connection closed");
        }
    }

}