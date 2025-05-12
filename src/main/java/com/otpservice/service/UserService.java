package com.otpservice.service;

import com.otpservice.dao.UserDao;
import com.otpservice.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

public class UserService {
    private final UserDao userDao;
    
    public UserService() {
        this.userDao = new UserDao();
    }
    
    public User registerUser(String username, String password, String role) throws SQLException {
        // Проверяем, не пытаемся ли создать второго админа
        if ("ADMIN".equals(role) && userDao.isAdminExists()) {
            throw new IllegalArgumentException("Admin user already exists");
        }
        
        // Проверяем, не существует ли уже пользователь с таким именем
        Optional<User> existingUser = userDao.findByUsername(username);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User with this username already exists");
        }
        
        // Хешируем пароль
        String hashedPassword = hashPassword(password);
        
        // Создаем нового пользователя
        User user = new User(username, hashedPassword, role);
        return userDao.create(user);
    }
    
    public Optional<User> authenticateUser(String username, String password) throws SQLException {
        Optional<User> user = userDao.findByUsername(username);
        
        if (user.isPresent() && verifyPassword(password, user.get().getPassword())) {
            return user;
        }
        
        return Optional.empty();
    }
    
    public List<User> getAllUsers() throws SQLException {
        return userDao.findAllUsers();
    }
    
    public boolean deleteUser(Long userId) throws SQLException {
        return userDao.deleteUser(userId);
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    private boolean verifyPassword(String inputPassword, String storedPassword) {
        String hashedInput = hashPassword(inputPassword);
        return hashedInput.equals(storedPassword);
    }
} 