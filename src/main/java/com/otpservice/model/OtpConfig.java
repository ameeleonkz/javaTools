package com.otpservice.model;

import java.time.LocalDateTime;

public class OtpConfig {
    private Long id;
    private Integer codeLength;
    private Integer expirationTimeMinutes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OtpConfig() {}

    public OtpConfig(Integer codeLength, Integer expirationTimeMinutes) {
        this.codeLength = codeLength;
        this.expirationTimeMinutes = expirationTimeMinutes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(Integer codeLength) {
        this.codeLength = codeLength;
    }

    public Integer getExpirationTimeMinutes() {
        return expirationTimeMinutes;
    }

    public void setExpirationTimeMinutes(Integer expirationTimeMinutes) {
        this.expirationTimeMinutes = expirationTimeMinutes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
} 