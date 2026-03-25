package com.payback.api.service;

import com.payback.api.dto.WithdrawalDTO;
import com.payback.api.dto.WithdrawalRequestDTO;

import java.util.List;

public interface WithdrawalService {
    WithdrawalDTO createWithdrawalRequest(Long userId, WithdrawalRequestDTO requestDTO);
    WithdrawalDTO approveWithdrawal(Long withdrawalId);
    WithdrawalDTO markAsPaid(Long withdrawalId);
    List<WithdrawalDTO> getWithdrawalHistory(Long userId);
}
