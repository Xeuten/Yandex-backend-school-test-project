package com.example.gradledemo.controllers;

import com.example.gradledemo.model.SystemItem;
import com.example.gradledemo.persistence.Database;
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
    Database base;
    @Autowired
    private SystemItemRepository systemItemRepository;

    @DeleteMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> deleteFiles(@PathVariable String id) throws NoSuchElementException {
        Optional<SystemItem> item = systemItemRepository.findById(id);
        HashMap<String, Object> map = new HashMap<>();
        if(item.isPresent()) {
           if(item.get().isValidImport(false)) {
               base.systemItemDelete(item.get());
               return new ResponseEntity<>(HttpStatus.OK);
           } else {
               map.put("code", 400);
               map.put("message", "Validation Failed");
               return ResponseEntity.status(400).body(map);
           }
        } else {
            map.put("code", 404);
            map.put("message", "Item not found");
            return ResponseEntity.status(404).body(map);
        }
    }
}
