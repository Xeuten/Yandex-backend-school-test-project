package com.example.gradledemo.controllers;

import com.example.gradledemo.services.SystemItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
public class FindFileController {

    @Autowired
    SystemItemService findService;

    @GetMapping({"/nodes/{id}", "/nodes"})
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> findFiles(@PathVariable @Nullable String id) {
        if(id == null) {
            return findService.code400Response();
        }
        return findService.findFilesResponse(id);
    }



}
