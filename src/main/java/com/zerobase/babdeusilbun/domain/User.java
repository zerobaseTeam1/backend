package com.zerobase.babdeusilbun.domain;


import com.zerobase.babdeusilbun.dto.UserDto;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 일반 이용자
 */
@Entity @Getter
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email")
})
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Builder
public class User extends BaseEntity{

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "user_id", nullable = false)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "school_id", nullable = false)
  private School school;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "major_id", nullable = false)
  private Major major;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private Boolean isBanned;

  private String image;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String nickname;

  @Column(nullable = false)
  private String phoneNumber;

  @Embedded
  private BankAccount bankAccount;

  @Column(nullable = false)
  private Long point;

  @Embedded
  private Address address;

  private LocalDateTime deletedAt;


  public void withdraw() {
    this.deletedAt = LocalDateTime.now();
  }

  public void updateSchool(School school) {
    this.school = school;
  }

  public void updateMajor(Major major) {
    this.major = major;
  }

  public void update(UserDto.UpdateRequest request) {
    if (request.getNickname() != null) this.nickname = request.getNickname();
    if (request.getPassword() != null) this.password = request.getPassword();
    if (request.getImage() != null) this.image = request.getImage();
    if (request.getPhoneNumber() != null) this.phoneNumber = request.getPhoneNumber();
  }

  public void updateAddress(UserDto.UpdateAddress address) {
    this.address = Address.builder()
            .postal(address.getPostal())
            .streetAddress(address.getStreetAddress())
            .detailAddress(address.getDetailAddress())
            .build();
  }
}
