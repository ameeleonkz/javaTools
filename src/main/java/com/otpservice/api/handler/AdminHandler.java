package com.otpservice.api.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.otpservice.model.OtpConfig;
import com.otpservice.model.User;
import com.otpservice.service.JwtService;
import com.otpservice.service.OtpService;
import com.otpservice.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminHandler implements HttpHandler {
    private final UserService userService;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    
    public AdminHandler(UserService userService, OtpService otpService, JwtService jwtService) {
        this.userService = userService;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // Регистрируем модуль для Java 8 дат
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        System.out.println("Received token: " + token);
        
        if (token == null || !token.startsWith("Bearer ")) {
            sendError(exchange, 401, "Missing or invalid token");
            return;
        }
        
        token = token.substring(7);
        System.out.println("Token after Bearer removal: " + token);
        
        if (!jwtService.isTokenValid(token)) {
            System.out.println("Token validation failed");
            sendError(exchange, 401, "Invalid token");
            return;
        }
        
        String role = jwtService.getRoleFromToken(token);
        System.out.println("User role from token: " + role);
        
        if (!"ADMIN".equals(role)) {
            System.out.println("Access denied: user is not an admin");
            sendError(exchange, 403, "Access denied");
            return;
        }
        
        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();
        System.out.println("Handling request: " + method + " " + path);
        
        try {
            if (path.equals("/api/admin/users")) {
                if (method.equals("GET")) {
                    handleGetUsers(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            } else if (path.equals("/api/admin/users/delete")) {
                if (method.equals("POST")) {
                    handleDeleteUser(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            } else if (path.equals("/api/admin/otp/config")) {
                if (method.equals("GET")) {
                    handleGetOtpConfig(exchange);
                } else if (method.equals("PUT")) {
                    handleUpdateOtpConfig(exchange);
                } else {
                    sendError(exchange, 405, "Method not allowed");
                }
            } else {
                sendError(exchange, 404, "Not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    private void handleGetUsers(HttpExchange exchange) throws IOException {
        try {
            List<User> users = userService.getAllUsers();
            // Преобразуем список пользователей в список Map для исключения паролей
            List<Map<String, Object>> userMaps = users.stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("username", user.getUsername());
                    userMap.put("role", user.getRole());
                    userMap.put("createdAt", user.getCreatedAt());
                    return userMap;
                })
                .toList();
            
            sendResponse(exchange, 200, userMaps);
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Failed to get users: " + e.getMessage());
        }
    }
    
    private void handleDeleteUser(HttpExchange exchange) throws IOException {
        Map<String, Object> request = readRequest(exchange);
        Object userIdObj = request.get("userId");
        System.out.println("Delete user request: " + request);
        
        if (userIdObj == null) {
            sendError(exchange, 400, "Missing userId");
            return;
        }
        
        try {
            Long userId;
            if (userIdObj instanceof Integer) {
                userId = ((Integer) userIdObj).longValue();
            } else if (userIdObj instanceof String) {
                userId = Long.parseLong((String) userIdObj);
            } else {
                sendError(exchange, 400, "Invalid userId format");
                return;
            }
            
            System.out.println("Attempting to delete user with ID: " + userId);
            
            // Сначала удаляем OTP коды пользователя
            System.out.println("Cleaning up OTP codes for user: " + userId);
            otpService.deleteUserOtpCodes(userId);
            
            // Затем удаляем самого пользователя
            if (userService.deleteUser(userId)) {
                System.out.println("User deleted successfully");
                sendResponse(exchange, 200, Map.of("message", "User deleted successfully"));
            } else {
                System.out.println("User not found with ID: " + userId);
                sendError(exchange, 404, "User not found");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid userId format: " + e.getMessage());
            sendError(exchange, 400, "Invalid userId format");
        } catch (Exception e) {
            System.out.println("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            sendError(exchange, 500, "Failed to delete user: " + e.getMessage());
        }
    }
    
    private void handleGetOtpConfig(HttpExchange exchange) throws IOException {
        try {
            OtpConfig config = otpService.getOtpConfig();
            sendResponse(exchange, 200, config);
        } catch (Exception e) {
            sendError(exchange, 500, "Failed to get OTP configuration");
        }
    }
    
    private void handleUpdateOtpConfig(HttpExchange exchange) throws IOException {
        Map<String, Object> request = readRequest(exchange);
        
        Object codeLengthObj = request.get("codeLength");
        Object expirationTimeObj = request.get("expirationTimeMinutes");
        
        if (codeLengthObj == null || expirationTimeObj == null) {
            sendError(exchange, 400, "Missing required fields");
            return;
        }
        
        try {
            int codeLength;
            int expirationTime;
            
            if (codeLengthObj instanceof Integer) {
                codeLength = (Integer) codeLengthObj;
            } else if (codeLengthObj instanceof String) {
                codeLength = Integer.parseInt((String) codeLengthObj);
            } else {
                sendError(exchange, 400, "Invalid codeLength format");
                return;
            }
            
            if (expirationTimeObj instanceof Integer) {
                expirationTime = (Integer) expirationTimeObj;
            } else if (expirationTimeObj instanceof String) {
                expirationTime = Integer.parseInt((String) expirationTimeObj);
            } else {
                sendError(exchange, 400, "Invalid expirationTimeMinutes format");
                return;
            }
            
            OtpConfig config = otpService.getOtpConfig();
            config.setCodeLength(codeLength);
            config.setExpirationTimeMinutes(expirationTime);
            
            otpService.updateOtpConfig(config);
            sendResponse(exchange, 200, Map.of("message", "OTP configuration updated successfully"));
        } catch (NumberFormatException e) {
            sendError(exchange, 400, "Invalid number format");
        } catch (Exception e) {
            sendError(exchange, 500, "Failed to update OTP configuration");
        }
    }
    
    private Map<String, Object> readRequest(HttpExchange exchange) throws IOException {
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