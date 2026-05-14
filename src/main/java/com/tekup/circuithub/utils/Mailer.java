package com.tekup.circuithub.utils;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Mailer {
    private static final ExecutorService EXEC = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "mailer");
        t.setDaemon(true);
        return t;
    });

    private static Properties config;

    private static synchronized Properties config() {
        if (config != null) return config;
        Properties p = new Properties();
        try (InputStream in = Mailer.class.getResourceAsStream("/com/tekup/circuithub/mail.properties")) {
            if (in == null) throw new IOException("mail.properties not found on classpath");
            p.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load mail.properties: " + e.getMessage(), e);
        }
        config = p;
        return p;
    }

    public static CompletableFuture<Void> sendVerificationCode(String to, String code) {
        return CompletableFuture.runAsync(() -> sendBlocking(to, code), EXEC);
    }

    private static void sendBlocking(String to, String code) {
        Properties cfg = config();
        String host = cfg.getProperty("smtp.host");
        String port = cfg.getProperty("smtp.port", "587");
        String user = cfg.getProperty("smtp.user");
        String pass = cfg.getProperty("smtp.password");
        String from = cfg.getProperty("smtp.from", user);
        String fromName = cfg.getProperty("smtp.from.name", "CircuitHub");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(user, pass);
            }
        });

        try {
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from, fromName));
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            msg.setSubject("Your CircuitHub verification code");
            msg.setContent(buildHtml(code), "text/html; charset=utf-8");
            Transport.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email: " + e.getMessage(), e);
        }
    }

    private static String buildHtml(String code) {
        return """
                <div style="font-family: Inter, Segoe UI, sans-serif; background:#0F172A; color:#E2E8F0; padding:32px;">
                  <h2 style="color:#10B981; margin:0 0 16px;">CircuitHub</h2>
                  <p>Use this code to finish creating your account:</p>
                  <p style="font-family: 'JetBrains Mono', Consolas, monospace; font-size:32px; letter-spacing:8px; color:#10B981; background:#1E293B; padding:16px 24px; border-radius:8px; display:inline-block;">
                    %s
                  </p>
                  <p style="color:#94A3B8; font-size:13px; margin-top:24px;">This code expires in 10 minutes. If you didn't request this, you can ignore this email.</p>
                </div>
                """.formatted(code);
    }
}
