package com.example.medicationadherence.ui.medications.wizard;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.os.ConfigurationCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medicationadherence.R;
import com.example.medicationadherence.adapter.ScheduleTimeAdapter;
import com.example.medicationadherence.background.notifications.AlertReceiver;
import com.example.medicationadherence.background.notifications.NotificationHelper;
import com.example.medicationadherence.data.Converters;
import com.example.medicationadherence.data.room.entities.Schedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class EditScheduleCardFragment extends Fragment implements RootWizardFragment.ErrFragment {
    private boolean fromWizard;
    private RootWizardViewModel wizardModel;
    private TextView timeErr;
    private TextView dayErr;
    private Switch daily;
    private CheckBox sun, mon, tues, wed, thurs, fri, sat;
    private boolean exitable = false;
    private boolean[] checks;
    private TimePickerDialog timePickerDialog;
    private AlertDialog doseCountDialog;
    private EditText doseCount;
    private ImageButton increaseDoses;
    private ImageButton decreaseDoses;
    private TextView doseCountSize;
    private AlertDialog dup;
    private ArrayList<Schedule> duplicates = new ArrayList<>();
    private AlertDialog hasDup;
    private NotificationManagerCompat notificationManager;
    public static String CHANNEL_1_ID = "channel1";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(fromWizard = Objects.requireNonNull(getParentFragment()).getParentFragment() instanceof RootWizardFragment){
            wizardModel = new ViewModelProvider(Objects.requireNonNull(getParentFragment().getParentFragment())).get(RootWizardViewModel.class);
            checks = Converters.intToBoolArray(EditScheduleCardFragmentArgs.fromBundle(Objects.requireNonNull(getArguments())).getDays());
            if ( wizardModel.getThisList().size() < 5)
                wizardModel.getThisList().add(this);
            else if(!wizardModel.getThisList().get(4).equals(this))
                wizardModel.getThisList().set(4,this);
        }
        wizardModel.getScheduleFD(checks);

        notificationManager = NotificationManagerCompat.from(getContext());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_schedule_card, container, false);
        Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).setTitle("Schedule");

        TextView medName = root.findViewById(R.id.scheduleMedName);
        daily = root.findViewById(R.id.scheduleDaily);
        sun = root.findViewById(R.id.scheduleSunday);
        mon = root.findViewById(R.id.scheduleMonday);
        tues = root.findViewById(R.id.scheduleTuesday);
        wed = root.findViewById(R.id.scheduleWednesday);
        thurs = root.findViewById(R.id.scheduleThursday);
        fri = root.findViewById(R.id.scheduleFriday);
        sat = root.findViewById(R.id.scheduleSaturday);
        final Button addTime = root.findViewById(R.id.scheduleSetTime);
        RecyclerView times = root.findViewById(R.id.scheduleTimes);
        timeErr = root.findViewById(R.id.timeCardTimeErr);
        dayErr = root.findViewById(R.id.timeCardDayErr);

        sun.setChecked(checks[0]);
        mon.setChecked(checks[1]);
        tues.setChecked(checks[2]);
        wed.setChecked(checks[3]);
        thurs.setChecked(checks[4]);
        fri.setChecked(checks[5]);
        sat.setChecked(checks[6]);

        final CompoundButton.OnCheckedChangeListener dailyListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sun.setChecked(isChecked);
                mon.setChecked(isChecked);
                tues.setChecked(isChecked);
                wed.setChecked(isChecked);
                thurs.setChecked(isChecked);
                fri.setChecked(isChecked);
                sat.setChecked(isChecked);
            }
        };
        daily.setOnCheckedChangeListener(dailyListener);
        CompoundButton.OnCheckedChangeListener dayListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                daily.setOnCheckedChangeListener(null);
                if(sun.isChecked() && mon.isChecked() && tues.isChecked() && wed.isChecked() && thurs.isChecked() && fri.isChecked() && sat.isChecked()){
                    daily.setChecked(true);
                } else {
                    daily.setChecked(false);
                }
                if(fromWizard && (sun.isChecked() || mon.isChecked() || tues.isChecked() || wed.isChecked() || thurs.isChecked() || fri.isChecked() || sat.isChecked()) && wizardModel.getScheduleTime() != -1)
                    exitable = true;
                daily.setOnCheckedChangeListener(dailyListener);
            }
        };
        sun.setOnCheckedChangeListener(dayListener);
        mon.setOnCheckedChangeListener(dayListener);
        tues.setOnCheckedChangeListener(dayListener);
        wed.setOnCheckedChangeListener(dayListener);
        thurs.setOnCheckedChangeListener(dayListener);
        fri.setOnCheckedChangeListener(dayListener);
        sat.setOnCheckedChangeListener(dayListener);

        times.setLayoutManager(new LinearLayoutManager(getContext()));
        if (wizardModel.getScheduleTimeAdapter() == null)
            wizardModel.setScheduleTimeAdapter(new ScheduleTimeAdapter(wizardModel, wizardModel.getDoseEntries()));
        times.setAdapter(wizardModel.getScheduleTimeAdapter());

        if(Converters.fromBoolArray(checks) != 0 && wizardModel.getDoseEntries().size() != 0)
            exitable = true;
        final TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onTimeSet(TimePicker view, final int hourOfDay, final int minute) {
                View v = inflater.inflate(R.layout.layout_dose_selector, container, false);
                increaseDoses = v.findViewById(R.id.doseCountUp);
                increaseDoses.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        double doseVal = Double.parseDouble(doseCount.getText().toString());
                        doseVal = Math.ceil(doseVal * 2.0) / 2.0;
                        if(Double.parseDouble(doseCount.getText().toString()) != doseVal){
                            doseCount.setText((doseVal + ""));
                        } else {
                            doseCount.setText(((doseVal += .5) + ""));
                        }
                        if (doseVal - .5 > 0)
                            decreaseDoses.setEnabled(true);
                    }
                });
                doseCount = v.findViewById(R.id.doseCountText);
                doseCount.setText((0+""));
                doseCount.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (s != null && !s.equals("") && count != 0) {
                            try {
                                double doseVal = Double.parseDouble(s.toString());
                                decreaseDoses.setEnabled(doseVal - .5 > 0);
                                doseCountDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(doseVal != 0);
                            } catch (NumberFormatException e){
                                decreaseDoses.setEnabled(false);
                                doseCountDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                            }
                        } else
                            doseCountDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                doseCountSize = v.findViewById(R.id.doseCountSize);
                doseCountSize.setText(("@ " + wizardModel.getMedDosage()));
                decreaseDoses = v.findViewById(R.id.doseCountDown);
                decreaseDoses.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        double doseVal = Double.parseDouble(doseCount.getText().toString());
                        doseVal = Math.floor(doseVal * 2.0) / 2.0;
                        if(Double.parseDouble(doseCount.getText().toString()) != doseVal){
                            doseCount.setText((doseVal + ""));
                        } else {
                            doseCount.setText(((doseVal -= .5) + ""));
                        }
                        if (doseVal - .5 <= 0)
                            decreaseDoses.setEnabled(false);
                    }
                });
                decreaseDoses.setEnabled(false);
                doseCountDialog = new AlertDialog.Builder(getActivity()).setView(v).setTitle("Select dose count").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Calendar c = Calendar.getInstance();
                        c.clear();
                        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c.set(Calendar.MINUTE, minute);
                        if ( fromWizard)
                            wizardModel.setScheduleTime(c.getTimeInMillis());
                        Schedule newS = new Schedule(null, Double.parseDouble(doseCount.getText().toString()), c.getTimeInMillis(), checks);
                        if (wizardModel.getScheduleFD(checks).contains(newS)){//Doesn't catch if days overlap but aren't same ex: same time/dose but one daily one on monday
                            dup = new AlertDialog.Builder(getContext()).setTitle("Error:").setMessage("Time and dosage already scheduled").setNegativeButton("OK", null).show();
                        } else {
                            wizardModel.getRemoved().remove(newS);
                            wizardModel.getScheduleFD(checks).add(newS);
                            String st = newS.getNumDoses() + " @ ";
                            if(DateFormat.is24HourFormat(getContext()))
                                st += new SimpleDateFormat("kk:mm", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(newS.getTime());
                            else
                                st += new SimpleDateFormat("hh:mm aa", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(newS.getTime());
                            wizardModel.getDoseEntries().add(st);
                            wizardModel.getScheduleTimeAdapter().notifyDataSetChanged();
                            if (fromWizard && (sun.isChecked() || mon.isChecked() || tues.isChecked() || wed.isChecked() || thurs.isChecked() || fri.isChecked() || sat.isChecked()) && wizardModel.getDoseEntries().size() != 0)
                                exitable = true;
                        }
                    }
                }).setNegativeButton("BACK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        timePickerDialog.show();
                    }
                }).create();
                doseCountDialog.show();
                doseCountDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        };
        addTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                timePickerDialog = new TimePickerDialog(getContext(), listener, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), DateFormat.is24HourFormat(getContext()));
                timePickerDialog.show();
            }
        });

        if(fromWizard){
            medName.setHeight(0);
            medName.setVisibility(View.INVISIBLE);
            ((RootWizardFragment) Objects.requireNonNull(Objects.requireNonNull(getParentFragment()).getParentFragment())).setAdd();
            ((RootWizardFragment) Objects.requireNonNull(getParentFragment().getParentFragment())).setHasLast(false);
        }
        if (savedInstanceState != null){
            if (savedInstanceState.getBoolean("timePickerVisible", false)){
                timePickerDialog = new TimePickerDialog(getContext(), listener, 0,0, DateFormat.is24HourFormat(getContext()));
                timePickerDialog.onRestoreInstanceState(savedInstanceState.getBundle("timePickerBundle"));
            }
            if (savedInstanceState.getBoolean("doseDialogVisible", false)){
                doseCountDialog = wizardModel.getDoseDialog();
                doseCountDialog.show();
            }
            if (savedInstanceState.getBoolean("dupVisible", false)){
                dup = new AlertDialog.Builder(getContext()).setTitle("Error:").setMessage("Time and dosage already scheduled").setNegativeButton("OK", null).show();
            }
            if (savedInstanceState.getBoolean("hasDupVisible", false)){
                boolean[] days = {sun.isChecked(), mon.isChecked(), tues.isChecked(), wed.isChecked(), thurs.isChecked(), fri.isChecked(), sat.isChecked()};
                for (Schedule s : wizardModel.getScheduleFD(checks)){
                    if (wizardModel.getSchedules().contains(new Schedule(s.getMedicationID(), s.getNumDoses(), s.getTime(), days))){
                        duplicates.add(s);
                    }
                }
                CharSequence[] entries = new CharSequence[duplicates.size()];
                for (int i = 0; i < duplicates.size(); i ++){
                    Schedule s = duplicates.get(i);
                    String st = s.getNumDoses() + " @ ";
                    if(DateFormat.is24HourFormat(getContext()))
                        st += new SimpleDateFormat("kk:mm", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(s.getTime());
                    else
                        st += new SimpleDateFormat("hh:mm aa", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(s.getTime());
                    entries[i] = st;
                }
                hasDup = new AlertDialog.Builder(getContext()).setTitle("Error: duplicate entries").setItems(entries, null).setNegativeButton("Remove Duplicates", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wizardModel.getScheduleFD(checks).removeAll(duplicates);
                        wizardModel.loadDoseEntries(true);
                        wizardModel.getScheduleTimeAdapter().notifyDataSetChanged();
                    }
                }).show();
            }
        }
        return root;
    }

    @Override
    public void showErrors() {
        timeErr.setVisibility(wizardModel.getDoseEntries().size() == 0 ? View.VISIBLE : View.INVISIBLE);
        dayErr.setVisibility((sun.isChecked() || mon.isChecked() || tues.isChecked() || wed.isChecked() || thurs.isChecked() || fri.isChecked() || sat.isChecked()) ? View.INVISIBLE : View.VISIBLE);
        CharSequence[] entries = new CharSequence[duplicates.size()];
        for (int i = 0; i < duplicates.size(); i ++){
            Schedule s = duplicates.get(i);
            String st = s.getNumDoses() + " @ ";
            if(DateFormat.is24HourFormat(getContext()))
                st += new SimpleDateFormat("kk:mm", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(s.getTime());
            else
                st += new SimpleDateFormat("hh:mm aa", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(s.getTime());
            entries[i] = st;
        }
        if(duplicates.size() != 0)
            hasDup = new AlertDialog.Builder(getContext()).setTitle("Error: duplicate entries").setItems(entries, null).setNegativeButton("Remove Duplicates", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    wizardModel.getScheduleFD(checks).removeAll(duplicates);
                    wizardModel.loadDoseEntries(true);
                    wizardModel.getScheduleTimeAdapter().notifyDataSetChanged();
                }
            }).show();
    }

    @Override
    public void pause() {
        boolean[] days = {sun.isChecked(), mon.isChecked(), tues.isChecked(), wed.isChecked(), thurs.isChecked(), fri.isChecked(), sat.isChecked()};
        for (Schedule s : wizardModel.getScheduleFD(checks)){
            s.setWeekdays(days);
            if (!wizardModel.getSchedules().contains(s)){
                wizardModel.getSchedules().add(s);
            }
        }
        wizardModel.getSchedules().removeAll(wizardModel.getRemoved());
        wizardModel.setDoseNull();
    }

    @Override
    public boolean isExitable() {
        duplicates.clear();
        boolean[] days = {sun.isChecked(), mon.isChecked(), tues.isChecked(), wed.isChecked(), thurs.isChecked(), fri.isChecked(), sat.isChecked()};
        for (Schedule s : wizardModel.getScheduleFD(checks)){
            if (Converters.fromBoolArray(checks) != Converters.fromBoolArray(days) && wizardModel.getSchedules().contains(new Schedule(s.getMedicationID(), s.getNumDoses(), s.getTime(), days))){
                duplicates.add(s);
            }
        }
        if (duplicates.size() == 0)
            return exitable;
        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (this.isVisible()){
            if (timePickerDialog != null){
                outState.putBoolean("timePickerVisible", timePickerDialog.isShowing());
                outState.putBundle("timePickerBundle", timePickerDialog.onSaveInstanceState());
            }
            if (doseCountDialog != null){
                outState.putBoolean("doseDialogVisible", doseCountDialog.isShowing());
                wizardModel.setDoseDialog(doseCountDialog);
            }
            if (dup != null){
                outState.putBoolean("dupVisible", dup.isShowing());
            }
            if (hasDup != null){
                outState.putBoolean("hasDupVisible", hasDup.isShowing());
            }
        }
        if (timePickerDialog != null)
            timePickerDialog.dismiss();
        if (doseCountDialog != null)
            doseCountDialog.dismiss();
        if (dup != null)
            dup.dismiss();
        if (hasDup != null)
            hasDup.dismiss();
        super.onSaveInstanceState(outState);
    }

    //@Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hourOfDay);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        startAlarm(c);
    }

    private void startAlarm(Calendar c){
        AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 1, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    public void sendOnChannel1(String title, String message) {
        NotificationHelper nh = new NotificationHelper(getContext());
        NotificationCompat.Builder nb = nh.getChannel1Notification(title, message);
        nh.getManager().notify(1, nb.build());
    }
}
