package com.example.gradledemo.controllers;

import com.example.gradledemo.model.SystemItem;
import com.example.gradledemo.persistence.SystemItemRepository;
import com.example.gradledemo.model.SystemItemImportRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Optional;

@RestController
public class ImportFileController {

    @Autowired
    private SystemItemRepository systemItemRepository;

    @PostMapping("/imports")
    @ResponseBody
    public ResponseEntity<HashMap<String, Object>> importFiles(@RequestBody SystemItemImportRequest request) {
        HashMap<String, Object> map = new HashMap<>();
        boolean isValidItems = request.getItems().stream().allMatch((x) -> {
                   SystemItem item = new SystemItem(x, request.getUpdateDate());
                   if(item.isValidImport(true)) {
                       if (x.getParentId() == null) return true;
                       Optional<SystemItem> optionalItem = systemItemRepository.findById(x.getParentId());
                       //элемент с id, совпадающим с parentid текущего элемента, нашёлся в базе и оказался файлом
                       boolean C = optionalItem.isPresent() && optionalItem.get().getType().getType().equals("FILE");
                       //элемент с id, совпадающим с parentid текущего элемента, нашёлся в запросе и оказался файлом
                       boolean D = request.getItems().stream().anyMatch((y) ->
                               (y.getId().equals(x.getParentId())) && y.getType().getType().equals("FILE"));
                       return !C && !D;
                   } else
                       return false;
        });

        if (request.validateIdsUnicity() && isValidItems) {
            request.getItems().forEach((x) -> systemItemRepository.save(new SystemItem(x, request.getUpdateDate())));
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            map.put("code", 400);
            map.put("message", "Validation Failed");
            return ResponseEntity.status(400).body(map);
        }
    }


}
