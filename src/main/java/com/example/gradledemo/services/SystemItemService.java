package com.example.gradledemo.services;

import com.example.gradledemo.model.SystemItem;
import com.example.gradledemo.model.SystemItemImportRequest;
import com.example.gradledemo.persistence.SystemItemRepository;
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

    public ResponseEntity<HashMap<String, Object>> findFilesResponse(@Nullable String id) {
        if(id == null) {
            return code400Response();
        }
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

    public ResponseEntity<HashMap<String, Object>> deleteFilesResponse(@Nullable String id) {
        if(id == null) {
            return code400Response();
        }
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

    public ResponseEntity<HashMap<String, Object>> findRecentFilesResponse(@Nullable String date) {
        if(date == null || ISO8601parser(date).isEmpty()) {
            return code400Response();
        }
        HashMap<String, Object> outputMap = new HashMap<>();
        outputMap.put("items", new ArrayList<>(systemItemRepository
                .findRecent(ISO8601parser(date).get(), ISO8601parser(date).get().minusDays(1)).stream().filter((x)
                        -> x.getType().getType().equals("FILE")).map(SystemItem::toHashMap).toList()));
        return ResponseEntity.status(HttpStatus.OK).body(outputMap);
    }

    public static Optional<OffsetDateTime> ISO8601parser(String str) {
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
