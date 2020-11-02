package com.example.medicationadherence.ui.summary;

import androidx.lifecycle.ViewModel;

import com.example.medicationadherence.data.room.dao.MedicationDAO;
import com.example.medicationadherence.model.DetailSummary;
import com.example.medicationadherence.ui.MainViewModel;

import java.util.ArrayList;
import java.util.List;

public class SummaryViewModel extends ViewModel {
    private List<DetailSummary> detailList;
    private double taken = 0;
    private double late = 0;
    private List<MedicationDAO.IDName> idList;
    private MainViewModel mainModel;
    private long earliest = 0;
    private double missed = 0;
    private long last = -1;

    List<DetailSummary> getDetailList() {
        return detailList;
    }

    void loadList(long startDate, long endDate){
        if (detailList == null)
            detailList = new ArrayList<>();
        else
            detailList.clear();
        if (idList == null)
            idList = mainModel.getMedIDs();
        if (idList != null && idList.size()!=0){
            double oMissed = 0;
            double oLate = 0;
            double oOnTime = 0;
            for (MedicationDAO.IDName idName :idList){
                long id = idName.medicationID;
                double missed = mainModel.getMissed(id, startDate, endDate);
                double late = mainModel.getLate(id, startDate, endDate);
                double onTime = mainModel.getOnTime(id, startDate, endDate);
                double total = missed + late + onTime;
                if (total != 0){
                    oMissed += missed;
                    oLate += late;
                    oOnTime += onTime;
                    detailList.add(new DetailSummary(idName.name, onTime/total, late/total));
                }
            }
            taken = oOnTime;
            late = oLate;
            missed = oMissed;
        }
    }

    long getEarliest() {
        if (earliest == 0)
            earliest = mainModel.getEarliestLog();
        return earliest;
    }

    public double getTaken() {
        return taken;
    }

    double getLate() {
        return late;
    }

    double getMissed() {
        return missed;
    }

    public void setMainModel(MainViewModel mainModel) {
        this.mainModel = mainModel;
    }

    long getLast() {
        return last;
    }

    void setLast(long last) {
        this.last = last;
    }
}
