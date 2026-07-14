package com.brandon.finance.email.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.brandon.finance.shared.base.entity.BaseEntity;
import com.brandon.finance.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Setter;

@Setter
@Entity
@Table(name = "email_verification_token")
public class EmailVerificaitonToken extends BaseEntity {
    
    @Column(nullable = false, unique = true, length = 255)
    private String token;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime expires_at;

    @Column(nullable = false)
    private boolean used = false;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime created_at;
}
