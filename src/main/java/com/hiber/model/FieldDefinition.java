package com.hiber.model;

public class FieldDefinition {

    private String id;
    private String doc;
    private String type;
    private Double multiplier;
    private Double offset;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDoc() { return doc; }
    public void setDoc(String doc) { this.doc = doc; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Double getMultiplier() { return multiplier; }
    public void setMultiplier(Double multiplier) { this.multiplier = multiplier; }

    public Double getOffset() { return offset; }
    public void setOffset(Double offset) { this.offset = offset; }
}
