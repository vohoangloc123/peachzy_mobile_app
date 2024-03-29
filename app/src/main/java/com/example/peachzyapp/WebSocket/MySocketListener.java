package com.example.peachzyapp.WebSocket;

import android.util.Pair;

import androidx.annotation.NonNull;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MySocketListener extends WebSocketListener {
    private MainViewModel viewModel;

    public MySocketListener(MainViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        // Handle WebSocket onOpen event here
        viewModel.setStatus(true);
        webSocket.send("Android device connected");
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        // Handle WebSocket onMessage event here
        viewModel.setMessage(new Pair<>(false, text));
    }

    @Override
    public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        // Handle WebSocket onClosing event here
        super.onClosing(webSocket, code, reason);
    }

    @Override
    public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
        super.onClosed(webSocket, code, reason);
        // Handle WebSocket onClosed event here
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        // Handle WebSocket onFailure event here
    }
}
