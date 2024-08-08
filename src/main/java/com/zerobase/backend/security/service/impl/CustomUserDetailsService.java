package com.zerobase.backend.security.service.impl;

import static com.zerobase.backend.exception.ErrorCode.ENTREPRENEUR_NOT_FOUND;
import static com.zerobase.backend.exception.ErrorCode.USER_NOT_FOUND;

import com.zerobase.backend.domain.Entrepreneur;
import com.zerobase.backend.domain.User;
import com.zerobase.backend.repository.EntrepreneurRepository;
import com.zerobase.backend.repository.UserRepository;
import com.zerobase.backend.security.dto.EntrepreneurCustomUserDetails;
import com.zerobase.backend.security.dto.UserCustomUserDetails;
import com.zerobase.backend.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  private final EntrepreneurRepository entrepreneurRepository;

  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

    boolean isUser = userRepository.existsByEmail(email);
    boolean isEntrepreneur = entrepreneurRepository.existsByEmail(email);

    if (isUser) {
      User findUser = findUserByEmail(email);
      return new UserCustomUserDetails(findUser);
    }
    if (isEntrepreneur) {
      Entrepreneur findEntrepreneur = findEntrepreneurByEmail(email);
      return new EntrepreneurCustomUserDetails(findEntrepreneur);
    }

    return null;
  }

  private Entrepreneur findEntrepreneurByEmail(String email) {
    return entrepreneurRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(ENTREPRENEUR_NOT_FOUND));
  }

  private User findUserByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new CustomException(USER_NOT_FOUND));
  }
}
