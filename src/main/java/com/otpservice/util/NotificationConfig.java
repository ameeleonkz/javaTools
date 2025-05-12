package com.otpservice.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class NotificationConfig {
    private static final Properties properties = new Properties();
    
    static {
        try (InputStream input = NotificationConfig.class.getClassLoader()
                .getResourceAsStream("notification.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find notification.properties");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading notification configuration", e);
        }
    }
    
    // Email configuration
    public static String getEmailHost() {
        return properties.getProperty("email.host");
    }
    
    public static String getEmailPort() {
        return properties.getProperty("email.port");
    }
    
    public static String getEmailUsername() {
        return properties.getProperty("email.username");
    }
    
    public static String getEmailPassword() {
        return properties.getProperty("email.password");
    }
    
    // SMS configuration
    public static String getSmsSourceAddress() {
        return properties.getProperty("sms.source.address");
    }
    
    // Telegram configuration
    public static String getTelegramBotToken() {
        return properties.getProperty("telegram.bot.token");
    }
    
    public static String getTelegramChatId() {
        return properties.getProperty("telegram.chat.id");
    }
    
    // File notification configuration
    public static String getFileNotificationPath() {
        return properties.getProperty("file.notification.path");
    }
    
    // Console output configuration
    public static boolean isConsoleOtpOutputEnabled() {
        return Boolean.parseBoolean(properties.getProperty("console.otp.output.enabled", "false"));
    }
    
    // Notification timeout configuration
    public static int getNotificationTimeout() {
        return Integer.parseInt(properties.getProperty("notification.timeout", "4000"));
    }
} 