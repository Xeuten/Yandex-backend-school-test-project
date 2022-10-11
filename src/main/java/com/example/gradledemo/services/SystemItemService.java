package com.example.gradledemo.services;

import com.example.gradledemo.model.SystemItem;
import com.example.gradledemo.model.SystemItemImportRequest;
import com.example.gradledemo.persistence.SystemItemRepository;
import com.example.gradledemo.rabbitmq.RabbitMQSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
public class SystemItemService {

    @Autowired
    private RabbitMQSender rabbitMQSender;

    @Autowired
    private SystemItemRepository systemItemRepository;

    private ResponseEntity<HashMap<String, Object>> code400Response() {
        HashMap<String, Object> outputMap = new HashMap<>();
        outputMap.put("code", 400);
        outputMap.put("message", "Validation Failed");
        return ResponseEntity.status(400).body(outputMap);
    }

    private ResponseEntity<HashMap<String, Object>> code404Response() {
        HashMap<String, Object> outputMap = new HashMap<>();
        outputMap.put("code", 404);
        outputMap.put("message", "Item not found");
        return ResponseEntity.status(404).body(outputMap);
    }

    public ResponseEntity<HashMap<String, Object>> findFilesResponse(@Nullable String id, String userAgentValue) {
        String metainfo = userAgentValue + "/GET";
        if(id == null) {
            rabbitMQSender.sendMessage(code400Response(), metainfo);
            return code400Response();
        }
        Optional<SystemItem> outputItem = systemItemRepository.findById(id);
        outputItem.ifPresent(this::recursiveChildren);
        if(outputItem.isEmpty()) {
            rabbitMQSender.sendMessage(code404Response(), metainfo);
            return code404Response();
        }
        if(!outputItem.get().isValidItem(false)) {
            rabbitMQSender.sendMessage(code400Response(), metainfo);
            return code400Response();
        } else {
            rabbitMQSender.sendMessage(ResponseEntity.status(HttpStatus.OK).body(outputItem.get().toHashMap()), metainfo);
            return ResponseEntity.status(HttpStatus.OK).body(outputItem.get().toHashMap());
        }
    }

