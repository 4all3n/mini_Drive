package com.minicloud.client.models;

/**
 * Client-side model representing a segment in the breadcrumb path navigation.
 */
public class BreadcrumbRecord {
    private Long id; // null represents "My Drive" root
    private String name;

    public BreadcrumbRecord() {}

    public BreadcrumbRecord(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @Override
    public String toString() {
        return name;
    }
}
