package com.example.peachzyapp.WebSocket;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MyWebSocketClient {
    private WebSocket webSocket;

    public void connectWebSocket() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("wss://s12275.nyc1.piesocket.com/v3/1?api_key=CIL9dbE6489dDCZhDUngwMm43Btfp4J9bdnxEK4m&notify_self=1")
                .build();
        WebSocketListener listener = new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                super.onOpen(webSocket, response);
                // Khi kết nối được thiết lập
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                super.onMessage(webSocket, text);
                // Khi nhận được tin nhắn văn bản từ server
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                super.onMessage(webSocket, bytes);
                // Khi nhận được tin nhắn dạng byte từ server
            }

            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                super.onClosing(webSocket, code, reason);
                // Khi kết nối đóng
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                super.onFailure(webSocket, t, response);
                // Khi có lỗi xảy ra
            }
        };
        webSocket = client.newWebSocket(request, listener);
    }

    public void sendMessage(String message) {
        if (webSocket != null) {
            webSocket.send(message);
        }
    }

    public void closeWebSocket() {
        if (webSocket != null) {
            webSocket.close(1000, null);
        }
    }
}
