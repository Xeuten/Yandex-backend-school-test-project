package com.example.gradledemo.controllers;

import com.example.gradledemo.services.SystemItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
public class DeleteFileController {

    @Autowired
    private SystemItemService deleteService;

    @DeleteMapping({"/delete/{id}", "/delete"})
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> deleteFiles(@PathVariable @Nullable String id,
                                                               @RequestHeader(HttpHeaders.USER_AGENT) String userAgentValue) {
        return deleteService.deleteFilesResponse(id, userAgentValue);
    }

}
