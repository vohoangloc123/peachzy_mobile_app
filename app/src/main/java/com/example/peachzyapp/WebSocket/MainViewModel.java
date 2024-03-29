package com.example.peachzyapp.WebSocket;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private MutableLiveData<Boolean> _socketStatus = new MutableLiveData<>(false);
    public LiveData<Boolean> socketStatus = _socketStatus;

    private MutableLiveData<Pair<Boolean, String>> _message = new MutableLiveData<>();
    public LiveData<Pair<Boolean, String>> message = _message;

    public void setStatus(boolean status) {
        // Implement your logic here
        _socketStatus.setValue(status);
    }

    public void setMessage(Pair<Boolean, String> message) {
        if (Boolean.TRUE.equals(_socketStatus.getValue()) ==true) {
            _message.setValue(message);
        }
    }
}
