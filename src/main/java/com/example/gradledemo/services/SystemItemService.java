package com.example.gradledemo.services;

import com.example.gradledemo.model.SystemItem;
import com.example.gradledemo.model.SystemItemImportRequest;
import com.example.gradledemo.persistence.SystemItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SystemItemService {

    @Autowired
    private SystemItemRepository systemItemRepository;

    public ResponseEntity<HashMap<String, Object>> code400Response() {
        HashMap<String, Object> outputMap = new HashMap<>();
        outputMap.put("code", 400);
        outputMap.put("message", "Validation Failed");
        return ResponseEntity.status(400).body(outputMap);
    }

    public ResponseEntity<HashMap<String, Object>> code404Response() {
        HashMap<String, Object> outputMap = new HashMap<>();
        outputMap.put("code", 404);
        outputMap.put("message", "Item not found");
        return ResponseEntity.status(404).body(outputMap);
    }

    public ResponseEntity<HashMap<String, Object>> findFilesResponse(String id) {
        Optional<SystemItem> outputItem = systemItemRepository.findById(id);
        outputItem.ifPresent(this::recursiveChildren);
        if(outputItem.isPresent()) {
            if(!outputItem.get().isValidItem(false)) {
                return code400Response();
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(outputItem.get().toHashMap());
            }
        } else {
            return code404Response();
        }
    }

    // Этот метод применяется только к папкам. В нём происходит заполнение поля children
    // для данной папки и всех папок, являющихся её потомками. Также заполняется поле size.
    void recursiveChildren(SystemItem ancestor) {
        if(ancestor.getType().getType().equals("FOLDER")) {
            ArrayList<SystemItem> children = new ArrayList<>(systemItemRepository.findByParentId(ancestor.getId()));
            ancestor.setChildren(children);
            if (ancestor.getChildren().size() != 0) {
                ancestor.getChildren().forEach(this::recursiveChildren);
                ancestor.setSize(children.stream().map(SystemItem::getSize).reduce(Long::sum).get());
            } else {
                ancestor.setSize(0L);
            }
        }
    }

    // Этот метод формирует ответ на запрос на импорт с учётом проверки запроса на корректный формат.
    public ResponseEntity<HashMap<String, Object>> importFilesResponse(SystemItemImportRequest request) {
        boolean isValidItems = request.getItems().stream().allMatch((x) -> {
            SystemItem item = new SystemItem(x, request.getUpdateDate());
            if(item.isValidItem(true)) {
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

        // В условии проверяется, все ли элементы из запроса имеют корректный формат, не
        // конфликтуют с базой и между собой.
        if (request.validateIdsUnicity() && isValidItems) {
            request.getItems().forEach((x) -> systemItemRepository.save(new SystemItem(x, request.getUpdateDate())));
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return code400Response();
        }
    }

    public ResponseEntity<HashMap<String, Object>> deleteFilesResponse(String id) {
        Optional<SystemItem> item = systemItemRepository.findById(id);
        if(item.isPresent()) {
            if(item.get().isValidItem(false)) {
                systemItemDelete(item.get());
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return code400Response();
            }
        } else {
            return code404Response();
        }
    }

    // Этот метод удаляет элемент и, в случае папки, всех её потомков из базы.
    public void systemItemDelete(SystemItem item) {
        systemItemRepository.delete(item);
        if (item.getType().getType().equals("FOLDER"))
            new ArrayList<SystemItem>(systemItemRepository.findByParentId(item.getId())).forEach(this::systemItemDelete);
    }


}
