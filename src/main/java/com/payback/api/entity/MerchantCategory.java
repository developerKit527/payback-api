package com.payback.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "merchant_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false)
    private String name;

    private String icon;

    @Column(name = "affiliate_url")
    private String affiliateUrl;

    @Column(name = "cashback_rate")
    private Double cashbackRate;

    @Column(name = "display_order")
    private Integer displayOrder;
}
