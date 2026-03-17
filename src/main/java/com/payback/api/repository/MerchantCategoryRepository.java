package com.payback.api.repository;

import com.payback.api.entity.MerchantCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MerchantCategoryRepository extends JpaRepository<MerchantCategory, Long> {
    List<MerchantCategory> findByMerchantIdOrderByDisplayOrderAsc(Long merchantId);
}
