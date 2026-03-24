package com.payback.api.service;

import com.payback.api.dto.UserDTO;
import com.payback.api.entity.User;
import com.payback.api.exception.EntityNotFoundException;
import com.payback.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserDTO updateUserProfile(Long userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        user.setName(name);
        User updatedUser = userRepository.save(user);
        
        return new UserDTO(updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail());
    }
}
