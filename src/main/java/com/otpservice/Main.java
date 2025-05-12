package com.otpservice;

import com.otpservice.api.handler.AdminHandler;
import com.otpservice.api.handler.AuthHandler;
import com.otpservice.api.handler.OtpHandler;
import com.otpservice.service.JwtService;
import com.otpservice.service.OtpService;
import com.otpservice.service.UserService;
import com.otpservice.service.notification.*;
import com.otpservice.util.NotificationConfig;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final int PORT = 8080;
    private static final int EXPIRED_CODES_CHECK_INTERVAL_MINUTES = 1;
    
    public static void main(String[] args) throws IOException {
        // Инициализация сервисов
        UserService userService = new UserService();
        OtpService otpService = new OtpService();
        JwtService jwtService = new JwtService();
        
        // Инициализация менеджера уведомлений
        NotificationManager notificationManager = new NotificationManager();
        
        // Добавление сервисов уведомлений с конфигурацией из файла
        // Сначала добавляем SMS сервис (приоритетный)
        notificationManager.addNotificationService(new SmsNotificationService(
            NotificationConfig.getSmsSourceAddress()
        ));
        
        // Затем добавляем остальные сервисы
        notificationManager.addNotificationService(new EmailNotificationService(
            NotificationConfig.getEmailHost(),
            NotificationConfig.getEmailPort(),
            NotificationConfig.getEmailUsername(),
            NotificationConfig.getEmailPassword()
        ));
        
        notificationManager.addNotificationService(new TelegramNotificationService(
            NotificationConfig.getTelegramBotToken(),
            NotificationConfig.getTelegramChatId()
        ));
        
        notificationManager.addNotificationService(new FileNotificationService(
            NotificationConfig.getFileNotificationPath()
        ));
        
        // Создание HTTP сервера
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Регистрация обработчиков
        server.createContext("/api/auth", new AuthHandler(userService, jwtService));
        server.createContext("/api/admin", new AdminHandler(userService, otpService, jwtService));
        server.createContext("/api/otp", new OtpHandler(otpService, jwtService, notificationManager));
        
        // Настройка пула потоков
        server.setExecutor(Executors.newFixedThreadPool(10));
        
        // Запуск сервера
        server.start();
        System.out.println("Server started on port " + PORT);
        
        // Запуск периодической проверки просроченных кодов
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(
            () -> {
                try {
                    otpService.markExpiredCodes();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            },
            0,
            EXPIRED_CODES_CHECK_INTERVAL_MINUTES,
            TimeUnit.MINUTES
        );
    }
} 