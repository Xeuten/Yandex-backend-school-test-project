package com.example.gradledemo.persistence;

import com.example.gradledemo.model.SystemItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;


public interface SystemItemRepository extends JpaRepository<SystemItem, String> {

    // This method receives a response from the db to a query to get all children of the
    // element with targeted id
    @Query(value = "SELECT * FROM main WHERE parent_id = :id", nativeQuery = true)
    List<SystemItem> findByParentId (@Param("id") String id);

    //This method receives a response from the db to a query to get all elements by targeted
    // date with one day margin
    @Query(value = "SELECT * FROM main WHERE (date <= :datetime AND date >= :previousDay)", nativeQuery = true)
    List<SystemItem> findRecent(@Param("datetime") OffsetDateTime datetime, @Param("previousDay") OffsetDateTime previousDay);
}
