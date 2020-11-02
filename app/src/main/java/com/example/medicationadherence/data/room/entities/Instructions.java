package com.example.medicationadherence.data.room.entities;

/* Instructions entity in MedicationDatabase
   
   Implemented using Android Room
   
   CS1980 Fall 2019
   @authors Erin Herlihy, David Stropkey, Nicholas West, Ian Patterson
*/

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(primaryKeys = {"medicationID", "instructions"}, foreignKeys =
@ForeignKey(entity = Medication.class, parentColumns = "medicationID", childColumns = "medicationID", onDelete = CASCADE))
public class Instructions {
	private long medicationID;           /* FK Medication.medicationID */
    @NonNull
	private String instructions;

    public Instructions(long medicationID, @NonNull String instructions) {
        this.medicationID = medicationID;
        this.instructions = instructions;
    }

    public void setMedicationID(long medicationID) {
        this.medicationID = medicationID;
    }

	public long getMedicationID() {
        return this.medicationID;
    }

	public void setInstructions(@NonNull String instructions) {
        this.instructions = instructions;
    }

	public @NonNull String getInstructions() {
        return this.instructions;
    }
	
}