    // This method is applied to folders only. Inside it the children field is filled for the
    // current folder and all its descendants. The field size is also filled.
    private void recursiveChildren(SystemItem ancestor) {
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

    // This method creates a response to an import request with respect to validation of
    // the request's format.
    public ResponseEntity<HashMap<String, Object>> importFilesResponse(SystemItemImportRequest request) {
        boolean isValidItems = request.getItems().stream().allMatch((x) -> {
            SystemItem item = new SystemItem(x, request.getUpdateDate());
            if(item.isValidItem(true)) {
                if (x.getParentId() == null) return true;
                Optional<SystemItem> optionalItem = systemItemRepository.findById(x.getParentId());
                //an element with an id, matching with parentid of the current element, was found in db and turned out to be a file
                boolean C = optionalItem.isPresent() && optionalItem.get().getType().getType().equals("FILE");
                //an element with an id, matching with parentid of the current element, was found in the request and turned out to be a file
                boolean D = request.getItems().stream().anyMatch((y) ->
                        (y.getId().equals(x.getParentId())) && y.getType().getType().equals("FILE"));
                return !C && !D;
            } else
                return false;
        });

        // In the if-clause is checked, if all elements of the request have correct format and
        // play well with each other and with db
        if (request.validateIdsUnicity() && isValidItems) {
            request.getItems().forEach((x) -> systemItemRepository.save(new SystemItem(x, request.getUpdateDate())));
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return code400Response();
        }
    }

    public ResponseEntity<HashMap<String, Object>> deleteFilesResponse(@Nullable String id, String userAgentValue) {
        String metainfo = userAgentValue + "/DELETE";
        if(id == null) {
            rabbitMQSender.sendMessage(code400Response(), metainfo);
            return code400Response();
        }
        Optional<SystemItem> item = systemItemRepository.findById(id);
        if(item.isEmpty()) {
            rabbitMQSender.sendMessage(code400Response(), metainfo);
            return code404Response();
        }
        if(item.get().isValidItem(false)) {
            systemItemDelete(item.get());
            rabbitMQSender.sendMessage(new ResponseEntity<>(HttpStatus.OK), metainfo);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            rabbitMQSender.sendMessage(code400Response(), metainfo);
            return code400Response();
        }
    }

    //This method deletes an element and if it's a folder, all its descendants from the db
    private void systemItemDelete(SystemItem item) {
        systemItemRepository.delete(item);
        if (item.getType().getType().equals("FOLDER")) {
            new ArrayList<SystemItem>(systemItemRepository.findByParentId(item.getId())).forEach(this::systemItemDelete);
        }
    }

    public ResponseEntity<HashMap<String, Object>> findRecentFilesResponse(@Nullable String date, String userAgentValue) {
        String metainfo = userAgentValue + "/UPDATES";
        if(date == null || ISO8601parser(date).isEmpty()) {
            rabbitMQSender.sendMessage(code400Response(), metainfo);
            return code400Response();
        }
        HashMap<String, Object> outputMap = new HashMap<>();
        outputMap.put("items", new ArrayList<>(systemItemRepository
                .findRecent(ISO8601parser(date).get(), ISO8601parser(date).get().minusDays(1)).stream().filter((x)
                        -> x.getType().getType().equals("FILE")).map(SystemItem::toHashMap).toList()));
        rabbitMQSender.sendMessage(ResponseEntity.status(HttpStatus.OK).body(outputMap), metainfo);
        return ResponseEntity.status(HttpStatus.OK).body(outputMap);
    }

    private Optional<OffsetDateTime> ISO8601parser(String str) {
        ArrayList<String> dateComponents = new ArrayList<>(Arrays.asList(str.split("-")));
        if (dateComponents.size() != 3 || dateComponents.get(0).length() != 4
                || dateComponents.get(1).length() != 2) {
            return Optional.empty();
        }
        ArrayList<String> THalves = new ArrayList<>(Arrays.asList(dateComponents.get(2).split("T")));
        if (THalves.size() > 2 || THalves.get(0).length() != 2) {
            return Optional.empty();
        }
        try {
            int year = Integer.parseInt(dateComponents.get(0));
            int month = Integer.parseInt(dateComponents.get(1));
            int day = Integer.parseInt(THalves.get(0));
            int hours = 0;
            int minutes = 0;
            int seconds = 0;
            int nanoseconds = 0;
            boolean isLeapYear = year % 100 != 0 && year % 4 == 0;
            if (THalves.size() == 2) {
                ArrayList<String> timeComponents = new ArrayList<>(Arrays.asList(THalves.get(1).split(":")));
                if (timeComponents.size() != 3 || timeComponents.get(0).length() != 2
                        || timeComponents.get(1).length() != 2) {
                    return Optional.empty();
                }
                hours = Integer.parseInt(timeComponents.get(0));
                minutes = Integer.parseInt(timeComponents.get(1));
                ArrayList<String> secondComponents = new ArrayList<>(Arrays.asList(timeComponents
                        .get(2).split("\\.")));
                if (secondComponents.get(0).length() > 3 || secondComponents.size() > 2) {
                    return Optional.empty();
                }
                if (secondComponents.size() == 1) {
                    if (secondComponents.get(0).length() != 3
                            || !secondComponents.get(0).substring(2).equals("Z")) {
                        return Optional.empty();
                    }
                    seconds = Integer.parseInt(secondComponents.get(0).substring(0, 2));
                } else {
                    int fractionLen = secondComponents.get(1).length();
                    if (secondComponents.get(0).length() != 2
                            || (fractionLen != 4 && fractionLen != 7 && fractionLen !=10)
                            || !secondComponents.get(1).substring(fractionLen-1).equals("Z")) {
                        return Optional.empty();
                    }
                    seconds = Integer.parseInt(secondComponents.get(0));
                    nanoseconds = (int) (Integer.parseInt(secondComponents.get(1).substring(0, fractionLen-1))
                            * Math.pow(10, 10 - fractionLen));
                }
            }
            if (year > 9999 || year < 1 || month > 12 || month < 1 || day > 31 || day < 1
                    || ((month == 4 || month == 6 || month == 9 || month == 11) && day == 31)
                    || (month == 2 && ((isLeapYear && day > 29) || (!isLeapYear && day > 28)))
                    || hours > 23 || hours < 0 || minutes > 59 || minutes < 0 || seconds > 59
                    || seconds < 0)
                return Optional.empty();
            return Optional.of(OffsetDateTime.of(year, month, day, hours, minutes, seconds,
                    nanoseconds, ZoneOffset.UTC));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

}
