package com.payback.api.repository;

import com.payback.api.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
    
    List<Merchant> findAllByOrderByClickCountDesc();
}
