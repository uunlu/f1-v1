package com.f1.seasonchampions.model;

public class Driver {
    private String driverId;
    private String permanentNumber;
    private String code;
    private String givenName;
    private String familyName;
    private String dateOfBirth;
    private String nationality;

    public Driver() {}

    public Driver(String driverId, String permanentNumber, String code, String givenName, String familyName, String dateOfBirth, String nationality) {
        this.driverId = driverId;
        this.permanentNumber = permanentNumber;
        this.code = code;
        this.givenName = givenName;
        this.familyName = familyName;
        this.dateOfBirth = dateOfBirth;
        this.nationality = nationality;
    }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public String getPermanentNumber() { return permanentNumber; }
    public void setPermanentNumber(String permanentNumber) { this.permanentNumber = permanentNumber; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getGivenName() { return givenName; }
    public void setGivenName(String givenName) { this.givenName = givenName; }

    public String getFamilyName() { return familyName; }
    public void setFamilyName(String familyName) { this.familyName = familyName; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
} 