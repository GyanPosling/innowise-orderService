package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.model.dto.request.ItemCreateRequest;
import com.innowise.orderservice.model.dto.request.ItemUpdateRequest;
import com.innowise.orderservice.model.dto.response.ItemResponse;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.service.ItemService;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional
    public ItemResponse create(ItemCreateRequest item) {
        Item entity = itemMapper.toEntity(item);
        return itemMapper.toResponse(itemRepository.save(entity));
    }

    @Override
    public ItemResponse getById(Long id) {
        Item entity = itemRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        return itemMapper.toResponse(entity);
    }

    @Override
    public List<ItemResponse> getAll() {
        return itemRepository.findAll().stream()
                .map(itemMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemResponse update(Long id, ItemUpdateRequest item) {
        Item existing = itemRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        if (item.getName() != null) {
            existing.setName(item.getName());
        }
        if (item.getPrice() != null) {
            existing.setPrice(item.getPrice());
        }
        return itemMapper.toResponse(itemRepository.save(existing));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Item existing = itemRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        itemRepository.delete(existing);
    }
}
