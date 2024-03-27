package com.example.peachzyapp.OTPAuthentication;

import android.os.AsyncTask;
import android.util.Log;

public class SendEmailTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "SendEmailTask";

    private final String recipientEmail;
    private final String otp;

    public SendEmailTask(String recipientEmail, String otp) {
        this.recipientEmail = recipientEmail;
        this.otp = otp;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Gửi email ở đây
        try {
            OTPManager.sendEmail(recipientEmail, otp);
            Log.d(TAG, "Email sent successfully!");
        } catch (Exception e) {
            Log.e(TAG, "Failed to send email: " + e.getMessage());
        }
        return null;
    }
}
