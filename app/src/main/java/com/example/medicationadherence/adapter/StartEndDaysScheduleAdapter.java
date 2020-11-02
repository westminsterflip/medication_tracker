package com.example.medicationadherence.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationadherence.R;
import com.example.medicationadherence.data.Converters;
import com.example.medicationadherence.ui.medications.wizard.EditScheduleFragmentDirections;
import com.example.medicationadherence.ui.medications.wizard.RootWizardViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class StartEndDaysScheduleAdapter extends RecyclerView.Adapter {
    private ArrayList<Integer> days;
    private RootWizardViewModel model;
    private ArrayList<Integer> justDeleted = new ArrayList<>();
    private ArrayList<Integer> justDelPos = new ArrayList<>();
    private Activity activity;

    public StartEndDaysScheduleAdapter(ArrayList<Integer> days, RootWizardViewModel model, Activity activity) {
        this.days = days;
        this.model = model;
        this.activity = activity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View sedsView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_schedule_days_start_end_card, parent, false);
        return new SEDSHolder(sedsView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SEDSHolder sedsHolder = (SEDSHolder) holder;
        final boolean[] day= Converters.intToBoolArray(days.get(position));
        sedsHolder.sun.setChecked(day[0]);
        sedsHolder.mon.setChecked(day[1]);
        sedsHolder.tues.setChecked(day[2]);
        sedsHolder.wed.setChecked(day[3]);
        sedsHolder.thurs.setChecked(day[4]);
        sedsHolder.fri.setChecked(day[5]);
        sedsHolder.sat.setChecked(day[6]);
        sedsHolder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditScheduleFragmentDirections.ActionEditScheduleFragment2ToEditScheduleCardFragment2 action = EditScheduleFragmentDirections.actionEditScheduleFragment2ToEditScheduleCardFragment2(Converters.fromBoolArray(day));
                model.getNavController().navigate(action);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public void delete(int pos){
        justDeleted.add(days.get(pos));
        justDelPos.add(pos);
        days.remove(pos);
        notifyDataSetChanged();
        showUndo();
    }

    private void showUndo(){
        View view = activity.findViewById(R.id.drawer_layout);
        String s = "Schedule{s} deleted";
        Snackbar undoBar = Snackbar.make(view, s, Snackbar.LENGTH_LONG);
        undoBar.setAction("UNDO", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                days.add(justDelPos.get(justDelPos.size()-1), justDeleted.get(justDeleted.size()-1));
                notifyDataSetChanged();
                justDeleted.clear();
                justDelPos.clear();
            }
        });
        undoBar.show();
        undoBar.addCallback(new Snackbar.Callback(){
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                int i;
                for (i = 0; i < justDeleted.size(); i++)
                    if (event != DISMISS_EVENT_ACTION && event != DISMISS_EVENT_CONSECUTIVE || i < justDeleted.size() - 1)
                        model.removeSchedules(justDeleted.get(i));
                if(event == DISMISS_EVENT_CONSECUTIVE){
                    int pos = justDelPos.get(justDelPos.size()-1);
                    int med = justDeleted.get(justDeleted.size()-1);
                    justDelPos.clear();
                    justDeleted.clear();
                    justDelPos.add(pos);
                    justDeleted.add(med);
                }else if (event != DISMISS_EVENT_ACTION) {
                    justDeleted.clear();
                    justDelPos.clear();
                }
            }
        });
    }

    class SEDSHolder extends RecyclerView.ViewHolder{
        final CheckBox sun;
        final CheckBox mon;
        final CheckBox tues;
        final CheckBox wed;
        final CheckBox thurs;
        final CheckBox fri;
        final CheckBox sat;
        final CardView card;

        SEDSHolder(@NonNull View view) {
            super(view);
            sun = view.findViewById(R.id.scheduleCardSunday);
            mon = view.findViewById(R.id.scheduleCardMonday);
            tues = view.findViewById(R.id.scheduleCardTuesday);
            wed = view.findViewById(R.id.scheduleCardWednesday);
            thurs = view.findViewById(R.id.scheduleCardThursday);
            fri = view.findViewById(R.id.scheduleCardFriday);
            sat = view.findViewById(R.id.scheduleCardSaturday);
            card = view.findViewById(R.id.scheduleCardView);
        }
    }
}
