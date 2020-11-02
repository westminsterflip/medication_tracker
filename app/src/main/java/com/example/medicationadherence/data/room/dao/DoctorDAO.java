package com.example.medicationadherence.data.room.dao;

/* Doctor entity DAO in MedicationDatabase
   
   Implemented using Android Room
   
   CS1980 Fall 2019
   @authors Erin Herlihy, David Stropkey, Nicholas West, Ian Patterson
*/

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.medicationadherence.data.room.entities.Doctor;

import java.util.List;

@Dao
public interface DoctorDAO {
    @Insert
    long insert(Doctor doctors);

    @Update
    void update(Doctor... doctors);

    @Delete
    void delete(Doctor... doctor);

	@Query("SELECT * FROM Doctor")
    List<Doctor> getAllDoctors();

    @Query("DELETE FROM Doctor")
    void clearTable();

    @Query("UPDATE Doctor SET name = :name, practiceName = :practiceName, address = :address, phone = :phone WHERE doctorID = :id")
    void update(Long id, String name, String practiceName, String address, String phone);

    @Query("SELECT * FROM DOCTOR WHERE name = :name")
    List<Doctor> getWithName(String name);

    @Query("SELECT * FROM DOCTOR WHERE doctorID = :doctorID")
    Doctor getWithID(Long doctorID);
}