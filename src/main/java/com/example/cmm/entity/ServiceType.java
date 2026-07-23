package com.example.cmm.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "service_types")
public class ServiceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 10)
    private String defaultTime;

    public ServiceType() {}

    public ServiceType(String name, String defaultTime) {
        this.name = name;
        this.defaultTime = defaultTime;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDefaultTime() { return defaultTime; }
    public void setDefaultTime(String defaultTime) { this.defaultTime = defaultTime; }
}
