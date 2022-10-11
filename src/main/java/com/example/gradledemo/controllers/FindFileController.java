package com.example.gradledemo.controllers;

import com.example.gradledemo.services.SystemItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
public class FindFileController {

    @Autowired
    private SystemItemService findService;

    @GetMapping({"/nodes/{id}", "/nodes"})
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> findFiles(@PathVariable @Nullable String id,
                                                             @RequestHeader(HttpHeaders.USER_AGENT) String userAgentValue) {
        return findService.findFilesResponse(id, userAgentValue);
    }

}
