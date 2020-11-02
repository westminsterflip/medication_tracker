package com.example.medicationadherence.ui.summary;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationadherence.R;
import com.example.medicationadherence.adapter.SummaryDetailAdapter;
import com.example.medicationadherence.ui.DisableableScrollView;
import com.example.medicationadherence.ui.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class SummaryFragment extends Fragment {
    private TextView graphLabel;
    private final Calendar cal = Calendar.getInstance();
    private DisableableScrollView summaryScroll;
    private ImageButton next;
    private ImageButton prev;
    private SummaryViewModel model;
    private MainViewModel mainModel;
    private SummaryDetailAdapter detailAdapter;
    private ProgressBar lateBar;
    private ProgressBar takenBar;
    private TextView summaryMissed;
    private TextView summaryLate;
    private TextView summaryTaken;
    private ImageButton summaryExpander;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(this).get(SummaryViewModel.class);
        mainModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(MainViewModel.class);
        model.setMainModel(mainModel);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_summary, container, false);
        cal.setTimeInMillis(mainModel.getSummaryTimeToView());
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.clear(Calendar.AM_PM);
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        mainModel.setSummaryTimeToView(cal.getTimeInMillis());
        graphLabel = root.findViewById(R.id.summaryDWM);
        changeScale();
        lateBar = root.findViewById(R.id.summaryLateBar);
        takenBar = root.findViewById(R.id.summaryTakenBar);
        summaryMissed = root.findViewById(R.id.summaryMissed);
        summaryLate = root.findViewById(R.id.summaryLate);
        summaryTaken = root.findViewById(R.id.summaryTaken);

        next = root.findViewById(R.id.summaryNext);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTimeToView(1);
            }
        });
        if (cal.getTimeInMillis() + TimeUnit.DAYS.toMillis(1) > Calendar.getInstance().getTimeInMillis()){
            next.setEnabled(false);
        } else {
            next.setEnabled(true);
        }
        prev = root.findViewById(R.id.summaryPrev);
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTimeToView(-1);
            }
        });

        Spinner timeScaleSpinner = root.findViewById(R.id.summaryUnitSelect);
        timeScaleSpinner.setSelection(mainModel.getSummaryViewScale());
        timeScaleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(mainModel.getSummaryViewScale() == 3){
                    mainModel.setSummaryViewScale(0);
                    updateTimeToView(0);
                }
                mainModel.setSummaryViewScale(position);
                updateTimeToView(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //This probably shouldn't occur
            }
        });

        final RecyclerView detailView = root.findViewById(R.id.summaryDetail);

        summaryExpander = root.findViewById(R.id.summaryExpand);
        View.OnClickListener expand = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detailView.getVisibility() == View.VISIBLE) {
                    detailView.setVisibility(View.INVISIBLE);
                    summaryExpander.setRotation(0);
                    summaryScroll.setScrollEnabled(false);
                    mainModel.setSummaryExpand(false);
                } else {
                    detailView.setVisibility(View.VISIBLE);
                    summaryExpander.setRotation(180);
                    summaryScroll.setScrollEnabled(true);
                    mainModel.setSummaryExpand(true);
                }
            }
        };
        summaryExpander.setOnClickListener(expand);
        View summaryCL = root.findViewById(R.id.summaryPieConstraintLayout);
        summaryCL.setOnClickListener(expand);

        summaryScroll = root.findViewById(R.id.summaryScroll);

        detailView.setLayoutManager(new LinearLayoutManager(getContext()));

        if(mainModel.isSummaryExpand()){
            detailView.setVisibility(View.VISIBLE);
            summaryExpander.setRotation(180);
            summaryScroll.setScrollEnabled(true);
        }
        model.loadList(0,0);
        detailAdapter = new SummaryDetailAdapter(model.getDetailList());

        detailView.setAdapter(detailAdapter);

        Button todayButton = root.findViewById(R.id.summaryTodayButton);
        todayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mainModel.getSummaryViewScale() != 3) {
                    Calendar todayCal = Calendar.getInstance();
                    todayCal.set(Calendar.HOUR_OF_DAY, 0);
                    todayCal.clear(Calendar.AM_PM);
                    todayCal.clear(Calendar.MINUTE);
                    todayCal.clear(Calendar.SECOND);
                    todayCal.clear(Calendar.MILLISECOND);
                    mainModel.setSummaryTimeToView(todayCal.getTimeInMillis());
                    updateTimeToView(0);
                }
            }
        });

        return root;
    }

    //negative = back, 0 = scale changed, positive = forward
    private void updateTimeToView(int dir){
        cal.setTimeInMillis(mainModel.getSummaryTimeToView());
        if (dir > 0) {
            cal.add(Calendar.DAY_OF_YEAR, ((mainModel.getSummaryViewScale() == 0) ? 1 : 0));
            cal.add(Calendar.DAY_OF_YEAR, ((mainModel.getSummaryViewScale() == 1) ? 7 : 0));
            cal.add(Calendar.MONTH, ((mainModel.getSummaryViewScale() == 2) ? 1 : 0));
            if (cal.getTimeInMillis() > Calendar.getInstance().getTimeInMillis()) {
                cal.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            }
            model.setLast(-1);
        } else if (dir < 0) {
            cal.add(Calendar.DAY_OF_YEAR, ((mainModel.getSummaryViewScale() == 0) ? -1 : 0));
            cal.add(Calendar.DAY_OF_YEAR, ((mainModel.getSummaryViewScale() == 1) ? -7 : 0));
            cal.add(Calendar.MONTH, ((mainModel.getSummaryViewScale() == 2) ? -1 : 0));
            model.setLast(-1);
        } else {
            if (mainModel.getSummaryViewScale() != 0 && model.getLast() == -1)
                model.setLast(cal.getTimeInMillis());
            else if (mainModel.getSummaryViewScale() == 0 && model.getLast() != -1){
                cal.setTimeInMillis(model.getLast());
                model.setLast(-1);
            }
        }
        if (cal.getTimeInMillis() < mainModel.getEarliestLog()) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(mainModel.getEarliestLog());
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_YEAR);
            c.clear();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_YEAR, day);
            cal.setTimeInMillis(c.getTimeInMillis());
        }
        Calendar temp = Calendar.getInstance();
        temp.setTimeInMillis(cal.getTimeInMillis());
        temp.add(Calendar.MONTH, 1);
        temp.set(Calendar.DAY_OF_MONTH, 1);
        changeScale();
        if (cal.getTimeInMillis() + TimeUnit.DAYS.toMillis(1) > Calendar.getInstance().getTimeInMillis()){
            next.setEnabled(false);
            next.setVisibility(View.INVISIBLE);
        } else if (cal.getTimeInMillis() + TimeUnit.DAYS.toMillis(7) > Calendar.getInstance().getTimeInMillis() && mainModel.getSummaryViewScale() > 0){
            next.setEnabled(false);
            next.setVisibility(View.INVISIBLE);
        } else if (temp.getTimeInMillis() > Calendar.getInstance().getTimeInMillis() && mainModel.getSummaryViewScale() > 1){
            next.setEnabled(false);
            next.setVisibility(View.INVISIBLE);
        } else {
            next.setEnabled(true);
            next.setVisibility(View.VISIBLE);
        }
        updateMainGraph();
    }

    private void changeScale(){
        if (mainModel.getSummaryViewScale() == 1)
            cal.set(Calendar.DAY_OF_WEEK,1);
        int year = cal.get(Calendar.YEAR);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        Locale syslocale = ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0);
        String dayName = new SimpleDateFormat("EEE", syslocale).format(cal.getTime());
        String monthName = new SimpleDateFormat("MMM", syslocale).format(cal.getTime());
        String label = "unchanged";
        switch (mainModel.getSummaryViewScale()){
            case 0:
                label = dayName+", "+monthName+" "+day;
                if (year != Calendar.getInstance().get(Calendar.YEAR))
                    label += ", " + year;
                break;
            case 1:
                label = dayName+", "+monthName+" "+day+" - ";
                Calendar calt = Calendar.getInstance();
                calt.setTimeInMillis(cal.getTimeInMillis());
                calt.add(Calendar.DAY_OF_YEAR, 6);
                int dayt = calt.get(Calendar.DAY_OF_MONTH);
                String dayNamet = new SimpleDateFormat("EEE", syslocale).format(calt.getTime());
                String monthNamet = new SimpleDateFormat("MMM", syslocale).format(calt.getTime());
                label += dayNamet+", "+monthNamet+" "+dayt;
                break;
            case 2:
                label = monthName + " " + year;
                cal.set(Calendar.DAY_OF_MONTH,1);
                break;
            case 3:
                label = "Overall";
                cal.setTimeInMillis(mainModel.getEarliestLog());
        }
        graphLabel.setText(label);
        mainModel.setSummaryTimeToView(cal.getTimeInMillis());
    }

    private void updateMainGraph(){
        long timeToView = mainModel.getSummaryTimeToView();
        Calendar temp = Calendar.getInstance();
        if (mainModel.getSummaryViewScale() != 3) {
            temp.setTimeInMillis(timeToView);
            temp.add(Calendar.DAY_OF_YEAR, ((mainModel.getSummaryViewScale() == 0) ? 1 : 0));
            temp.add(Calendar.DAY_OF_YEAR, ((mainModel.getSummaryViewScale() == 1) ? 7 : 0));
            temp.add(Calendar.MONTH, ((mainModel.getSummaryViewScale() == 2) ? 1 : 0));
        } else {
            int day = temp.get(Calendar.DAY_OF_YEAR) + 1;
            int year = temp.get(Calendar.YEAR);
            temp.clear();
            temp.set(Calendar.YEAR, year);
            temp.set(Calendar.DAY_OF_YEAR, day);
            temp.add(Calendar.MILLISECOND, -1);
        }
        model.loadList(timeToView, temp.getTimeInMillis());
        detailAdapter.notifyDataSetChanged();
        if (model.getDetailList().size() != 0)
            summaryExpander.setVisibility(View.VISIBLE);
        else
            summaryExpander.setVisibility(View.INVISIBLE);
        double late = model.getLate(), taken = model.getTaken(), missed = model.getMissed(), total = late + taken + missed;
        if (total != 0){
            lateBar.setProgress((int)Math.round(late/total*100.0)+(int)Math.round(taken/total*100.0), true);
            takenBar.setProgress((int)Math.round(taken/total*100.0), true);
            summaryTaken.setText(("On time: " + (int)taken + "/" + (int)total));
            summaryLate.setText(("Late: " + (int)late + "/" + (int)total));
            summaryMissed.setText(("Missed: " + (int)missed + "/" + (int)total));
        } else {
            takenBar.setProgress(100, true);
            summaryTaken.setText("");
            summaryLate.setText(R.string.summary_no_data);
            summaryMissed.setText("");
        }
        if (model.getEarliest() >= mainModel.getSummaryTimeToView() && model.getEarliest() < temp.getTimeInMillis() || model.getEarliest() == 0){
            prev.setEnabled(false);
            prev.setVisibility(View.INVISIBLE);
        }else{
            prev.setEnabled(true);
            prev.setVisibility(View.VISIBLE);
        }
        if(mainModel.getSummaryViewScale() == 3){
            next.setEnabled(false);
            next.setVisibility(View.INVISIBLE);
            prev.setEnabled(false);
            prev.setVisibility(View.INVISIBLE);
        }
    }
}