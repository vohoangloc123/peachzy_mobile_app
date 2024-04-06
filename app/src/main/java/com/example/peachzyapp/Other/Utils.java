package com.example.peachzyapp.Other;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
    public static String getCurrentTime() {
        // Lấy thời gian hiện tại
        Calendar calendar = Calendar.getInstance();

        // Định dạng thời gian
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

        // Chuyển đổi thời gian thành chuỗi và trả về
        return dateFormat.format(calendar.getTime());
    }
}
