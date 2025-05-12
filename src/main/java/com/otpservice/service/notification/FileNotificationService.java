package com.otpservice.service.notification;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileNotificationService implements NotificationService {
    private final String filePath;
    
    public FileNotificationService(String filePath) {
        this.filePath = filePath;
    }
    
    @Override
    public void sendOtpCode(String recipient, String code) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath, true))) {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            writer.printf("[%s] OTP code for %s: %s%n", timestamp, recipient, code);
        } catch (IOException e) {
            throw new Exception("Failed to write OTP code to file", e);
        }
    }
} 