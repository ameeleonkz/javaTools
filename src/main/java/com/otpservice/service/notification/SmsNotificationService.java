package com.otpservice.service.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmsNotificationService implements NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(SmsNotificationService.class);
    private final String sourceAddress;
    
    public SmsNotificationService(String sourceAddress) {
        this.sourceAddress = sourceAddress;
    }
    
    @Override
    public void sendOtpCode(String recipient, String code) throws Exception {
        // Моковая реализация - просто логируем отправку
        logger.info("Mock SMS sent to {} from {} with code: {}", recipient, sourceAddress, code);
        // Всегда успешно
    }
} 