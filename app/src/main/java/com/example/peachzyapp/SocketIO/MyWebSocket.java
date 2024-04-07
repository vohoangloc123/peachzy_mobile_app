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

public class MyWebSocket extends WebSocketListener {
    private WebSocket webSocket;
    private OkHttpClient client;
    String url;
    Request request;
    private WebSocketListener listener; // Biến tham chiếu tới giao diện mới
    public interface WebSocketListener {
        void onMessageReceived(String message);
    }
    public MyWebSocket(String url, WebSocketListener listener) {
        this.listener = listener; // Lưu trữ listener được chuyển từ ChatBoxFragment
        OkHttpClient client = new OkHttpClient();
        request = new Request.Builder()
                .url(url)
                .build();
        webSocket = client.newWebSocket(request, this);
    }

    @Override
    public void onOpen(WebSocket webSocket, okhttp3.Response response) {
        super.onOpen(webSocket, response);
        // Khi kết nối mở thành công, bạn có thể thực hiện hành động tại đây.
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        // Nhận tin nhắn văn bản từ máy chủ và xử lý ở đây.
        Log.d("WebSocket", "Received message: " + text);
        listener.onMessageReceived(text);

    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        // Xử lý khi kết nối đóng.
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
        super.onFailure(webSocket, t, response);
        // Xử lý khi xảy ra lỗi.
        Log.d("Lost connect", "YES");
    }
    private void connect() {
        webSocket = client.newWebSocket(request, this);
    }
    public void sendMessage(String message) {
        Log.d("SendMessage", message);
        webSocket.send(message);
    }


    public void closeWebSocket() {
        webSocket.close(1000, "Closing connection");
    }

}
