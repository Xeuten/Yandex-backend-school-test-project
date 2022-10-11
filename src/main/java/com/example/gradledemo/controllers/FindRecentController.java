package com.example.gradledemo.controllers;

import com.example.gradledemo.services.SystemItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
public class FindRecentController {

    @Autowired
    private SystemItemService findRecentService;

    @PostMapping("/updates")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> findRecentFiles(@RequestBody @Nullable String requestDate,
                                                                   @RequestHeader(HttpHeaders.USER_AGENT) String userAgentValue) {
        return findRecentService.findRecentFilesResponse(requestDate, userAgentValue);
    }

}
