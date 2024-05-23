package com.example.peachzyapp.SocketIO;

import android.util.Log;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MyWebSocket {
    private WebSocket webSocket;
    private OkHttpClient client;
    private String url;
    private WebSocketListener listener;

    public interface WebSocketListener {
        void onMessageReceived(String message);
        void onConnectionStateChanged(boolean isConnected);
    }

    public MyWebSocket(String url, WebSocketListener listener) {
        this.url = url;
        this.listener = listener;
        this.client = new OkHttpClient();
        connect();
    }

    private void connect() {
        Request request = new Request.Builder()
                .url(url)
                .build();

        webSocket = client.newWebSocket(request, new okhttp3.WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                super.onOpen(webSocket, response);
                listener.onConnectionStateChanged(true);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                listener.onMessageReceived(text);
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                super.onClosed(webSocket, code, reason);
                listener.onConnectionStateChanged(false);
                reconnect();
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                super.onFailure(webSocket, t, response);
                listener.onConnectionStateChanged(false);
                reconnect();
            }
        });
    }

    private void reconnect() {
        // Reconnect logic
        if (webSocket != null) {
            webSocket.cancel();
            webSocket = null;
        }
        connect();
    }

    public void sendMessage(String message) {
        if (webSocket != null && webSocket.send(message)) {
            Log.d("GroupChatListForForwardMessageFragment", "On message: "+message);
        } else {
            Log.e("GroupChatListForForwardMessageFragment", "Failed to send message: " + message);
        }
    }

    public void closeWebSocket() {
        if (webSocket != null) {
            webSocket.close(1000, "Closing connection");
        }
    }
}
