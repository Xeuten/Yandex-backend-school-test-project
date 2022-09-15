package com.example.gradledemo.persistence;

import com.example.gradledemo.model.SystemItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;


public interface SystemItemRepository extends JpaRepository<SystemItem, String> {

    //Этот метод получает от базы данных ответ на запрос на получение всех детей элемента с переданным id
    @Query(value = "SELECT * FROM main WHERE parent_id = :id", nativeQuery = true)
    List<SystemItem> findByParentId (@Param("id") String id);

//    @Query(value = "SELECT * FROM main WHERE date >= (:date - INTERVAL '24 HOURS')::TIMESTAMP", nativeQuery = true)
//    List<SystemItem> findRecent(@Param("date") Date date);
}
