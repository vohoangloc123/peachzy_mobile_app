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
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private WebSocket webSocket;
    private boolean isConnected = false;

    Button btnConnect;
    Button btnDisconnect;
    EditText etMessage;
    Button btnSend;
    TextView tvMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_websocket_android);

        btnConnect = findViewById(R.id.connectButton);
        btnDisconnect = findViewById(R.id.disconnectButton);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvMessage = findViewById(R.id.tvMessage);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.socketStatus.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isConnected) {
                tvMessage.setText(isConnected ? "Connected" : "Disconnected");
            }
        });

        viewModel.message.observe(this, new Observer<Pair<Boolean, String>>() {
            @Override
            public void onChanged(Pair<Boolean, String> message) {
                String newText = message.first ? "You: " : "Other: ";
                newText += message.second + "\n";
                tvMessage.append(newText);

                boolean sentByYou = message.first;
                String messageContent = message.second;
                if (sentByYou) {
                    Log.d("WebsocketAndroid", "Message sent successfully: " + messageContent);
                } else {
                    Log.d("WebsocketAndroid", "Received message: " + messageContent);
                }
            }
        });

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isConnected) {
                    Log.d("WebsocketAndroid", "Connecting to WebSocket");
                    Request request = createRequest();
                    MySocketListener socketListener = new MySocketListener(viewModel);
                    webSocket = okHttpClient.newWebSocket(request, socketListener);
                    isConnected = true;
                    viewModel.setStatus(true);
                    Toast.makeText(WebsocketAndroid.this, "Connected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(WebsocketAndroid.this, "Already connected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDisconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConnected && webSocket != null) {
                    viewModel.setStatus(false);
                    webSocket.close(1000, "Cancelled Manually");
                    isConnected = false;
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = etMessage.getText().toString();
                if (!message.isEmpty()) {
                    Log.d("WebSocketDebug", "Sending message: " + message);
                    webSocket.send(message);
                    Toast.makeText(WebsocketAndroid.this, "Message sent: " + message, Toast.LENGTH_SHORT).show();
                    viewModel.setMessage(new Pair<>(true, message));
                    etMessage.getText().clear();
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
