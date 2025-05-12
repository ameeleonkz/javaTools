package com.otpservice.dao;

import com.otpservice.model.OtpConfig;
import com.otpservice.util.DatabaseUtil;

import java.sql.*;
import java.util.Optional;

public class OtpConfigDao {
    
    public OtpConfig getConfig() throws SQLException {
        String sql = "SELECT * FROM otp_config ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                OtpConfig config = new OtpConfig();
                config.setId(rs.getLong("id"));
                config.setCodeLength(rs.getInt("code_length"));
                config.setExpirationTimeMinutes(rs.getInt("expiration_time_minutes"));
                config.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                config.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return config;
            }
            
            // Если конфигурации нет, создаем дефолтную
            return createDefaultConfig();
        }
    }
    
    private OtpConfig createDefaultConfig() throws SQLException {
        String sql = "INSERT INTO otp_config (code_length, expiration_time_minutes) " +
                    "VALUES (6, 5) RETURNING id, created_at, updated_at";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                OtpConfig config = new OtpConfig(6, 5);
                config.setId(rs.getLong("id"));
                config.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                config.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return config;
            }
            
            throw new SQLException("Failed to create default OTP configuration");
        }
    }
    
    public void updateConfig(OtpConfig config) throws SQLException {
        String sql = "UPDATE otp_config SET code_length = ?, expiration_time_minutes = ?, " +
                    "updated_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, config.getCodeLength());
            pstmt.setInt(2, config.getExpirationTimeMinutes());
            pstmt.setTimestamp(3, Timestamp.valueOf(config.getUpdatedAt()));
            pstmt.setLong(4, config.getId());
            
            pstmt.executeUpdate();
        }
    }
} 