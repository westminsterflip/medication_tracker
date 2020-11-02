package com.example.medicationadherence.data.room.entities;

/* Medication entity in MedicationDatabase
   
   Implemented using Android Room
   
   CS1980 Fall 2019
   @authors Erin Herlihy, David Stropkey, Nicholas West, Ian Patterson
*/


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.SET_NULL;

@Entity(foreignKeys =
@ForeignKey(entity = Doctor.class, parentColumns = "doctorID", childColumns = "doctorID", onDelete = SET_NULL), indices = {
@Index("doctorID")})
public class Medication {
	@PrimaryKey(autoGenerate = true)
	private Long medicationID;
    private String medImageURL;
	private String name;
	private boolean status;
	private Long doctorID;     /* FK Doctor.name */
	private String dosage;
	private long startDate;            /* FORMAT: YYYY-MM-DD (for now) */
	private long endDate;              /* FORMAT: YYYY-MM-DD (for now) */
	private int containerVolume;
	private double cost;
	private long lateTime;

    public Long getMedicationID() {
        return medicationID;
    }

    public void setMedicationID(Long medicationID) {
        this.medicationID = medicationID;
    }

    public String getMedImageURL() {
        return medImageURL;
    }

    public void setMedImageURL(String medImageURL) {
        this.medImageURL = medImageURL;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Long getDoctorID() {
        return doctorID;
    }

    public void setDoctorID(Long doctorID) {
        this.doctorID = doctorID;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public int getContainerVolume() {
        return containerVolume;
    }

    public void setContainerVolume(int containerVolume) {
        this.containerVolume = containerVolume;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public Medication(String medImageURL, String name, boolean status, Long doctorID, String dosage, long startDate, long endDate, int containerVolume, double cost, long lateTime) {
        this.medImageURL = medImageURL;
        this.name = name;
        this.status = status;
        this.doctorID = doctorID;
        this.dosage = dosage;
        this.startDate = startDate;
        this.endDate = endDate;
        this.containerVolume = containerVolume;
        this.cost = cost;
        this.lateTime = lateTime;
    }

    @Ignore
    public Medication(Long medicationID, String medImageURL, String name, boolean status, Long doctorID, String dosage, long startDate, long endDate, int containerVolume, double cost, long lateTime) {
        this.medicationID = medicationID;
        this.medImageURL = medImageURL;
        this.name = name;
        this.status = status;
        this.doctorID = doctorID;
        this.dosage = dosage;
        this.startDate = startDate;
        this.endDate = endDate;
        this.containerVolume = containerVolume;
        this.cost = cost;
        this.lateTime = lateTime;
    }

    public long getLateTime() {
        return lateTime;
    }

    public void setLateTime(long lateTime) {
        this.lateTime = lateTime;
    }
}