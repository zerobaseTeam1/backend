package com.zerobase.babdeusilbun.controller;

import com.zerobase.babdeusilbun.dto.PurchaseSnapshotDto;
import com.zerobase.babdeusilbun.security.dto.CustomUserDetails;
import com.zerobase.babdeusilbun.service.SnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class SnapshotController {

  private final SnapshotService snapshotService;

  // 주문 후 공동 주문 스냅샷 리스트 조회
  // /api/users/meetings/{meetingId}/snapshots/post-order/team
  @PreAuthorize("hasRole('USER')")
  @GetMapping("/meetings/{meetingId}/snapshots/post-order/team")
  public ResponseEntity<Page<PurchaseSnapshotDto>> getTeamPurchaseSnapshots(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @PathVariable Long meetingId, Pageable pageable
  ) {

    return ResponseEntity.ok(
        snapshotService.getTeamPurchaseSnapshots(userDetails.getId(), meetingId, pageable)
    );
  }

  // 주문 후 개별 주문 스냅샷 리스트 조회
  // /api/users/{userId}/meetings/{meetingId}/snapshots/post-purchases/individuals

  // 주문 후 주문 스냅샷 조회
  // /api/users/{userId}/meetings/{meetingId}/snapshots/post-purchases/purchases

  // 포인트 스냅샷 리스트 조회
  // /api/users/{userId}/snapshots/points

  // /api/users/{userId}/snapshots/points
  // /api/users/{userId}/meetings/{meetingId}/snapshots/payments

}
