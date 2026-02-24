package com.hiber.model;

public class Meta {

    private String id;
    private String application;
    private String endian;

    public String getEndian() { return endian; }
    public void setEndian(String endian) { this.endian = endian; }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }
}
