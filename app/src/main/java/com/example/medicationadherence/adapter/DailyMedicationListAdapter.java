package com.example.medicationadherence.adapter;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.os.ConfigurationCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.medicationadherence.R;
import com.example.medicationadherence.data.room.dao.ScheduleDAO;
import com.example.medicationadherence.data.room.entities.MedicationLog;
import com.example.medicationadherence.ui.MainViewModel;
import com.example.medicationadherence.ui.home.DailyMedListViewModel;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DailyMedicationListAdapter extends RecyclerView.Adapter implements Serializable {
    private List<ScheduleDAO.ScheduleCard> medicationList;
    private Activity activity;
    private boolean larger;
    private List<MedicationLog> logList;
    private MainViewModel mainModel;
    private long date;
    private DailyMedListViewModel dailyModel;
    private Bundle bundle;

    DailyMedicationListAdapter(List<ScheduleDAO.ScheduleCard> medicationList, Activity activity, MainViewModel mainModel, List<MedicationLog> logList, long date, DailyMedListViewModel dailyModel){
        this.medicationList = medicationList;
        this.activity = activity;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        larger = prefs.getBoolean("useWideImages", false);
        this.mainModel = mainModel;
        this.logList = logList;
        this.date = date;
        this.dailyModel = dailyModel;
    }

    @NonNull
    @Override
    public DailyMedicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View medicationView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_daily_medication_card, parent, false);
        return new DailyMedicationViewHolder(medicationView);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final DailyMedicationViewHolder holderm = (DailyMedicationViewHolder) holder;
        if(medicationList.get(position).medImageURL != null && !medicationList.get(position).medImageURL.equals("")){ //If an image is specified it will load, otherwise the default is a pill on a background
            Glide.with(activity).load(medicationList.get(position).medImageURL).thumbnail(0.5f).transition(new DrawableTransitionOptions().crossFade()).diskCacheStrategy(DiskCacheStrategy.ALL).into(holderm.medImage);
            holderm.medImage.setBackgroundColor(Integer.parseInt("00FFFFFF", 16));
            holderm.medImage.setImageTintList(null);
            holderm.medImage.setContentDescription(medicationList.get(position).medName + " image");
        }
        if (!larger)
            holderm.medImage.getLayoutParams().width = 80 * activity.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT;
        holderm.medName.setText(medicationList.get(position).medName);
        String instr = medicationList.get(position).instructions;
        if(instr != null && !instr.equals("")){
            holderm.instructions.setText(medicationList.get(position).instructions);
            View.OnClickListener expand = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holderm.expand.getRotation() == 180){
                        holderm.expand.setRotation(0);
                        holderm.instructions.setVisibility(View.GONE);
                    } else {
                        holderm.expand.setRotation(180);
                        holderm.instructions.setVisibility(View.VISIBLE);
                    }
                }
            };
            holderm.card.setOnClickListener(expand);
            holderm.expand.setOnClickListener(expand);
        } else {
            holderm.expand.setVisibility(View.GONE);
        }
        holderm.medDosage.setText((medicationList.get(position).doses + "@" + medicationList.get(position).dosageAmt));
        String st = "";
        if(DateFormat.is24HourFormat(activity))
            st += new SimpleDateFormat("kk:mm", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(medicationList.get(position).timeOfDay);
        else
            st += new SimpleDateFormat("hh:mm aa", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(medicationList.get(position).timeOfDay);
        holderm.dosageTime.setText(st);

        int pos = -1;
        for (int i = 0; i < logList.size(); i++) {
            MedicationLog m = logList.get(i);
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(m.getDate());
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            c.clear();
            c.set(Calendar.HOUR_OF_DAY, hour);
            c.set(Calendar.MINUTE, minute);
            if (medicationList.get(position).medicationID.equals(m.getMedicationID()) && c.getTimeInMillis() == medicationList.get(position).timeOfDay)
                pos = i;
        }
        if (pos != -1){
            MedicationLog medicationLog = logList.get(pos);
            if (medicationLog.getTaken()){
                holderm.taken.setChecked(true);
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(medicationLog.getDate());
                c.setTimeInMillis(medicationLog.getDate() + logList.get(pos).getTimeLate());
                st = "Taken @ ";
                if(DateFormat.is24HourFormat(activity))
                    st += new SimpleDateFormat("kk:mm", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(c.getTimeInMillis());
                else
                    st += new SimpleDateFormat("hh:mm aa", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(c.getTimeInMillis());
                holderm.taken.setText(st);
            } else {
                holderm.missed.setChecked(true);
            }
        }
        Calendar temp = Calendar.getInstance();
        long hr = temp.get(Calendar.HOUR_OF_DAY);
        long min = temp.get(Calendar.MINUTE);
        temp.clear(Calendar.MILLISECOND);
        temp.clear(Calendar.SECOND);
        temp.clear(Calendar.MINUTE);
        temp.clear(Calendar.HOUR_OF_DAY);
        temp.clear(Calendar.HOUR);
        temp.clear(Calendar.AM_PM);
        long lateTime = mainModel.getMedWithID(medicationList.get(position).medicationID).getLateTime();
        Calendar medTime = Calendar.getInstance();
        medTime.setTimeInMillis(medicationList.get(position).timeOfDay-lateTime);
        if(date == temp.getTimeInMillis() && hr*60+min >= medTime.get(Calendar.HOUR_OF_DAY)*60 + medTime.get(Calendar.MINUTE)) {
            View.OnClickListener radioListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = -1;
                    for (int i = 0; i < logList.size(); i++) {
                        MedicationLog m = logList.get(i);
                        Calendar c = Calendar.getInstance();
                        c.setTimeInMillis(m.getDate());
                        int hour = c.get(Calendar.HOUR_OF_DAY);
                        int minute = c.get(Calendar.MINUTE);
                        c.clear();
                        c.set(Calendar.HOUR_OF_DAY, hour);
                        c.set(Calendar.MINUTE, minute);

                        Calendar c1 = Calendar.getInstance();
                        c1.setTimeInMillis(medicationList.get(position).timeOfDay);
                        hour = c1.get(Calendar.HOUR_OF_DAY);
                        minute = c1.get(Calendar.MINUTE);
                        c1.clear();
                        c1.set(Calendar.HOUR_OF_DAY, hour);
                        c1.set(Calendar.MINUTE, minute);

                        if (medicationList.get(position).medicationID.equals(m.getMedicationID()) && c.getTimeInMillis() == c1.getTimeInMillis())
                            pos = i;
                    }
                    if (v.getId() == R.id.dailyMedMissed) {
                        dailyModel.setOpenPos(-1);
                        holderm.taken.setText(("Taken"));
                        if (pos == -1) {
                            Calendar out = Calendar.getInstance();
                            out.setTimeInMillis(date);
                            Calendar temp = Calendar.getInstance();
                            temp.setTimeInMillis(medicationList.get(position).timeOfDay);
                            out.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
                            out.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
                            long medDate = out.getTimeInMillis();
                            mainModel.insert(new MedicationLog(medicationList.get(position).medicationID, medDate, false, 0));
                        } else {
                            if (logList.get(pos).getTaken())
                                mainModel.updateMedLog(logList.get(pos).getMedicationID(), logList.get(pos).getDate(), logList.get(pos).getTimeLate(), 0, false);
                        }
                    } else {
                        if (position == dailyModel.getOpenPos() && dailyModel.getTimePickerDialog() != null)
                            bundle = dailyModel.getTimePickerDialog().onSaveInstanceState();
                        if (pos == -1){
                            final Calendar out = Calendar.getInstance();
                            out.setTimeInMillis(date);
                            Calendar temp = Calendar.getInstance();
                            temp.setTimeInMillis(medicationList.get(position).timeOfDay);
                            out.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
                            out.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
                            final long medDate = out.getTimeInMillis();
                            TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    Calendar temp = Calendar.getInstance();
                                    temp.clear();
                                    temp.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    temp.set(Calendar.MINUTE, minute);
                                    String st = "Taken @ ";
                                    if(DateFormat.is24HourFormat(activity))
                                        st += new SimpleDateFormat("kk:mm", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(temp.getTimeInMillis());
                                    else
                                        st += new SimpleDateFormat("hh:mm aa", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(temp.getTimeInMillis());
                                    holderm.taken.setText(st);
                                    Calendar out1 = Calendar.getInstance();
                                    out1.setTimeInMillis(out.getTimeInMillis());
                                    out1.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
                                    out1.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
                                    out1.set(Calendar.MILLISECOND, 0);
                                    out1.set(Calendar.SECOND, 0);
                                    mainModel.insert(new MedicationLog(medicationList.get(position).medicationID, medDate, true, out1.getTimeInMillis()-out.getTimeInMillis()));
                                    dailyModel.setOpenPos(-1);
                                    bundle = dailyModel.getTimePickerDialog().onSaveInstanceState();
                                }
                            };
                            dailyModel.setListener(listener);
                            dailyModel.setTimePickerDialog(new TimePickerDialog(activity, dailyModel.getListener(), out.get(Calendar.HOUR_OF_DAY), out.get(Calendar.MINUTE), DateFormat.is24HourFormat(activity)));
                            dailyModel.getTimePickerDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    dailyModel.setOpenPos(-1);
                                }
                            });
                            if (bundle != null){
                                dailyModel.getTimePickerDialog().onRestoreInstanceState(bundle);
                            }
                            dailyModel.setOpenPos(position);
                            dailyModel.getTimePickerDialog().show();
                        } else {
                            final int pos1 = pos;
                            final Calendar out = Calendar.getInstance();
                            out.setTimeInMillis(date);
                            Calendar temp = Calendar.getInstance();
                            temp.setTimeInMillis(medicationList.get(position).timeOfDay + logList.get(pos).getTimeLate());
                            out.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
                            out.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
                            TimePickerDialog.OnTimeSetListener listener = new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                    Calendar temp = Calendar.getInstance();
                                    temp.clear();
                                    temp.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                    temp.set(Calendar.MINUTE, minute);
                                    String st = "Taken @ ";
                                    if(DateFormat.is24HourFormat(activity))
                                        st += new SimpleDateFormat("kk:mm", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(temp.getTimeInMillis());
                                    else
                                        st += new SimpleDateFormat("hh:mm aa", ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).get(0)).format(temp.getTimeInMillis());
                                    holderm.taken.setText(st);
                                    Calendar out1 = Calendar.getInstance();
                                    out1.setTimeInMillis(out1.getTimeInMillis());
                                    out1.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
                                    out1.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
                                    out1.set(Calendar.MILLISECOND, 0);
                                    out1.set(Calendar.SECOND, 0);
                                    Calendar out2 = Calendar.getInstance();
                                    out2.setTimeInMillis(date);
                                    temp.setTimeInMillis(medicationList.get(position).timeOfDay);
                                    out2.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
                                    out2.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
                                    mainModel.updateMedLog(logList.get(pos1).getMedicationID(), logList.get(pos1).getDate(), logList.get(pos1).getTimeLate(), out1.getTimeInMillis() - out2.getTimeInMillis(), true);
                                    dailyModel.setOpenPos(-1);
                                    bundle = dailyModel.getTimePickerDialog().onSaveInstanceState();
                                }
                            };
                            dailyModel.setListener(listener);
                            dailyModel.setTimePickerDialog(new TimePickerDialog(activity, dailyModel.getListener(), temp.get(Calendar.HOUR_OF_DAY), temp.get(Calendar.MINUTE), DateFormat.is24HourFormat(activity)));
                            dailyModel.getTimePickerDialog().setOnCancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    dailyModel.setOpenPos(-1);
                                }
                            });
                            if (bundle != null)
                                    dailyModel.getTimePickerDialog().onRestoreInstanceState(bundle);
                            dailyModel.setOpenPos(position);
                            dailyModel.getTimePickerDialog().show();
                        }
                    }
                    logList = mainModel.getDailyLogs(date);//could modify list instead of completely reloading
                }
            };
            holderm.missed.setOnClickListener(radioListener);
            holderm.taken.setOnClickListener(radioListener);
            if (position == dailyModel.getOpenPos()) {
                holderm.taken.performClick();
            }
        } else {
            holderm.missed.setEnabled(false);//use setClickable to disable w/o greying out
            holderm.taken.setEnabled(false);
        }
    }

    @Override
    public int getItemCount() {
        return medicationList.size();
    }

    public class DailyMedicationViewHolder extends RecyclerView.ViewHolder{
        final ImageView medImage;
        final TextView medName;
        final TextView instructions;
        final TextView medDosage;
        final TextView dosageTime;
        final ImageButton expand;
        final CardView card;
        final RadioButton missed;
        final RadioButton taken;

        DailyMedicationViewHolder(View view){
            super(view);
            medImage=view.findViewById(R.id.medImage);
            medName=view.findViewById(R.id.textViewMedName);
            instructions=view.findViewById(R.id.dailyInstructions);
            medDosage=view.findViewById(R.id.textViewMedDosage);
            dosageTime=view.findViewById(R.id.textViewDosageTime);
            expand=view.findViewById(R.id.dailyMedicationExpand);
            missed=view.findViewById(R.id.dailyMedMissed);
            taken=view.findViewById(R.id.dailyMedTaken);
            card=view.findViewById(R.id.dailyCard);
        }
    }
}