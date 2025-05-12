package com.otpservice.dao;

import com.otpservice.model.OtpCode;
import com.otpservice.util.DatabaseUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OtpCodeDao {
    
    public OtpCode create(OtpCode otpCode) throws SQLException {
        String sql = "INSERT INTO otp_codes (user_id, code, status, operation_id, expires_at) " +
                    "VALUES (?, ?, ?, ?, ?) RETURNING id, created_at";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, otpCode.getUserId());
            pstmt.setString(2, otpCode.getCode());
            pstmt.setString(3, otpCode.getStatus());
            pstmt.setString(4, otpCode.getOperationId());
            pstmt.setTimestamp(5, Timestamp.valueOf(otpCode.getExpiresAt()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    otpCode.setId(rs.getLong("id"));
                    otpCode.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
            }
            
            return otpCode;
        }
    }
    
    public Optional<OtpCode> findActiveByUserIdAndCode(Long userId, String code) throws SQLException {
        String sql = "SELECT * FROM otp_codes WHERE user_id = ? AND code = ? AND status = 'ACTIVE' " +
                    "AND expires_at > ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.setString(2, code);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    OtpCode otpCode = new OtpCode();
                    otpCode.setId(rs.getLong("id"));
                    otpCode.setUserId(rs.getLong("user_id"));
                    otpCode.setCode(rs.getString("code"));
                    otpCode.setStatus(rs.getString("status"));
                    otpCode.setOperationId(rs.getString("operation_id"));
                    otpCode.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    otpCode.setExpiresAt(rs.getTimestamp("expires_at").toLocalDateTime());
                    otpCode.setUsedAt(rs.getTimestamp("used_at") != null ? 
                        rs.getTimestamp("used_at").toLocalDateTime() : null);
                    return Optional.of(otpCode);
                }
            }
            
            return Optional.empty();
        }
    }
    
    public void markAsUsed(Long otpId) throws SQLException {
        String sql = "UPDATE otp_codes SET status = 'USED', used_at = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.setLong(2, otpId);
            pstmt.executeUpdate();
        }
    }
    
    public void markExpiredCodes() throws SQLException {
        String sql = "UPDATE otp_codes SET status = 'EXPIRED' " +
                    "WHERE status = 'ACTIVE' AND expires_at <= ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            pstmt.executeUpdate();
        }
    }
    
    public void deleteUserOtpCodes(Long userId) throws SQLException {
        String sql = "DELETE FROM otp_codes WHERE user_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, userId);
            pstmt.executeUpdate();
        }
    }
} 