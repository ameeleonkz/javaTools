package com.otpservice.service.notification;

import com.otpservice.util.NotificationConfig;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.StandardCharsets;

public class TelegramNotificationService implements NotificationService {
    private final String botToken;
    private final String chatId;
    
    public TelegramNotificationService(String botToken, String chatId) {
        this.botToken = botToken;
        this.chatId = chatId;
    }
    
    @Override
    public void sendOtpCode(String recipient, String code) throws Exception {
        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
        String message = "Your OTP code is: " + code;
        
        // Настройка таймаута из конфигурации
        int timeout = NotificationConfig.getNotificationTimeout();
        RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(timeout)
            .setConnectionRequestTimeout(timeout)
            .setSocketTimeout(timeout)
            .build();
        
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build()) {
            HttpPost httpPost = new HttpPost(url);
            
            String jsonBody = String.format(
                "{\"chat_id\":\"%s\",\"text\":\"%s\"}",
                chatId,
                message
            );
            
            StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");
            
            try (CloseableHttpResponse response = client.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getStatusLine().getStatusCode() != 200) {
                    throw new Exception("Failed to send Telegram message: " + responseBody);
                }
            }
        }
    }
} 