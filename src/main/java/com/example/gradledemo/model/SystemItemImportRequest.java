package com.example.gradledemo.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

public class SystemItemImportRequest {
    private ArrayList<SystemItemImport> items;
    private Date updateDate;

    public SystemItemImportRequest(ArrayList<SystemItemImport> items, Date updateDate) {
        this.items = items;
        this.updateDate = updateDate;
    }

    public String toString(){
        return items.stream().map((x) -> x.getId() + x.getUrl() + x.getParentId() + x.getType()
                )
                .collect(Collectors.joining(", "));
    }

    public ArrayList<SystemItemImport> getItems() {
        return items;
    }

    public void setItems(ArrayList<SystemItemImport> items) {
        this.items = items;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public boolean validateIdsUnicity() {
        ArrayList<String> ids = new ArrayList<>(this.getItems().stream().map(SystemItemImport::getId).toList());
        return new HashSet<String>(ids).size() == ids.size();
    }
}
