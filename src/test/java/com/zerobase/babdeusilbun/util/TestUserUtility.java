package com.zerobase.babdeusilbun.util;

import com.zerobase.babdeusilbun.domain.School;
import com.zerobase.babdeusilbun.domain.User;
import com.zerobase.babdeusilbun.security.dto.CustomUserDetails;

public class TestUserUtility {
  private final static School school = School.builder()
      .id(1L)
      .name("가짜 학교")
      .campus("가짜캠퍼스")
      .build();
  private final static User user = User.builder()
      .id(1L)
      .email("test@test.com")
      .password("password")
      .school(school)
      .build();

  public static CustomUserDetails createTestUser() {
    return new CustomUserDetails(user);
  }

  public static User getUser() {
    return user;
  }
}
