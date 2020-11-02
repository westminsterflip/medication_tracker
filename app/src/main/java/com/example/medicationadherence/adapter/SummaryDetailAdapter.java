package com.example.medicationadherence.adapter;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationadherence.R;
import com.example.medicationadherence.model.DetailSummary;

import java.util.List;
import java.util.Locale;

public class SummaryDetailAdapter extends RecyclerView.Adapter {
    private List<DetailSummary> detailList;

    public SummaryDetailAdapter(List<DetailSummary> detailList) {
        this.detailList = detailList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View detailView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_summary_detail_card, parent, false);
        return new DetailViewHolder(detailView);
    }

    //assumes percTaken + percLate <= 100%
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DetailViewHolder detailHolder = (DetailViewHolder) holder;
        detailHolder.medName.setText(detailList.get(position).getMedName());
        double percLate = detailList.get(position).getPercLate()*100.0, percTaken = detailList.get(position).getPercTaken()*100.0;
        String temp = String.format(Locale.ENGLISH,"%.2f",percLate)+"% taken late";
        detailHolder.percLate.setText(temp);
        temp = String.format(Locale.ENGLISH, "%.2f",percTaken)+"% taken on time";
        detailHolder.percTaken.setText(temp);
        temp = String.format(Locale.ENGLISH, "%.2f",100-percLate-percTaken)+"% missed";
        detailHolder.percMissed.setText(temp);
        detailHolder.percLateBar.setProgress((int)Math.round(percLate+percTaken));
        detailHolder.percTakenBar.setProgress((int)Math.round(percTaken));
    }

    @Override
    public int getItemCount() {
        return detailList.size();
    }

    class DetailViewHolder extends RecyclerView.ViewHolder{
        final TextView medName;
        final TextView percTaken;
        final TextView percLate;
        final TextView percMissed;
        final ProgressBar percTakenBar;
        final ProgressBar percLateBar;

        DetailViewHolder(View view){
            super(view);
            medName=view.findViewById(R.id.detailCardMedicineName);
            percTaken=view.findViewById(R.id.detailCardTaken);
            percLate=view.findViewById(R.id.detailCardLate);
            percMissed=view.findViewById(R.id.detailCardMissed);
            percTakenBar=view.findViewById(R.id.detailCardTakenBar);
            percLateBar=view.findViewById(R.id.detailCardLateBar);
        }
    }
}
