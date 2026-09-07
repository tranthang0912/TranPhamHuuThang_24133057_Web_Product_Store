package com.baitap03.service;

import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import com.baitap03.util.Constants;

public class EmailService {

    public void sendActivationOtp(String recipient, String fullname, String otp) {
        send(
                recipient,
                "Mã OTP kích hoạt tài khoản",
                "Xin chào " + fullname + ",\n\n"
                + "Mã OTP kích hoạt tài khoản của bạn là: " + otp + "\n"
                + "Mã có hiệu lực trong " + Constants.OTP_EXPIRE_MINUTES + " phút.\n\n"
                + "Nếu bạn không thực hiện đăng ký, hãy bỏ qua email này."
        );
    }

    public void sendPasswordResetOtp(String recipient, String fullname, String otp) {
        send(
                recipient,
                "Mã OTP đặt lại mật khẩu",
                "Xin chào " + fullname + ",\n\n"
                + "Mã OTP đặt lại mật khẩu của bạn là: " + otp + "\n"
                + "Mã có hiệu lực trong " + Constants.OTP_EXPIRE_MINUTES + " phút.\n\n"
                + "Nếu bạn không yêu cầu đổi mật khẩu, hãy bỏ qua email này."
        );
    }

    private void send(String recipient, String subject, String body) {
        String host = Constants.readSetting("SMTP_HOST", "smtp.gmail.com");
        String port = Constants.readSetting("SMTP_PORT", "587");
        String username = Constants.readSetting("SMTP_USERNAME", "");
        String password = Constants.readSetting("SMTP_PASSWORD", "");
        String from = Constants.readSetting("SMTP_FROM_EMAIL", username);

        if (username.isBlank() || password.isBlank() || from.isBlank()) {
            throw new IllegalStateException(
                    "Chưa cấu hình SMTP_USERNAME, SMTP_PASSWORD và SMTP_FROM_EMAIL"
            );
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject, "UTF-8");
            message.setText(body, "UTF-8");
            Transport.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Không thể gửi email OTP", e);
        }
    }
}
