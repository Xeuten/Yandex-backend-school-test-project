package com.example.gradledemo.model;

public enum SystemItemType {
     FILE("FILE"), FOLDER("FOLDER");
     private String type;
     SystemItemType(String type) {
          this.type = type;
     }
     public String getType() {
          return type;
     }
}
