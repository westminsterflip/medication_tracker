package com.example.medicationadherence.data.room.dao;/* Schedule entity DAO in MedicationDatabase
   
   Implemented using Android Room
   
   CS1980 Fall 2019
   @authors Erin Herlihy, David Stropkey, Nicholas West, Ian Patterson
*/

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.medicationadherence.data.room.entities.Schedule;

import java.util.List;

@Dao
public interface ScheduleDAO {
    @Insert
    void insert(Schedule... schedules);

    @Update
    void update(Schedule... schedules);

    @Delete
    void delete(Schedule... schedule);

    @Query("DELETE FROM SCHEDULE")
    void clearTable();

    @Query("SELECT Medication.medicationID as medicationID, Medication.medImageURL as medImageURL, Medication.name as medName,"+
           "Medication.dosage AS dosageAmt, Medication.startDate AS startDate,"+
           " Medication.endDate AS endDate, SCHEDULE.numDoses AS doses, SCHEDULE.time AS timeOfDay"+
           ", SCHEDULE.weekdays AS days, Instructions.instructions AS instructions, Medication.status as active FROM Medication INNER JOIN "+
           "SCHEDULE ON Medication.medicationID = SCHEDULE.medicationID LEFT JOIN Instructions ON instructions.medicationID = Medication.medicationID")
    List<ScheduleCard> loadScheduled();

    @Query("select * from schedule where medicationID = :id")
    List<Schedule> getScheduleForMed(Long id);

    class ScheduleCard{
        public Long medicationID;
        public String medImageURL;
        public String medName;
        public String dosageAmt;
        public long startDate;
        public long endDate;
        public double doses;
        public long timeOfDay;
        public boolean[] days;
        public String instructions;
        public boolean active;

        ScheduleCard(Long medicationID, String medImageURL, String medName, String dosageAmt, long startDate, long endDate, double doses, long timeOfDay, boolean[] days, String instructions, boolean active) {
            this.medImageURL = medImageURL;
            this.medName = medName;
            this.dosageAmt = dosageAmt;
            this.startDate = startDate;
            this.endDate = endDate;
            this.doses = doses;
            this.timeOfDay = timeOfDay;
            this.days = days;
            this.instructions = instructions;
            this.active = active;
            this.medicationID = medicationID;
        }
    }
}