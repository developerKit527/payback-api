package com.payback.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "merchant_offers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MerchantOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "discount_text")
    private String discountText;

    @Column(name = "affiliate_url")
    private String affiliateUrl;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
