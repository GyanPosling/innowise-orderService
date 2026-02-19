package com.innowise.orderservice.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.model.dto.request.ItemCreateRequest;
import com.innowise.orderservice.model.dto.request.ItemUpdateRequest;
import com.innowise.orderservice.model.dto.response.ItemResponse;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.repository.ItemRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @InjectMocks
    private ItemServiceImpl itemService;

    @Mock
    private ItemRepository itemRepository;

    @Spy
    private ItemMapper itemMapper = Mappers.getMapper(ItemMapper.class);

    private ItemCreateRequest createRequest;
    private Item item;

    @BeforeEach
    void setUp() {
        createRequest = ItemCreateRequest.builder()
                .name("car")
                .price(BigDecimal.valueOf(700))
                .build();
        item = Item.builder()
                .id(1L)
                .name("car")
                .price(BigDecimal.valueOf(700))
                .build();
    }

    @Test
    void create_shouldReturnItemResponse_whenRequestIsValid() {
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ItemResponse response = itemService.create(createRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("car", response.getName());
        assertEquals(BigDecimal.valueOf(700), response.getPrice());
    }

    @Test
    void getById_shouldReturnItemResponse_whenItemExists() {
        when(itemRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(item));

        ItemResponse response = itemService.getById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("car", response.getName());
        assertEquals(BigDecimal.valueOf(700), response.getPrice());
    }

    @Test
    void getById_shouldThrowException_whenItemNotFound() {
        when(itemRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.getById(1L));
    }

    @Test
    void getAll_shouldReturnMappedItems() {
        Item other = Item.builder().id(2L).name("bike").price(BigDecimal.valueOf(500)).build();
        when(itemRepository.findAll()).thenReturn(List.of(item, other));

        List<ItemResponse> responses = itemService.getAll();

        assertEquals(2, responses.size());
        assertEquals("car", responses.get(0).getName());
        assertEquals("bike", responses.get(1).getName());
    }

    @Test
    void update_shouldUpdateFields_whenItemExists() {
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .name("bike")
                .price(BigDecimal.valueOf(900))
                .build();
        when(itemRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemResponse response = itemService.update(1L, request);

        assertEquals("bike", response.getName());
        assertEquals(BigDecimal.valueOf(900), response.getPrice());
        verify(itemRepository).save(item);
    }

    @Test
    void update_shouldLeavePriceUnchanged_whenPriceIsNull() {
        ItemUpdateRequest request = ItemUpdateRequest.builder()
                .name("bike")
                .build();
        when(itemRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemResponse response = itemService.update(1L, request);

        assertEquals("bike", response.getName());
        assertEquals(BigDecimal.valueOf(700), response.getPrice());
    }

    @Test
    void update_shouldThrowException_whenItemNotFound() {
        when(itemRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());
        ItemUpdateRequest request = ItemUpdateRequest.builder().name("Updated").build();

        assertThrows(ItemNotFoundException.class, () -> itemService.update(1L, request));
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void delete_shouldRemoveItem_whenItemExists() {
        when(itemRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(item));

        itemService.delete(1L);

        verify(itemRepository).delete(item);
    }

    @Test
    void delete_shouldThrowException_whenItemNotFound() {
        when(itemRepository.findByIdAndDeletedAtIsNull(anyLong())).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.delete(1L));
        verify(itemRepository, never()).delete(any(Item.class));
    }
}
