package com.example.medicationadherence.data.room.dao;/* Medication entity DAO in MedicationDatabase
   
   Implemented using Android Room
   
   CS1980 Fall 2019
   @authors Erin Herlihy, David Stropkey, Nicholas West, Ian Patterson
*/

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.medicationadherence.data.room.entities.Medication;

import java.util.List;

@Dao
public interface MedicationDAO {
    @Insert
    long insert(Medication medications);

    @Update
    void update(Medication... medications);

    @Query("update medication set medImageURL = :medImageURL, name = :name, status = :status, doctorID = :doctorID, dosage = :dosage, startDate = :startDate, endDate = :endDate, containerVolume = :containerVolume, cost = :cost, lateTime = :lateTime where medicationID = :id")
    void update(Long id, String medImageURL, String name, boolean status, Long doctorID, String dosage, long startDate, long endDate, int containerVolume, double cost, long lateTime);
 
    @Delete
    void delete(Medication... medication);
	
	@Query("SELECT * FROM Medication")
    List<Medication> getAllMedications();

	@Query("DELETE FROM Medication")
    void clearTable();

	@Query("SELECT * FROM MEDICATION WHERE medicationID = :medicationID")
    Medication getMedWithID(Long medicationID);

	@Query("select distinct(medication.medicationID), medication.name from medication inner join medicationlog where medication.medicationid = medicationlog.medicationid")
	List<IDName> getMedIDs();

	class IDName{
	    public Long medicationID;
	    public String name;

        IDName(Long medicationID, String name) {
            this.medicationID = medicationID;
            this.name = name;
        }
    }
}