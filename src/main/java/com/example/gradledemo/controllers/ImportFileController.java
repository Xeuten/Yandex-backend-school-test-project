package com.example.gradledemo.controllers;

import com.example.gradledemo.model.SystemItemImportRequest;
import com.example.gradledemo.services.SystemItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class ImportFileController {

    @Autowired
    private SystemItemService importService;

    @PostMapping("/imports")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> importFiles(@RequestBody SystemItemImportRequest request) {
        return importService.importFilesResponse(request);
    }

}
