package com.example.medicationadherence.data.room.entities;

/* Doctor entity in MedicationDatabase
   
   Implemented using Android Room
   
   CS1980 Fall 2019
   @authors Erin Herlihy, David Stropkey, Nicholas West, Ian Patterson
*/

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class Doctor {
	@PrimaryKey(autoGenerate = true)
	private Long doctorID;
	@NonNull
	private String name;
	private String practiceName;
	private String address;
	private String phone;
	
	public void setName(@NonNull String name) {
        this.name = name;
    }
	
	@NonNull
    public String getName() {
        return this.name;
    }
	
	public void setPracticeName(String practiceName) {
        this.practiceName = practiceName;
    }
	
	public String getPracticeName() {
        return this.practiceName;
    }
	
	public void setAddress(String address) {
        this.address = address;
    }
	
	public String getAddress() {
        return this.address;
    }
	
	public void setPhone(String phone) {
        this.phone = phone;
    }
	
	public String getPhone() {
        return this.phone;
    }
	
	public void setDoctorID(Long doctorID) {
        this.doctorID = doctorID;
    }
	
	public Long getDoctorID() {
        return this.doctorID;
    }

    public Doctor(@NonNull String name, String practiceName, String address, String phone) {
        this.name = name;
        this.practiceName = practiceName;
        this.address = address;
        this.phone = phone;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
	    if (!(obj instanceof Doctor))
	        return false;
        Doctor doc = (Doctor)obj;
        boolean out = Objects.requireNonNull(doc).getName().equals(name);
        out &= (doc.getPracticeName()==null?practiceName==null:doc.getPracticeName().equals(practiceName));
        out &= (doc.getAddress()==null?address==null:doc.getAddress().equals(address));
        out &= (doc.getPhone()==null?phone==null:doc.getPhone().equals(phone));
	    return out;
    }
}