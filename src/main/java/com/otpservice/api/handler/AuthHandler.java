package com.otpservice.api.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.otpservice.model.User;
import com.otpservice.service.JwtService;
import com.otpservice.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AuthHandler implements HttpHandler {
    private final UserService userService;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;
    
    public AuthHandler(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            sendError(exchange, 405, "Method not allowed");
            return;
        }
        
        String path = exchange.getRequestURI().getPath();
        
        try {
            if (path.equals("/api/auth/register")) {
                handleRegister(exchange);
            } else if (path.equals("/api/auth/login")) {
                handleLogin(exchange);
            } else {
                sendError(exchange, 404, "Not found");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }
    
    private void handleRegister(HttpExchange exchange) throws IOException {
        Map<String, String> request = readRequest(exchange);
        
        String username = request.get("username");
        String password = request.get("password");
        String role = request.get("role");
        
        if (username == null || password == null || role == null) {
            sendError(exchange, 400, "Missing required fields");
            return;
        }
        
        try {
            User user = userService.registerUser(username, password, role);
            sendResponse(exchange, 201, Map.of("message", "User registered successfully"));
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (Exception e) {
            sendError(exchange, 500, "Failed to register user");
        }
    }
    
    private void handleLogin(HttpExchange exchange) throws IOException {
        Map<String, String> request = readRequest(exchange);
        
        String username = request.get("username");
        String password = request.get("password");
        
        if (username == null || password == null) {
            sendError(exchange, 400, "Missing username or password");
            return;
        }
        
        try {
            var user = userService.authenticateUser(username, password);
            if (user.isPresent()) {
                String token = jwtService.generateToken(user.get());
                sendResponse(exchange, 200, Map.of("token", token));
            } else {
                sendError(exchange, 401, "Invalid username or password");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Failed to authenticate user");
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