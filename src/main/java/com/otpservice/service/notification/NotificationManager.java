package com.otpservice.service.notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    private final List<NotificationService> notificationServices;
    
    public NotificationManager() {
        this.notificationServices = new ArrayList<>();
    }
    
    public void addNotificationService(NotificationService service) {
        notificationServices.add(service);
    }
    
    public void sendOtpCode(String recipient, String code) throws Exception {
        List<Exception> exceptions = new ArrayList<>();
        boolean anySuccess = false;
        
        // Пробуем отправить через все каналы
        for (NotificationService service : notificationServices) {
            try {
                service.sendOtpCode(recipient, code);
                anySuccess = true;
            } catch (Exception e) {
                exceptions.add(e);
            }
        }
        
        // Если ни один канал не сработал, выбрасываем исключение
        if (!anySuccess) {
            throw new Exception("Failed to send OTP code through any channel: " + exceptions);
        }
    }
} 