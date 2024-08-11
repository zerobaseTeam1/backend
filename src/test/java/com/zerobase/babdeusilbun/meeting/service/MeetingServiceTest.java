package com.zerobase.babdeusilbun.meeting.service;

import static com.zerobase.babdeusilbun.enums.MeetingStatus.*;
import static com.zerobase.babdeusilbun.enums.PurchaseType.*;
import static org.assertj.core.api.Assertions.*;

import com.zerobase.babdeusilbun.domain.Meeting;
import com.zerobase.babdeusilbun.domain.Store;
import com.zerobase.babdeusilbun.dto.DeliveryAddressDto;
import com.zerobase.babdeusilbun.dto.MeetingDto;
import com.zerobase.babdeusilbun.dto.MetAddressDto;
import com.zerobase.babdeusilbun.enums.PurchaseType;
import com.zerobase.babdeusilbun.meeting.dto.MeetingRequest;
import com.zerobase.babdeusilbun.repository.MeetingRepository;
import com.zerobase.babdeusilbun.repository.StoreRepository;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MeetingServiceTest {

  @Autowired
  private MeetingRepository meetingRepository;

  @Autowired
  private MeetingService meetingService;
  @Autowired
  private StoreRepository storeRepository;

  @BeforeEach
  void init() {

  }

  @Test
  @DisplayName("모임 조회 - 결제 마감 시간 순")
  void searchMeetingByDeadline() {

    PageRequest pageRequest = PageRequest.of(0, 4);

    String searchMenu = "";

    Page<MeetingDto> pageable =
        meetingService.getAllMeetingList(1L, "deadline", searchMenu, pageRequest);

    List<MeetingDto> content = pageable.getContent();

    assertThat(pageable.getSize()).isEqualTo(4);
    assertThat(pageable.getTotalElements()).isEqualTo(3);
    assertThat(pageable.getTotalPages()).isEqualTo(1);
    assertThat(content.size()).isEqualTo(3);

    assertThat(content.getFirst().getPaymentAvailableAt()).isEqualTo(
        LocalDateTime.of(2024, Month.AUGUST, 24, 12, 0)
    );
    assertThat(content.getLast().getPaymentAvailableAt()).isEqualTo(
        LocalDateTime.of(2024, Month.AUGUST, 24, 12, 20)
    );
  }

  @Test
  @DisplayName("모임 조회 - 배송시간 시간 순")
  void searchMeetingByShippingTime() {

    PageRequest pageRequest = PageRequest.of(0, 4);
    String searchMenu = "";

    Page<MeetingDto> pageable =
        meetingService.getAllMeetingList(1L, "shipping-time", searchMenu, pageRequest);

    List<MeetingDto> content = pageable.getContent();

    assertThat(pageable.getSize()).isEqualTo(4);
    assertThat(pageable.getTotalElements()).isEqualTo(3);
    assertThat(pageable.getTotalPages()).isEqualTo(1);
    assertThat(content.size()).isEqualTo(3);

    assertThat(content.getFirst().getDeliveredAt()).isEqualTo(
        LocalDateTime.of(2024, Month.AUGUST, 24, 12, 0)
    );
    assertThat(content.getLast().getDeliveredAt()).isEqualTo(
        LocalDateTime.of(2024, Month.AUGUST, 24, 12, 20)
    );
  }

  @Test
  @DisplayName("모임 조회 - 배달비 순")
  void searchMeetingByShippingFee() {

    PageRequest pageRequest = PageRequest.of(0, 4);
    String searchMenu = "";

    Page<MeetingDto> pageable =
        meetingService.getAllMeetingList(1L, "shipping-fee", searchMenu, pageRequest);

    List<MeetingDto> content = pageable.getContent();

    assertThat(pageable.getSize()).isEqualTo(4);
    assertThat(pageable.getTotalElements()).isEqualTo(3);
    assertThat(pageable.getTotalPages()).isEqualTo(1);
    assertThat(content.size()).isEqualTo(3);

    assertThat(content.getFirst().getDeliveryFee()).isEqualTo(3000);
    assertThat(content.getLast().getDeliveryFee()).isEqualTo(4000);
  }

  @Test
  @DisplayName("모임 조회 - 최소 주문금액 순")
  void searchMeetingByMinPrice() {

    PageRequest pageRequest = PageRequest.of(0, 4);
    String searchMenu = "";

    Page<MeetingDto> pageable =
        meetingService.getAllMeetingList(1L, "min-price", searchMenu, pageRequest);

    List<MeetingDto> content = pageable.getContent();
    Long storeAId = content.getLast().getStoreId();
    Long storeBId = content.getFirst().getStoreId();
    Store findStoreA = storeRepository.findById(storeBId).get();
    Store findStoreB = storeRepository.findById(storeAId).get();

    assertThat(pageable.getSize()).isEqualTo(4);
    assertThat(pageable.getTotalElements()).isEqualTo(3);
    assertThat(pageable.getTotalPages()).isEqualTo(1);
    assertThat(content.size()).isEqualTo(3);

    assertThat(findStoreA.getMinOrderAmount()).isEqualTo(1000L);
    assertThat(findStoreB.getMinOrderAmount()).isEqualTo(2000L);
  }

  @Test
  @DisplayName("모임 조회 - 검색어")
  void searchMeetingBySearchMenu() {

    PageRequest pageRequest = PageRequest.of(0, 4);
    String searchMenu = "storeB";

    Page<MeetingDto> pageable =
        meetingService.getAllMeetingList(1L, "min-price", searchMenu, pageRequest);

    List<MeetingDto> content = pageable.getContent();
    Long storeBId = content.getLast().getStoreId();
    Store findStoreB = storeRepository.findById(storeBId).get();

    assertThat(pageable.getSize()).isEqualTo(4);
    assertThat(pageable.getTotalElements()).isEqualTo(1);
    assertThat(pageable.getTotalPages()).isEqualTo(1);
    assertThat(content.size()).isEqualTo(1);

    assertThat(findStoreB.getName()).isEqualTo("storeB");
  }

  @Test
  @DisplayName("모임 정보 조회")
  void getMeetingInfo() {
    Long meetingAId = 1L;
    Long storeBId = 1L;
    Long userAId = 1L;

    Meeting findMeeting = meetingRepository.findById(meetingAId).get();

    assertThat(findMeeting.getId()).isEqualTo(meetingAId);
    assertThat(findMeeting.getStore().getId()).isEqualTo(storeBId);
    assertThat(findMeeting.getLeader().getId()).isEqualTo(userAId);

    assertThat(findMeeting.getPurchaseType()).isEqualTo(DELIVERY_TOGETHER);
    assertThat(findMeeting.getStatus()).isEqualTo(GATHERING);
    assertThat(findMeeting.getPaymentAvailableDt())
        .isEqualTo(LocalDateTime.of(2024, Month.AUGUST, 24, 12, 10));
  }

  @Test
  @DisplayName("모임 생성")
  void createMeeting() {

    UserDetails userDetails = new User("testuser@test.com", "",
        List.of(new SimpleGrantedAuthority("user")));

    LocalDateTime now = LocalDateTime.now();
    DeliveryAddressDto deliveryAddressDto = DeliveryAddressDto.builder()
        .deliveryPostal("dp").deliveryStreetAddress("ds").deliveryDetailAddress("dd").build();
    MetAddressDto metAddressDto = MetAddressDto.builder()
        .metPostal("mp").metDetailAddress("md").metStreetAddress("ms").build();

    MeetingRequest.Create request = MeetingRequest.Create.builder()
        .storeId(1L)
        .purchaseType(DELIVERY_TOGETHER)
        .minHeadcount(10)
        .maxHeadcount(20)
        .isEarlyPaymentAvailable(true)
        .paymentAvailableAt(now)
        .deliveryAddress(deliveryAddressDto)
        .metAddress(metAddressDto)
        .build();

    int preSize = meetingRepository.findAll().size();
    meetingService.createMeeting(request, userDetails);
    List<Meeting> findAll = meetingRepository.findAll();
    Meeting savedMeeting = findAll.getLast();

    assertThat(findAll.size()).isEqualTo(preSize + 1);
    assertThat(savedMeeting.getLeader().getEmail()).isEqualTo("testuser@test.com");
    assertThat(savedMeeting.getStore().getId()).isEqualTo(1L);
    assertThat(savedMeeting.getPurchaseType()).isEqualTo(DELIVERY_TOGETHER);
    assertThat(savedMeeting.getMinHeadcount()).isEqualTo(10);
    assertThat(savedMeeting.getMaxHeadcount()).isEqualTo(20);
    assertThat(savedMeeting.getIsEarlyPaymentAvailable()).isTrue();
    assertThat(savedMeeting.getPaymentAvailableDt()).isEqualTo(now);
    assertThat(savedMeeting.getDeliveredAddress().getPostal())
        .isEqualTo(deliveryAddressDto.getDeliveryPostal());
    assertThat(savedMeeting.getDeliveredAddress().getStreetAddress())
        .isEqualTo(deliveryAddressDto.getDeliveryStreetAddress());
    assertThat(savedMeeting.getDeliveredAddress().getDetailAddress())
        .isEqualTo(deliveryAddressDto.getDeliveryDetailAddress());
    assertThat(savedMeeting.getMetAddress().getPostal())
        .isEqualTo(metAddressDto.getMetPostal());
    assertThat(savedMeeting.getMetAddress().getStreetAddress())
        .isEqualTo(metAddressDto.getMetStreetAddress());
    assertThat(savedMeeting.getMetAddress().getDetailAddress())
        .isEqualTo(metAddressDto.getMetDetailAddress());
  }


}