package com.example.gradledemo.persistence;

import com.example.gradledemo.model.SystemItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Database {

    @Autowired
    private SystemItemRepository systemItemRepository;

    public HashMap<String, Object> systemItemFilling(String id) {
        Optional<SystemItem> outputItem = systemItemRepository.findById(id);
        outputItem.ifPresent(this::recursiveChildren);
        HashMap<String, Object> outputMap = new HashMap<>();
        if(outputItem.isPresent()) {
            if(!outputItem.get().isValidImport(false))
              {
                outputMap.put("code", 400);
                outputMap.put("message", "Validation Failed");
            } else {
                outputMap = outputItem.get().toHashMap();
            }
        } else {
            outputMap.put("code", 404);
            outputMap.put("message", "Item not found");
        }
        return outputMap;
    }

    public void systemItemDelete(SystemItem item) {
        if (item.getType().getType() == "FILE") {
            systemItemRepository.delete(item);
        } else {
            new ArrayList<SystemItem>(systemItemRepository.findByParentId(item.getId())).forEach(this::systemItemDelete);
        }
    }


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

}
