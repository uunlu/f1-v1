package com.f1.seasonchampions.model;

public class Constructor {
    private String constructorId;
    private String name;
    private String nationality;

    public Constructor() {}

    public Constructor(String constructorId, String name, String nationality) {
        this.constructorId = constructorId;
        this.name = name;
        this.nationality = nationality;
    }

    public String getConstructorId() { return constructorId; }
    public void setConstructorId(String constructorId) { this.constructorId = constructorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
} 