package com.example.gradledemo.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Entity
@Table(name="main")
public class SystemItem {

    public SystemItem() {}

    public SystemItem(SystemItemImport importItem, Date date) {
        this.id = importItem.getId();
        this.url = importItem.getUrl();
        this.parentId = importItem.getParentId();
        this.size = importItem.getSize();
        this.type = importItem.getType();
        this.date = date;
    }

    @Id
    @Column
    private String id;

    @Column
    private String url;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column
    private String parentId;

    @Column
    private Long size;

    @Column
    @Enumerated(EnumType.STRING)
    private SystemItemType type;


    // Children of folders are not stored in the db. They are filled only in the course of
    // creating a response to "get" request
    @Transient
    private ArrayList<SystemItem> children;

    public SystemItemType getType() {
        return type;
    }

    public void setType(String type) {
        this.type = SystemItemType.valueOf(type);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public ArrayList<SystemItem> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<SystemItem> children) {
        this.children = children;
    }

    // In the course of converting a folder into a hashmap its children are filled
    public HashMap<String, Object> toHashMap() {
        HashMap<String, Object> outputMap = new HashMap<>();
        outputMap.put("id", this.getId());
        outputMap.put("url", this.getUrl());
        outputMap.put("type", this.getType().getType());
        outputMap.put("parentId", this.getParentId());
        outputMap.put("date", getDate());
        outputMap.put("size", getSize());
        if(this.getType().getType().equals("FILE")) {
            outputMap.put("children", null);
        } else if (this.getChildren() == null) {
            outputMap.put("children", new SystemItem[0]);
        } else {
            ArrayList<HashMap<String, Object>> children = new ArrayList<>();
            this.getChildren().forEach((x) -> children.add(x.toHashMap()));
            outputMap.put("children", children);
        }
        return outputMap;
    }


    // This method validates an element's format correct. Since the validating conditions differ for
    // import and find/delete requests, the method has an argument which verifies the type of request
    public boolean isValidItem(boolean isImport) {
        return !(this.getType().getType().equals("FILE") && (this.getSize() <= 0 || this.getUrl().length() > 255))
                && !(this.getType().getType().equals("FOLDER") && ((this.getSize() != null && isImport)
                    || this.getUrl() != null))
                && this.getId() != null && this.getDate() != null && this.getType().getType() != null;
    }

}
