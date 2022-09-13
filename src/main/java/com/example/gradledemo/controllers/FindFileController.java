package com.example.gradledemo.controllers;

import com.example.gradledemo.persistence.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
public class FindFileController {

    @Autowired
    Database base;

    @GetMapping("/nodes/{id}")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> findFiles(@PathVariable String id) {
        HashMap<String, Object> item = base.systemItemFilling(id);
        if (item.containsKey("id")) {
            return ResponseEntity.status(HttpStatus.OK).body(item);
        } else if (item.containsValue(400)) {
            return ResponseEntity.status(400).body(item);
        } else {
            return ResponseEntity.status(404).body(item);
        }
    }
}
