package com.example.peachzyapp;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.peachzyapp.WebSocket.MainViewModel;
import com.example.peachzyapp.WebSocket.MySocketListener;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebsocketAndroid extends AppCompatActivity {
    private MainViewModel viewModel;
    private WebSocket webSocket;
    Button btnConnect;
    Button btnDisconnect;
    EditText etMessage;
    Button btnSend;
    TextView tvMessage;
    private boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_android);

        btnConnect=findViewById(R.id.connectButton);
        btnDisconnect=findViewById(R.id.disconnectButton);
        etMessage=findViewById(R.id.etMessage);
        btnSend=findViewById(R.id.btnSend);
        tvMessage=findViewById(R.id.tvMessage);
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.socketStatus.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                tvMessage.setText(isConnected ? "Connected" : "Disconnected");
            }
        });
        // Khi nhấn vào nút Connect
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

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
    }

    // Phương thức kết nối đến WebSocket server
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
            Toast.makeText(this, "WebSocket is not connected", Toast.LENGTH_SHORT).show();
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
