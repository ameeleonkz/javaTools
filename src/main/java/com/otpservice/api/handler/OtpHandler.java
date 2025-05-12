package com.otpservice.api.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.otpservice.model.OtpCode;
import com.otpservice.service.JwtService;
import com.otpservice.service.OtpService;
import com.otpservice.service.notification.NotificationManager;
import com.otpservice.util.NotificationConfig;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OtpHandler implements HttpHandler {
    private final OtpService otpService;
    private final JwtService jwtService;
    private final NotificationManager notificationManager;
    private final ObjectMapper objectMapper;
    
    public OtpHandler(OtpService otpService, JwtService jwtService, NotificationManager notificationManager) {
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.notificationManager = notificationManager;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }
        
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            sendError(exchange, 401, "Missing or invalid token");
            return;
        }
        
        token = token.substring(7);
        if (!jwtService.isTokenValid(token)) {
            sendError(exchange, 401, "Invalid token");
            return;
        }
        
        String path = exchange.getRequestURI().getPath();
        
        try {
            if (path.equals("/api/otp/generate")) {
                handleGenerateOtp(exchange, token);
            } else if (path.equals("/api/otp/validate")) {
                handleValidateOtp(exchange, token);
            } else {
                sendError(exchange, 404, "Not found");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    private void handleGenerateOtp(HttpExchange exchange, String token) throws IOException {
        Map<String, String> request = readRequest(exchange);
        String recipient = request.get("recipient");
        
        if (recipient == null) {
            sendError(exchange, 400, "Missing recipient");
            return;
        }
        
        try {
            Long userId = jwtService.getUserIdFromToken(token);
            String operationId = UUID.randomUUID().toString();
            
            OtpCode otpCode = otpService.generateOtpCode(userId, operationId);
            
            // Выводим OTP в консоль, если включено в настройках
            if (NotificationConfig.isConsoleOtpOutputEnabled()) {
                System.out.println("\n=== OTP Code Generated ===");
                System.out.println("Recipient: " + recipient);
                System.out.println("OTP Code: " + otpCode.getCode());
                System.out.println("Operation ID: " + operationId);
                System.out.println("Expires at: " + otpCode.getExpiresAt());
                System.out.println("========================\n");
            }
            
            notificationManager.sendOtpCode(recipient, otpCode.getCode());
            
            Map<String, String> response = new HashMap<>();
            response.put("operationId", operationId);
            response.put("message", "OTP code sent successfully");
            
            sendResponse(exchange, 200, response);
        } catch (Exception e) {
            sendError(exchange, 500, "Failed to generate and send OTP code");
        }
    }
    
    private void handleValidateOtp(HttpExchange exchange, String token) throws IOException {
        Map<String, String> request = readRequest(exchange);
        String code = request.get("code");
        
        if (code == null) {
            sendError(exchange, 400, "Missing OTP code");
            return;
        }
        
        try {
            Long userId = jwtService.getUserIdFromToken(token);
            boolean isValid = otpService.validateOtpCode(userId, code);
            
            if (isValid) {
                sendResponse(exchange, 200, Map.of("message", "OTP code validated successfully"));
            } else {
                sendError(exchange, 400, "Invalid OTP code");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Failed to validate OTP code");
        }
    }
    
    private Map<String, String> readRequest(HttpExchange exchange) throws IOException {
        try (InputStream requestBody = exchange.getRequestBody()) {
            return objectMapper.readValue(requestBody, Map.class);
        }
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, Object response) throws IOException {
        String jsonResponse = objectMapper.writeValueAsString(response);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.length());
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        }
    }
    
    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        sendResponse(exchange, statusCode, error);
    }
} 