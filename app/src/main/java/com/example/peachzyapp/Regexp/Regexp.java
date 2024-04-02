package com.example.peachzyapp.Regexp;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regexp {
    public boolean isValidGmailEmail(String email) {
        // Biểu thức chính quy để kiểm tra tính hợp lệ của địa chỉ email với đuôi là @gmail.com
        String gmailPattern = "^[a-zA-Z0-9_+&*-]+(\\.[a-zA-Z0-9_+&*-]+)*@gmail\\.com$";

        // Tạo Pattern object
        Pattern pattern = Pattern.compile(gmailPattern);

        // Tạo Matcher object
        Matcher matcher = pattern.matcher(email);

        // Kiểm tra xem địa chỉ email có khớp với biểu thức chính quy không
        return matcher.matches();
    }

    public boolean isValidPassword(String password) {

        String passwordPattern = ".{8,}";

        // Tạo Pattern object
        Pattern pattern = Pattern.compile(passwordPattern);

        // Tạo Matcher object
        Matcher matcher = pattern.matcher(password);

        // Kiểm tra xem địa chỉ email có khớp với biểu thức chính quy không
        return matcher.matches();
    }

    public boolean isValidName(String name) {

        String namePattern = "^[a-zA-Z]+$";

        // Tạo Pattern object
        Pattern pattern = Pattern.compile(namePattern);

        // Tạo Matcher object
        Matcher matcher = pattern.matcher(name);

        // Kiểm tra xem địa chỉ email có khớp với biểu thức chính quy không
        return matcher.matches();
    }



}
