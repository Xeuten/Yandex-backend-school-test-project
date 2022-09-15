package com.example.gradledemo.services;

import com.example.gradledemo.model.SystemItem;
import com.example.gradledemo.persistence.SystemItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemItemServiceTest {

    @Mock
    private SystemItemRepository systemItemRepository;

    @InjectMocks
    private SystemItemService systemItemService = new SystemItemService();

    @Test
    void shouldFindFile() {
        SystemItem item = new SystemItem();
        item.setId("элемент_1_3");
        item.setSize(4L);
        item.setType("FILE");
        item.setUrl("/file/url1");
        item.setParentId("элемент_1_2");
        item.setDate(new Date());
        when(systemItemRepository.findById("элемент_1_3")).thenReturn(Optional.of(item));
        ResponseEntity<HashMap<String, Object>> responseEntity = systemItemService.findFilesResponse("элемент_1_3");
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    void shouldNotFindFile() {
        when(systemItemRepository.findById("non_existent_id")).thenReturn(Optional.empty());
        ResponseEntity<HashMap<String, Object>> responseEntity = systemItemService.findFilesResponse("non_existent_id");
        assertEquals(responseEntity.getStatusCode(), HttpStatus.NOT_FOUND);
    }

}