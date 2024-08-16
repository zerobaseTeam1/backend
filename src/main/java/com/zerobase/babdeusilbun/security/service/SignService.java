package com.zerobase.babdeusilbun.security.service;

import com.zerobase.babdeusilbun.dto.SignDto.VerifyPasswordRequest;
import com.zerobase.babdeusilbun.dto.SignDto.VerifyPasswordResponse;
import com.zerobase.babdeusilbun.security.dto.SignRequest.BusinessSignUp;
import com.zerobase.babdeusilbun.security.dto.SignRequest.SignIn;
import com.zerobase.babdeusilbun.security.dto.SignRequest.UserSignUp;
import com.zerobase.babdeusilbun.security.dto.WithdrawalRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface SignService {
  VerifyPasswordResponse passwordConfirm(VerifyPasswordRequest request, Long userId);

  boolean isEmailIsUnique(String email);

  void userSignUp(UserSignUp request);

  String userSignIn(SignIn request);

  String entrepreneurSignIn(SignIn request);

  void entrepreneurSignUp(BusinessSignUp request);

  void logout(String jwtToken);

  void userWithdrawal(String jwtToken, WithdrawalRequest request);

  void entrepreneurWithdrawal(String jwtToken, WithdrawalRequest request);
}
