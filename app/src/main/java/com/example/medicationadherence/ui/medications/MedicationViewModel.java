package com.example.medicationadherence.ui.medications;

import androidx.lifecycle.ViewModel;

import com.example.medicationadherence.adapter.MedicationListAdapter;
import com.example.medicationadherence.data.room.entities.Medication;

import java.util.List;

public class MedicationViewModel extends ViewModel {
    private MedicationListAdapter medAdapter;
    private List<Medication> medList;

    MedicationListAdapter getMedAdapter() {
        return medAdapter;
    }

    void setMedAdapter(MedicationListAdapter medAdapter) {
        this.medAdapter = medAdapter;
    }

    public List<Medication> getMedList() {
        return medList;
    }

    void setMedList(List<Medication> medList) {
        this.medList = medList;
    }
}
