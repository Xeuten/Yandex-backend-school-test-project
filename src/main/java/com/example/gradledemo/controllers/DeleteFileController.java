package com.example.gradledemo.controllers;

import com.example.gradledemo.model.SystemItem;
import com.example.gradledemo.services.SystemItemService;
import com.example.gradledemo.persistence.SystemItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
public class DeleteFileController {

    @Autowired
    SystemItemService deleteService;

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> deleteFiles(@PathVariable String id) {
        return deleteService.deleteFilesResponse(id);
    }
}
