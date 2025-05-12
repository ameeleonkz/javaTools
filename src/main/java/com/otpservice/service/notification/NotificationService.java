package com.otpservice.service.notification;

public interface NotificationService {
    void sendOtpCode(String recipient, String code) throws Exception;
} 