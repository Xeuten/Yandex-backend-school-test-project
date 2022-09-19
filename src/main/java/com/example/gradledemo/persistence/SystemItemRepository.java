package com.example.gradledemo.persistence;

import com.example.gradledemo.model.SystemItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;


public interface SystemItemRepository extends JpaRepository<SystemItem, String> {

    //Этот метод получает от базы данных ответ на запрос на получение всех детей элемента с переданным id
    @Query(value = "SELECT * FROM main WHERE parent_id = :id", nativeQuery = true)
    List<SystemItem> findByParentId (@Param("id") String id);

    //Этот метод получает от базы данных ответ на запрос на получение всех элементов таблицы,
    //даты которых попадают в интервал одних суток от переданной
    @Query(value = "SELECT * FROM main WHERE (date <= :datetime AND date >= :previousDay)", nativeQuery = true)
    List<SystemItem> findRecent(@Param("datetime") OffsetDateTime datetime, @Param("previousDay") OffsetDateTime previousDay);
}
