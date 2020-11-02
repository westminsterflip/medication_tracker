package com.example.medicationadherence.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationadherence.R;
import com.example.medicationadherence.data.room.dao.ScheduleDAO;
import com.example.medicationadherence.ui.MainViewModel;
import com.example.medicationadherence.ui.home.DailyMedListViewModel;

import java.util.List;

public class DailyViewPagerAdapter extends RecyclerView.Adapter {
    private List<Long> dateList;
    private List<List<ScheduleDAO.ScheduleCard>> medLists;
    private Activity activity;
    private MainViewModel mainModel;
    private DailyMedListViewModel dailyModel;

    public DailyViewPagerAdapter(List<Long> dateList, List<List<ScheduleDAO.ScheduleCard>> medLists, Activity activity, MainViewModel mainModel, DailyMedListViewModel dailyModel){
        this.dateList = dateList;
        this.medLists = medLists;
        this.activity = activity;
        this.mainModel = mainModel;
        this.dailyModel = dailyModel;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View dailyView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_daily_recycler, parent, false);
        return new DailyViewPagerHolder(dailyView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DailyViewPagerHolder holderd = (DailyViewPagerHolder) holder;
        holderd.recyclerView.setLayoutManager(new LinearLayoutManager(holderd.recyclerView.getContext()));
        holderd.recyclerView.setAdapter(new DailyMedicationListAdapter(medLists.get(position), activity, mainModel, mainModel.getDailyLogs(dateList.get(position)), dateList.get(position), dailyModel));
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    private class DailyViewPagerHolder extends RecyclerView.ViewHolder{
        final RecyclerView recyclerView;

        DailyViewPagerHolder(View view){
            super (view);
            recyclerView = view.findViewById(R.id.dailyRecyclerView);
        }
    }
}
