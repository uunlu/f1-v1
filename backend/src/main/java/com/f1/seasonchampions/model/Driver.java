package com.f1.seasonchampions.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Driver {
    private String driverId;
    private String permanentNumber;
    private String code;
    private String givenName;
    private String familyName;
    private String dateOfBirth;
    private String nationality;
} 