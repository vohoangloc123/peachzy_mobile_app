package com.example.peachzyapp.OTPAuthentication;

import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class OTPManager {
    // Hàm để tạo số ngẫu nhiên từ 0 đến 999999
    public static int generateOTP() {
        Random rand = new Random();
        return rand.nextInt(1000000); // 1000000 là giới hạn trên (exclusive), nghĩa là số lớn nhất sẽ là 999999
    }

    // Hàm để so sánh OTP đã gửi và OTP nhập từ người dùng
    public static boolean verifyOTP(int sentOTP, int enteredOTP) {
        return sentOTP == enteredOTP;
    }
    public static void sendEmail(String recipientEmail, String otp) {
        // Thông tin tài khoản Gmail và mật khẩu của bạn
        final String username = "nhatnguyen9.6h@gmail.com"; // Thay đổi thành địa chỉ email của bạn
        final String password = "lcgpnktjwzrcwjkj"; // Thay đổi thành mật khẩu email của bạn

        // Cấu hình các thông số cho mail server
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        // Tạo phiên gửi email
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        try {
            // Tạo đối tượng MimeMessage
            MimeMessage message = new MimeMessage(session);
            // Đặt người gửi
            message.setFrom(new InternetAddress(username));
            // Đặt người nhận
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(recipientEmail));
            // Đặt tiêu đề email
            message.setSubject("Mã OTP cho ứng dụng của bạn");

            // Tạo nội dung HTML
            String htmlContent = "<html><head><title>Mã OTP cho ứng dụng Peachzy</title></head><body>";
            htmlContent += "<h1>Ứng dụng Peachzy</h1>";
            htmlContent += "<p>Email người gửi: " + username + "</p>";
            htmlContent += "<p>Email người nhận: " + recipientEmail + "</p>";
            htmlContent += "<p>Mã OTP của bạn là: <strong>" + otp + "</strong></p>";
            htmlContent += "</body></html>";

            // Thiết lập nội dung là HTML
            message.setContent(htmlContent, "text/html; charset=utf-8");

            // Gửi email
            Transport.send(message);

            System.out.println("Đã gửi email thành công!");

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

}
