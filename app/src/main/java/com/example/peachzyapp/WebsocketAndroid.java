package com.example.peachzyapp;

import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
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
    private WebSocketListener webSocketListener;
    private MainViewModel viewModel;
    private final OkHttpClient okHTTPClient = new OkHttpClient();
    private WebSocket webSocket;
    Button btnConnect;
    Button btnDisconnect;
    EditText etMessage;
    Button btnSend;
    TextView tvMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_websocket_android);
        btnConnect=findViewById(R.id.connectButton);
        btnDisconnect=findViewById(R.id.disconnectButton);
        etMessage=findViewById(R.id.etMessage);
        btnSend=findViewById(R.id.btnSend);
        tvMessage=findViewById(R.id.tvMessage);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        MainViewModel viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        webSocketListener = new MySocketListener(viewModel);
        viewModel.socketStatus.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                tvMessage.setText(isConnected ? "Connected" : "Disconnected");
            }
        });

        final StringBuilder text = new StringBuilder();
        viewModel.message.observe(this, new Observer<Pair<Boolean, String>>() {
            @Override
            public void onChanged(Pair<Boolean, String> message) {
                text.append(message.first ? "You: " : "Other: ").append(message.second).append("\n");
                tvMessage.setText(text.toString());
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webSocket = okHTTPClient.newWebSocket(createRequest(), webSocketListener);
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocket != null) {
                    webSocket.close(1000, "Cancelled Manually");
                }
            }
        });
        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webSocket != null) {
                    webSocket.close(1000, "Cancelled Manually");
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString();
                if (!message.isEmpty()) {
                    if (webSocket != null) {
                        webSocket.send(message);
                        viewModel.setMessage(new Pair<>(true, message));
                    }
                } else {
                    Toast.makeText(WebsocketAndroid.this, "Enter something here ..", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    private Request createRequest() {
        String webSocketUrl = "wss://s12275.nyc1.piesocket.com/v3/1?api_key=CIL9dbE6489dDCZhDUngwMm43Btfp4J9bdnxEK4m&notify_self=1";
        return new Request.Builder()
                .url(webSocketUrl)
                .build();
    }
}
