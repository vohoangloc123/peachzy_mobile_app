package com.example.peachzyapp.SocketIO;

import android.util.Log;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class MySocketManager {
    private static final String SERVER_URL = "http://localhost:3000";
    private Socket mSocket;
    public MySocketManager() {
        try {
            mSocket = IO.socket(SERVER_URL);
            mSocket.on("chat message", onNewMessage);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            // Xử lý tin nhắn mới từ máy chủ Socket.IO ở đây
            String message = (String) args[0];
            Log.d("SocketIO", "Received message: " + message);
        }
    };
//    public MySocketManager() {
//        try {
//            mSocket = IO.socket(SERVER_URL);
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public void connect() {
        mSocket.connect();
    }

    public void disconnect() {
        mSocket.disconnect();
    }

    public void sendMessage(String message) {
        mSocket.emit("chat message", message);
    }
}
