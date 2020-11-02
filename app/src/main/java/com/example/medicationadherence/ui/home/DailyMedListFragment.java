package com.example.medicationadherence.ui.home;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.medicationadherence.R;
import com.example.medicationadherence.adapter.DailyViewPagerAdapter;
import com.example.medicationadherence.data.room.dao.ScheduleDAO;
import com.example.medicationadherence.ui.MainViewModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

//TODO: need service to deactivate medications/mark as missed if day over
public class DailyMedListFragment extends Fragment {
    private DailyMedListViewModel model;
    private TextView date;
    private ViewPager2 dailyViewPager;
    private boolean fromCal = true;
    private MainViewModel mainModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(this).get(DailyMedListViewModel.class);
        mainModel = new ViewModelProvider(Objects.requireNonNull(getActivity())).get(MainViewModel.class);
        model.setMainModel(mainModel);
        model.getDateList();
        long timeToView = DailyMedListFragmentArgs.fromBundle(Objects.requireNonNull(getArguments())).getTimeToView();
        if (timeToView == 0){//TODO: removed items showed up on frontpage
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            c.clear();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);
            timeToView = c.getTimeInMillis();
            fromCal = false;
        }
        if(model.getDate() == -1){
            model.setDate(timeToView);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(model.getDate());
            cal.add(Calendar.DAY_OF_YEAR, 1);
            model.setNextDate(cal.getTimeInMillis());
            cal.add(Calendar.DAY_OF_YEAR, -2);
            model.setPrevDate(cal.getTimeInMillis());
        }
        model.setCardList(mainModel.getRepository().getScheduleCard());
        final Observer<List<List<ScheduleDAO.ScheduleCard>>> medicationObserver = new Observer<List<List<ScheduleDAO.ScheduleCard>>>() {
            @Override
            public void onChanged(List<List<ScheduleDAO.ScheduleCard>> dailyMedications) {
                if (model.getMedAdapter() != null) {
                    model.getMedAdapter().notifyDataSetChanged();
                }
            }
        };
        model.getMedications().observe(this, medicationObserver);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_daily_med_list, container, false);
        ImageButton next = root.findViewById(R.id.dailyNextButton);
        final ImageButton prev = root.findViewById(R.id.dailyPrevButton);
        dailyViewPager = root.findViewById(R.id.dailyViewPager);
        date = root.findViewById(R.id.dailyDateView);
        updateText();
        if (fromCal) {
            final View.OnClickListener changeDay = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeDate((v.getId() == R.id.dailyNextButton) ? 1 : -1);
                }
            };
            next.setOnClickListener(changeDay);
            prev.setOnClickListener(changeDay);
            model.setMedAdapter(new DailyViewPagerAdapter(model.getDateList(), model.getMedications().getValue(), getActivity(), mainModel, model));
            dailyViewPager.setAdapter(model.getMedAdapter());
            dailyViewPager.setCurrentItem(model.getDateList().size()-2);
            dailyViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    if (model.getDateList().size() != 2 && position != 1 || position != 0) {
                        final int dir = position + 2 - model.getDateList().size();
                        final Calendar cal2 = Calendar.getInstance();
                        cal2.setTimeInMillis(model.getDate());
                        cal2.add(Calendar.DAY_OF_YEAR, 2*dir);
                        dailyViewPager.post(new Runnable() {
                            @Override
                            public void run() {
                                if(dir < 0){
                                    model.setNextDate(model.getDate());
                                    model.setDate(model.getPrevDate());
                                    model.setPrevDate(cal2.getTimeInMillis());
                                    model.loadPrevMeds();
                                } else if (dir == 1){
                                    model.setPrevDate(model.getDate());
                                    model.setDate(model.getNextDate());
                                    model.setNextDate(cal2.getTimeInMillis());
                                    model.loadNextMeds();
                                }
                                model.getMedAdapter().notifyDataSetChanged(); //this just works now?
                                if (model.getDateList().size() == 2)
                                    dailyViewPager.setCurrentItem(0);
                                else
                                    dailyViewPager.setCurrentItem(1);
                                updateText();
                                prev.setEnabled(model.getPrevDate() != -1);
                                prev.setVisibility((model.getPrevDate() != -1) ? View.VISIBLE : View.INVISIBLE);
                            }
                        });
                    }
                }
            });
            prev.setEnabled(model.getPrevDate() != -1);
            prev.setVisibility((model.getPrevDate() != -1) ? View.VISIBLE : View.INVISIBLE);
        } else {
            next.setVisibility(View.INVISIBLE);
            prev.setVisibility(View.INVISIBLE);
            model.setMedAdapter(new DailyViewPagerAdapter(Collections.singletonList(model.getDateList().get(model.getDateList().size()-2)), Collections.singletonList(Objects.requireNonNull(model.getMedications().getValue()).get(model.getDateList().size()-2)), getActivity(), mainModel, model));
            dailyViewPager.setAdapter(model.getMedAdapter());
            dailyViewPager.setUserInputEnabled(false);
        }
        return root;
    }

    private void changeDate(int dir){
        dailyViewPager.setCurrentItem(dailyViewPager.getCurrentItem()+dir);
    }

    private void updateText() {
        String dateText = new SimpleDateFormat("EEE, MMM d", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(model.getDate());
        String year = new SimpleDateFormat("yyyy", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(model.getDate());
        if (!year.equals(Calendar.getInstance().get(Calendar.YEAR) + "")) {
            dateText += " " + year;
        }
        date.setText(dateText);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (model.getTimePickerDialog() != null){
            if (model.getTimePickerDialog().isShowing()) {
                model.getTimePickerDialog().setOnCancelListener(null);
                model.getTimePickerDialog().dismiss();
            }
        }
        super.onSaveInstanceState(outState);
    }
}
