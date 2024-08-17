package com.zerobase.babdeusilbun.service;

import static com.zerobase.babdeusilbun.exception.ErrorCode.ALREADY_EXIST_STORE;
import static com.zerobase.babdeusilbun.exception.ErrorCode.ENTREPRENEUR_NOT_FOUND;
import static com.zerobase.babdeusilbun.exception.ErrorCode.NO_AUTH_ON_STORE;
import static com.zerobase.babdeusilbun.exception.ErrorCode.STORE_NOT_FOUND;
import static com.zerobase.babdeusilbun.util.ImageUtility.STORE_IMAGE_FOLDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.zerobase.babdeusilbun.component.ImageComponent;
import com.zerobase.babdeusilbun.domain.Category;
import com.zerobase.babdeusilbun.domain.Entrepreneur;
import com.zerobase.babdeusilbun.domain.Store;
import com.zerobase.babdeusilbun.domain.StoreCategory;
import com.zerobase.babdeusilbun.domain.StoreImage;
import com.zerobase.babdeusilbun.dto.AddressDto;
import com.zerobase.babdeusilbun.dto.CategoryDto.IdsRequest;
import com.zerobase.babdeusilbun.dto.CategoryDto.Information;
import com.zerobase.babdeusilbun.dto.StoreDto.CreateRequest;
import com.zerobase.babdeusilbun.exception.CustomException;
import com.zerobase.babdeusilbun.repository.CategoryRepository;
import com.zerobase.babdeusilbun.repository.EntrepreneurRepository;
import com.zerobase.babdeusilbun.repository.SchoolRepository;
import com.zerobase.babdeusilbun.repository.StoreCategoryRepository;
import com.zerobase.babdeusilbun.repository.StoreImageRepository;
import com.zerobase.babdeusilbun.repository.StoreRepository;
import com.zerobase.babdeusilbun.service.impl.StoreServiceImpl;
import com.zerobase.babdeusilbun.util.TestEntrepreneurUtility;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class StoreServiceTest {
  @Mock
  private EntrepreneurRepository entrepreneurRepository;

  @Mock
  private StoreRepository storeRepository;

  @Mock
  private StoreImageRepository imageRepository;

  @Mock
  private SchoolRepository schoolRepository;

  @Mock
  private CategoryRepository categoryRepository;

  @Mock
  private StoreCategoryRepository storeCategoryRepository;

  @Mock
  private ImageComponent imageComponent;

  @InjectMocks
  private StoreServiceImpl storeService;

  private final List<Category> categories = List.of(
      Category.builder().id(1L).name("야식").build(),
      Category.builder().id(2L).name("점심").build(),
      Category.builder().id(3L).name("저녁").build()
  );

  private final CreateRequest createRequest = CreateRequest.builder()
      .name("가짜 상점")
      .description("가짜 상점입니다.")
      .minPurchasePrice(10000L)
      .minDeliveryTime(30)
      .maxDeliveryTime(60)
      .deliveryPrice(1000L)
      .address(new AddressDto("00000", "도로명주소", "상세주소"))
      .phoneNumber("01012345678")
      .openTime(LocalTime.of(9, 0))
      .closeTime(LocalTime.of(21, 0))
      .build();

  @DisplayName("가게 생성 성공 사례 테스트")
  @Test
  void createStoreSuccess() {
    //given
    Entrepreneur entrepreneur = TestEntrepreneurUtility.getEntrepreneur();
    List<MultipartFile> images = List.of();
    Store expected = createRequest.toEntity(entrepreneur);

    //when
    when(entrepreneurRepository.findByIdAndDeletedAtIsNull(eq(TestEntrepreneurUtility.getEntrepreneur().getId())))
        .thenReturn(Optional.of(entrepreneur));
    when(storeRepository.existsByEntrepreneurAndNameAndAddressAndDeletedAtIsNull(
        eq(entrepreneur), eq(createRequest.getName()), any()))
        .thenReturn(false);
    when(imageComponent.uploadImageList(eq(images), eq(STORE_IMAGE_FOLDER))).thenReturn(List.of("url1", "url2"));

    ArgumentCaptor<Store> storeCaptor = ArgumentCaptor.forClass(Store.class);
    ArgumentCaptor<List<StoreImage>> storeImagesCaptor = ArgumentCaptor.forClass(List.class);

    //then
    int uploadedImageCount = storeService.createStore(entrepreneur.getId(), images, createRequest);

    verify(storeRepository, times(1)).save(storeCaptor.capture());
    verify(imageRepository, times(1)).saveAll(storeImagesCaptor.capture());

    assertEquals(2, uploadedImageCount);
    List<StoreImage> savedImages = storeImagesCaptor.getValue();
    assertEquals("url1", savedImages.get(0).getUrl());
    assertTrue(savedImages.get(0).getIsRepresentative());
    assertEquals("url2", savedImages.get(1).getUrl());
    assertFalse(savedImages.get(1).getIsRepresentative());

    assertEquals(
        expected.getEntrepreneur().getId(), storeCaptor.getValue().getEntrepreneur().getId());
    assertEquals(expected.getName(), storeCaptor.getValue().getName());
    assertEquals(AddressDto.fromEntity(expected.getAddress()),
        AddressDto.fromEntity(storeCaptor.getValue().getAddress()));
  }

  @DisplayName("가게 생성 실패(중복된 가게 등록 요청)")
  @Test
  void createStoreFailedAlreadyExists() {
    //given
    Entrepreneur entrepreneur = TestEntrepreneurUtility.getEntrepreneur();
    List<MultipartFile> images = List.of();

    //when
    when(entrepreneurRepository.findByIdAndDeletedAtIsNull(eq(entrepreneur.getId()))).thenReturn(Optional.of(entrepreneur));
    when(storeRepository.existsByEntrepreneurAndNameAndAddressAndDeletedAtIsNull(
        eq(entrepreneur), eq(createRequest.getName()), any())).thenReturn(true);

    //then
    CustomException exception = assertThrows(CustomException.class, () -> {
      storeService.createStore(entrepreneur.getId(), images, createRequest);
    });

    assertEquals(ALREADY_EXIST_STORE, exception.getErrorCode());
  }

  @DisplayName("가게 생성 실패(사업가 미존재)")
  @Test
  void createStoreFailedEntrepreneurNotFound() {
    //given
    Entrepreneur entrepreneur = TestEntrepreneurUtility.getEntrepreneur();
    List<MultipartFile> images = List.of();

    //when
    when(entrepreneurRepository.findByIdAndDeletedAtIsNull(any())).thenReturn(Optional.empty());

    //then
    CustomException exception = assertThrows(CustomException.class, () -> {
      storeService.createStore(entrepreneur.getId(), images, createRequest);
    });

    assertEquals(ENTREPRENEUR_NOT_FOUND, exception.getErrorCode());
  }

  @DisplayName("카테고리 조회 성공")
  @Test
  void getAllCategoriesSuccess() {
    //given
    Pageable pageable = PageRequest.of(0, 10);
    List<Information> informationList = categories.stream().map(Information::fromEntity).toList();

    Page<Information> categoryPage = new PageImpl<>(informationList, pageable, categories.size());

    //when
    when(categoryRepository.getAllCategories(eq(pageable.getPageNumber()), eq(pageable.getPageSize())))
        .thenReturn(categoryPage);

    Page<Information> result = storeService.getAllCategories(pageable.getPageNumber(), pageable.getPageSize());

    //then
    assertEquals(categoryPage, result);
    assertEquals(categories.size(), result.getTotalElements());
  }

  @DisplayName("상점에 카테고리 등록 성공")
  @Test
  void enrollToCategorySuccess() {
    //given
    Entrepreneur entrepreneur = TestEntrepreneurUtility.getEntrepreneur();
    Store store = createRequest.toEntity(entrepreneur);

    List<Long> existingCategoryIds = List.of(1L);
    IdsRequest idsRequest = new IdsRequest(Set.of(2L, 3L));

    //when
    when(entrepreneurRepository.findByIdAndDeletedAtIsNull(eq(entrepreneur.getId())))
        .thenReturn(Optional.of(entrepreneur));
    when(storeRepository.findByIdAndDeletedAtIsNull(eq(store.getId())))
        .thenReturn(Optional.of(store));
    when(storeCategoryRepository.findCategoryIdsByStore(eq(store)))
        .thenReturn(existingCategoryIds);
    when(categoryRepository.findById(2L))
        .thenReturn(Optional.of(Category.builder().id(2L).name("2L의 카테고리").build()));
    when(categoryRepository.findById(3L))
        .thenReturn(Optional.of(Category.builder().id(3L).name("2L의 카테고리").build()));

    int successCount = storeService.enrollToCategory(entrepreneur.getId(), store.getId(), idsRequest);

    //then
    ArgumentCaptor<StoreCategory> storeCategoryCaptor = ArgumentCaptor.forClass(StoreCategory.class);
    verify(storeCategoryRepository, times(2)).save(storeCategoryCaptor.capture());

    assertEquals(2, successCount);
    assertEquals(3L, storeCategoryCaptor.getValue().getCategory().getId());

    verify(storeCategoryRepository, times(2)).save(any(StoreCategory.class));
  }

  @DisplayName("상점에 카테고리 등록 실패(상점 미존재)")
  @Test
  void enrollToCategoryFailedStoreNotFound() {
    //given
    Entrepreneur entrepreneur = TestEntrepreneurUtility.getEntrepreneur();
    IdsRequest idsRequest = new IdsRequest(Set.of(2L));

    //when
    when(entrepreneurRepository.findByIdAndDeletedAtIsNull(eq(entrepreneur.getId())))
        .thenReturn(Optional.of(entrepreneur));
    when(storeRepository.findByIdAndDeletedAtIsNull(eq(1L)))
        .thenReturn(Optional.empty());

    //then
    CustomException exception = assertThrows(CustomException.class, () -> {
      storeService.enrollToCategory(entrepreneur.getId(), 1L, idsRequest);
    });

    assertEquals(STORE_NOT_FOUND, exception.getErrorCode());
  }

  @DisplayName("상점에 카테고리 등록 실패(로그인한 이용자가 상점주인이 아님)")
  @Test
  void enrollToCategoryFailedNoAuth() {
    //given
    Entrepreneur entrepreneur = TestEntrepreneurUtility.getEntrepreneur();
    Store store = createRequest.toEntity(new Entrepreneur());

    IdsRequest idsRequest = new IdsRequest(Set.of(1L));

    //when
    when(entrepreneurRepository.findByIdAndDeletedAtIsNull(eq(entrepreneur.getId())))
        .thenReturn(Optional.of(entrepreneur));
    when(storeRepository.findByIdAndDeletedAtIsNull(eq(store.getId())))
        .thenReturn(Optional.of(store));

    //then
    CustomException exception = assertThrows(CustomException.class, () -> {
      storeService.enrollToCategory(entrepreneur.getId(), store.getId(), idsRequest);
    });

    assertEquals(NO_AUTH_ON_STORE, exception.getErrorCode());
  }

  @DisplayName("상점에 카테고리 삭제 성공")
  @Test
  void deleteOnCategorySuccess() {
    //given
    Entrepreneur entrepreneur = TestEntrepreneurUtility.getEntrepreneur();
    Store store = createRequest.toEntity(entrepreneur);
    IdsRequest idsRequest = new IdsRequest(Set.of(1L, 2L));

    //when
    when(entrepreneurRepository.findByIdAndDeletedAtIsNull(eq(entrepreneur.getId())))
        .thenReturn(Optional.of(entrepreneur));
    when(storeRepository.findByIdAndDeletedAtIsNull(eq(store.getId())))
        .thenReturn(Optional.of(store));
    when(storeCategoryRepository.deleteByStoreAndCategory_IdIn(eq(store), eq(idsRequest.getCategoryIds())))
        .thenReturn(2);

    //then
    int deletedCount = storeService.deleteOnCategory(entrepreneur.getId(), store.getId(), idsRequest);

    assertEquals(2, deletedCount);
    verify(storeCategoryRepository, times(1))
        .deleteByStoreAndCategory_IdIn(eq(store), eq(idsRequest.getCategoryIds()));
  }

  @DisplayName("상점에 카테고리 삭제 실패(상점 미존재)")
  @Test
  void deleteOnCategoryFailedStoreNotFound() {
    //given
    Entrepreneur entrepreneur = TestEntrepreneurUtility.getEntrepreneur();
    IdsRequest idsRequest = new IdsRequest(Set.of(2L));

    //when
    when(entrepreneurRepository.findByIdAndDeletedAtIsNull(eq(entrepreneur.getId())))
        .thenReturn(Optional.of(entrepreneur));
    when(storeRepository.findByIdAndDeletedAtIsNull(eq(1L)))
        .thenReturn(Optional.empty());

    //then
    CustomException exception = assertThrows(CustomException.class, () -> {
      storeService.deleteOnCategory(entrepreneur.getId(), 1L, idsRequest);
    });

    assertEquals(STORE_NOT_FOUND, exception.getErrorCode());
  }

  @DisplayName("상점에 카테고리 삭제 실패(로그인한 이용자가 상점주인이 아님)")
  @Test
  void deleteOnCategoryFailedNoAuth() {
    //given
    Entrepreneur entrepreneur = TestEntrepreneurUtility.getEntrepreneur();
    Store store = createRequest.toEntity(new Entrepreneur());

    IdsRequest idsRequest = new IdsRequest(Set.of(1L));

    //when
    when(entrepreneurRepository.findByIdAndDeletedAtIsNull(eq(entrepreneur.getId())))
        .thenReturn(Optional.of(entrepreneur));
    when(storeRepository.findByIdAndDeletedAtIsNull(eq(store.getId())))
        .thenReturn(Optional.of(store));

    //then
    CustomException exception = assertThrows(CustomException.class, () -> {
      storeService.deleteOnCategory(entrepreneur.getId(), store.getId(), idsRequest);
    });

    assertEquals(NO_AUTH_ON_STORE, exception.getErrorCode());
  }
}
