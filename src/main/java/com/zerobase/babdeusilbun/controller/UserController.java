package com.zerobase.babdeusilbun.controller;

import static com.zerobase.babdeusilbun.dto.UserDto.*;
import static com.zerobase.babdeusilbun.swagger.annotation.UserSwagger.*;
import static org.springframework.http.HttpStatus.PARTIAL_CONTENT;
import static org.springframework.http.MediaType.*;

import com.zerobase.babdeusilbun.domain.Address;
import com.zerobase.babdeusilbun.dto.UserDto;
import com.zerobase.babdeusilbun.security.dto.CustomUserDetails;
import com.zerobase.babdeusilbun.service.UserService;
import com.zerobase.babdeusilbun.swagger.annotation.UserSwagger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;


  /**
   * 내 정보 조회
   */
  @PreAuthorize("hasRole('USER')")
  @GetMapping("/my-page")
  @GetMyProfileSwagger
  public ResponseEntity<MyPage> getMyProfile(@AuthenticationPrincipal CustomUserDetails user) {
    MyPage myPage= userService.getMyPage(user.getId());
    return ResponseEntity.ok(myPage);
  }

  /**
   * 내 정보 수정
   */
  @PreAuthorize("hasRole('USER')")
  @PatchMapping(consumes = {MULTIPART_FORM_DATA_VALUE, APPLICATION_JSON_VALUE})
  @UpdateProfileSwagger
  public ResponseEntity<Void> updateProfile(
      @AuthenticationPrincipal CustomUserDetails user,
      @RequestPart(value = "file", required = false) MultipartFile image,
      @RequestPart(value = "request") UpdateRequest request) {
    UpdateRequest changeRequest = userService.updateProfile(user.getId(), image, request);

    return (image != null && changeRequest.getImage() == null) ||
        (request.getSchoolId() != null && changeRequest.getSchoolId() == null) ||
        (request.getMajorId() != null && changeRequest.getMajorId() == null) ?
        ResponseEntity.status(PARTIAL_CONTENT).build() : ResponseEntity.ok().build();
  }

  /**
   * 내 주소 수정
   */
  @PreAuthorize("hasRole('USER')")
  @PutMapping("/address")
  @UpdateAddressSwagger
  public ResponseEntity<Void> updateAddress(
          @AuthenticationPrincipal CustomUserDetails user,
          @Validated @RequestBody UpdateAddress updateAddress) {
    userService.updateAddress(user.getId(), updateAddress);
    return ResponseEntity.ok().build();
  }

  /**
   * 내 계좌 수정
   */
  @PreAuthorize("hasRole('USER')")
  @PutMapping("/account")
  @UpdateAccountSwagger
  public ResponseEntity<Void> updateAccount(
          @AuthenticationPrincipal CustomUserDetails user,
          @Validated @RequestBody UpdateAccount updateAccount) {
      userService.updateAccount(user.getId(), updateAccount);
      return ResponseEntity.ok().build();
    };

  /**
   * 프로필 조회
   */
  @GetMapping("/{userId}")
  @GetMeetingInfoSwagger
  public ResponseEntity<Profile> getMeetingInfo(@PathVariable Long userId) {
    Profile userProfile = userService.getUserProfile(userId);
    return ResponseEntity.ok(userProfile);
  }
}
