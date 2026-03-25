package com.payback.api.service.impl;

import com.payback.api.dto.WithdrawalDTO;
import com.payback.api.dto.WithdrawalRequestDTO;
import com.payback.api.entity.User;
import com.payback.api.entity.Withdrawal;
import com.payback.api.entity.WithdrawalStatus;
import com.payback.api.exception.EntityNotFoundException;
import com.payback.api.repository.UserRepository;
import com.payback.api.repository.WithdrawalRepository;
import com.payback.api.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    private final WithdrawalRepository withdrawalRepository;
    private final UserRepository userRepository;

    @Value("${withdrawal.minimum.amount:100.00}")
    private BigDecimal minimumAmount;

    @Override
    @Transactional
    public WithdrawalDTO createWithdrawalRequest(Long userId, WithdrawalRequestDTO requestDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Validate amount against balance
        if (requestDTO.getAmount().compareTo(user.getCashbackBalance()) > 0) {
            throw new IllegalArgumentException("Withdrawal amount exceeds available balance");
        }

        // Validate minimum amount
        if (requestDTO.getAmount().compareTo(minimumAmount) < 0) {
            throw new IllegalArgumentException("Minimum withdrawal amount is " + minimumAmount + " rupees");
        }

        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setUser(user);
        withdrawal.setUpiId(requestDTO.getUpiId());
        withdrawal.setAmount(requestDTO.getAmount());
        withdrawal.setStatus(WithdrawalStatus.PENDING);
        withdrawal.setRequestedAt(LocalDateTime.now());

        Withdrawal savedWithdrawal = withdrawalRepository.save(withdrawal);
        return convertToDTO(savedWithdrawal);
    }

    @Override
    @Transactional
    public WithdrawalDTO approveWithdrawal(Long withdrawalId) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new EntityNotFoundException("Withdrawal request not found"));

        if (withdrawal.getStatus() != WithdrawalStatus.PENDING) {
            throw new IllegalArgumentException("Cannot approve withdrawal in current status: " + withdrawal.getStatus());
        }

        withdrawal.setStatus(WithdrawalStatus.APPROVED);
        Withdrawal updatedWithdrawal = withdrawalRepository.save(withdrawal);
        return convertToDTO(updatedWithdrawal);
    }

    @Override
    @Transactional
    public WithdrawalDTO markAsPaid(Long withdrawalId) {
        Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId)
                .orElseThrow(() -> new EntityNotFoundException("Withdrawal request not found"));

        if (withdrawal.getStatus() != WithdrawalStatus.APPROVED) {
            throw new IllegalArgumentException("Cannot mark as paid. Withdrawal must be approved first. Current status: " + withdrawal.getStatus());
        }

        User user = withdrawal.getUser();
        
        // Deduct amount from user's cashback balance
        BigDecimal newBalance = user.getCashbackBalance().subtract(withdrawal.getAmount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient balance to complete withdrawal");
        }
        
        user.setCashbackBalance(newBalance);
        userRepository.save(user);

        withdrawal.setStatus(WithdrawalStatus.PAID);
        withdrawal.setProcessedAt(LocalDateTime.now());
        Withdrawal updatedWithdrawal = withdrawalRepository.save(withdrawal);
        
        return convertToDTO(updatedWithdrawal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WithdrawalDTO> getWithdrawalHistory(Long userId) {
        List<Withdrawal> withdrawals = withdrawalRepository.findByUserIdOrderByRequestedAtDesc(userId);
        return withdrawals.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private WithdrawalDTO convertToDTO(Withdrawal withdrawal) {
        WithdrawalDTO dto = new WithdrawalDTO();
        dto.setId(withdrawal.getId());
        dto.setUpiId(withdrawal.getUpiId());
        dto.setAmount(withdrawal.getAmount());
        dto.setStatus(withdrawal.getStatus());
        dto.setRequestedAt(withdrawal.getRequestedAt());
        dto.setProcessedAt(withdrawal.getProcessedAt());
        return dto;
    }
}
