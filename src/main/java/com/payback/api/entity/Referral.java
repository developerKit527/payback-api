package com.payback.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "referrals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "referrer_id", nullable = false)
    private User referrer;

    @ManyToOne
    @JoinColumn(name = "referred_id", nullable = false)
    private User referred;

    @Column(nullable = false)
    private BigDecimal bonusCashback;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
