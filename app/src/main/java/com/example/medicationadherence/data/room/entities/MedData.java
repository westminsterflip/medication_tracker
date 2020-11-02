package com.example.medicationadherence.data.room.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class MedData {
    @PrimaryKey
    private int id;
    private int altId;
    private String name;
    private String dosage;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAltId() {
        return altId;
    }

    public void setAltId(int altId) {
        this.altId = altId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public MedData(int id, int altId, String name, String dosage) {
        this.id = id;
        this.altId = altId;
        this.name = name;
        this.dosage = dosage;
    }

    @Ignore
    public MedData(int id, String name, String dosage) {
        this.id = id;
        this.altId = -1;
        this.name = name;
        this.dosage = dosage;
    }
}
