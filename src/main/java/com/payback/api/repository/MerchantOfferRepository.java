package com.payback.api.repository;

import com.payback.api.entity.MerchantOffer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MerchantOfferRepository extends JpaRepository<MerchantOffer, Long> {
    List<MerchantOffer> findByMerchantIdAndIsActiveTrue(Long merchantId);
}
