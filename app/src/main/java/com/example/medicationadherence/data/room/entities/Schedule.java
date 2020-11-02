package com.example.medicationadherence.data.room.entities;

/* Schedule entity in MedicationDatabase
   
   Implemented using Android Room
   
   CS1980 Fall 2019
   @authors Erin Herlihy, David Stropkey, Nicholas West, Ian Patterson
*/

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.example.medicationadherence.data.Converters;

import static androidx.room.ForeignKey.CASCADE;

@Entity(primaryKeys = {"medicationID", "time", "weekdays"}, foreignKeys =
@ForeignKey(entity = Medication.class, parentColumns = "medicationID", childColumns = "medicationID", onDelete = CASCADE))
public class Schedule {
    @NonNull
	private Long medicationID;       /* FK Medication.medicationID */
	private double numDoses;
	private long time;
	@NonNull
	private boolean[] weekdays;     //boolean array for scheduled on day {SMTWTFS}

	public void setMedicationID(@NonNull Long medicationID) {
        this.medicationID = medicationID;
    }
	
	public @NonNull Long getMedicationID() {
        return this.medicationID;
    }

	public void setNumDoses(double numDoses) {
        this.numDoses = numDoses;
    }

	public double getNumDoses() {
        return this.numDoses;
    }
	
	public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return this.time;
    }

    @NonNull
    public boolean[] getWeekdays() {
        return weekdays;
    }

    public void setWeekdays(@NonNull boolean[] weekdays) {
        this.weekdays = weekdays;
    }

    public Schedule(@NonNull Long medicationID, double numDoses, long time, @NonNull boolean[] weekdays) {
        this.medicationID = medicationID;
        this.numDoses = numDoses;
        this.time = time;
        this.weekdays = weekdays;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean equals(@Nullable Object obj) {
	    if (!(obj instanceof Schedule))
	        return false;
        Schedule s = (Schedule) obj;
        if (s.medicationID == null && this.medicationID != null || this.medicationID == null && s.medicationID != null) return false;
	    return (s.medicationID == null && this.medicationID == null || s.medicationID.equals(this.medicationID)) && s.numDoses == this.numDoses && s.time == this.time && Converters.fromBoolArray(s.weekdays) == Converters.fromBoolArray(this.weekdays);
    }
}