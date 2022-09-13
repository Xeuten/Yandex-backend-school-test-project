package com.example.gradledemo.persistence;

import com.example.gradledemo.model.SystemItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface SystemItemRepository extends JpaRepository<SystemItem, String> {

    @Query(value = "SELECT * FROM main WHERE parent_id = :id", nativeQuery = true)
    List<SystemItem> findByParentId (@Param("id") String id);
}
