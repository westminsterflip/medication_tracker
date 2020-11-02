package com.example.medicationadherence.ui;


import android.app.Application;

import androidx.lifecycle.AndroidViewModel;

import com.example.medicationadherence.data.Repository;
import com.example.medicationadherence.data.room.dao.MedicationDAO;
import com.example.medicationadherence.data.room.entities.Doctor;
import com.example.medicationadherence.data.room.entities.Instructions;
import com.example.medicationadherence.data.room.entities.Medication;
import com.example.medicationadherence.data.room.entities.MedicationLog;
import com.example.medicationadherence.data.room.entities.Schedule;

import java.util.Calendar;
import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private long summaryTimeToView = -1;
    private boolean summaryExpand = false;
    private int summaryViewScale = 0;
    private int medSortMode = 0; //0 = a-z, 1 = z-a
    private Repository repository;


    public MainViewModel (Application application){
        super(application);
        repository = new Repository(application);
    }

    public List<Medication> getMedList(){return repository.getMedList();}

    public long getSummaryTimeToView() {
        if (summaryTimeToView == -1)
            summaryTimeToView = Calendar.getInstance().getTimeInMillis();
        return summaryTimeToView;
    }

    public void setSummaryTimeToView(long summaryTimeToView) {
        this.summaryTimeToView = summaryTimeToView;
    }

    public boolean isSummaryExpand() {
        return summaryExpand;
    }

    public void setSummaryExpand(boolean summaryExpand) {
        this.summaryExpand = summaryExpand;
    }

    public int getSummaryViewScale() {
        return summaryViewScale;
    }

    public void setSummaryViewScale(int summaryViewScale) {
        this.summaryViewScale = summaryViewScale;
    }

    public int getMedSortMode() {
        return medSortMode;
    }

    public void setMedSortMode(int medSortMode) {
        this.medSortMode = medSortMode;
    }

    public Repository getRepository() {
        return repository;
    }

    public Long insert(Doctor doctor){
        return repository.insert(doctor);
    }

    public void insert(Instructions instructions){
        repository.insert(instructions);
    }

    public long insert(Medication medication){
        return repository.insert(medication);
    }

    public void insert(MedicationLog medicationLog){
        repository.insert(medicationLog);
    }

    public void insert(Schedule schedule){
        repository.insert(schedule);
    }

    public void deleteAll(){
        repository.deleteAll();
    }

    public void updateDoctor(Long id, String doctorName, String practice, String address, String phone){
        if(phone != null && phone.equals("")) phone = null;
        if(address != null && address.equals("")) address = null;
        if(practice != null && practice.equals("")) practice =null;
        repository.updateDoctor(id, doctorName, practice, address, phone);
    }

    public List<Doctor> getDocWithName(String name){
        return repository.getDocWithName(name);
    }

    public Doctor getDoctorWithID(Long doctorID){return repository.getDocWithID(doctorID);}

    public Medication getMedWithID(Long medicationID){return repository.getMedWithID(medicationID);}

    public void updateMedication(Long id, String medImageURL, String name, boolean status, Long doctorID, String dosage, long startDate, long endDate, int containerVolume, double cost, long lateTime){
        if (medImageURL == null || medImageURL.equals(""))
            repository.updateMedication(id, null, name, status, doctorID, dosage, startDate, endDate, containerVolume, cost, lateTime);
        else
            repository.updateMedication(id, medImageURL, name, status, doctorID, dosage, startDate, endDate, containerVolume, cost, lateTime);
    }

    public List<Schedule> getScheduleFM(Long id){
        return repository.getScheduleForMed(id);
    }

    public void remove(Schedule schedule){
        repository.remove(schedule);
    }

    public void remove(Medication medication){
        repository.remove(medication);
    }

    public void remove(Doctor doctor){
        repository.remove(doctor);
    }

    public int getMissed(Long medicationID, long startDate, long endDate){
        return repository.getMissed(medicationID, startDate, endDate);
    }

    public int getLate(Long medicationID, long startDate, long endDate){
        return repository.getLate(medicationID, startDate, endDate);
    }

    public int getOnTime(Long medicationID, long startDate, long endDate){
        return repository.getOnTime(medicationID, startDate, endDate);
    }

    public List<MedicationDAO.IDName> getMedIDs(){
        return repository.getMedIDs();
    }

    public long getEarliestLog(){
        long out = repository.getEarliestLog();
        if (out == 0) {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_YEAR);
            c.clear();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_YEAR, day);
            return c.getTimeInMillis();
        }else
            return out;
    }

    public List<MedicationLog> getDailyLogs(long date){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_YEAR);
        c.clear();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_YEAR, day+1);
        return repository.getDailyLogs(date, c.getTimeInMillis());
    }

    public void updateMedLog(Long medicationID, long date, long oldTimeLate, long newTimeLate, boolean taken){
        repository.updateLog(medicationID, date, oldTimeLate, newTimeLate, taken);
    }
}
