package com.otpservice.service;

import com.otpservice.dao.OtpCodeDao;
import com.otpservice.dao.OtpConfigDao;
import com.otpservice.model.OtpCode;
import com.otpservice.model.OtpConfig;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class OtpService {
    private final OtpCodeDao otpCodeDao;
    private final OtpConfigDao otpConfigDao;
    private final SecureRandom random;
    
    public OtpService() {
        this.otpCodeDao = new OtpCodeDao();
        this.otpConfigDao = new OtpConfigDao();
        this.random = new SecureRandom();
    }
    
    public OtpCode generateOtpCode(Long userId, String operationId) throws SQLException {
        OtpConfig config = otpConfigDao.getConfig();
        
        // Генерируем случайный код заданной длины
        String code = generateRandomCode(config.getCodeLength());
        
        // Создаем OTP код
        OtpCode otpCode = new OtpCode(
            userId,
            code,
            "ACTIVE",
            operationId,
            LocalDateTime.now().plusMinutes(config.getExpirationTimeMinutes())
        );
        
        return otpCodeDao.create(otpCode);
    }
    
    public boolean validateOtpCode(Long userId, String code) throws SQLException {
        Optional<OtpCode> otpCode = otpCodeDao.findActiveByUserIdAndCode(userId, code);
        
        if (otpCode.isPresent()) {
            otpCodeDao.markAsUsed(otpCode.get().getId());
            return true;
        }
        
        return false;
    }
    
    public void markExpiredCodes() throws SQLException {
        otpCodeDao.markExpiredCodes();
    }
    
    public void deleteUserOtpCodes(Long userId) throws SQLException {
        otpCodeDao.deleteUserOtpCodes(userId);
    }
    
    public OtpConfig getOtpConfig() throws SQLException {
        return otpConfigDao.getConfig();
    }
    
    public void updateOtpConfig(OtpConfig config) throws SQLException {
        config.setUpdatedAt(LocalDateTime.now());
        otpConfigDao.updateConfig(config);
    }
    
    private String generateRandomCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }
} 