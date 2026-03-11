package com.payback.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "merchants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Merchant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "logo_url")
    private String logoUrl;
    
    @Column(name = "cashback_rate", nullable = false)
    private Double cashbackRate;
    
    @Column(name = "manual_tracking_url")
    private String manualTrackingUrl;
    
    @Column(name = "click_count", nullable = false)
    private Long clickCount = 0L;
}
