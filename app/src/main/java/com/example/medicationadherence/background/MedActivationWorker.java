package com.example.medicationadherence.background;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.BackoffPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.medicationadherence.data.Repository;
import com.example.medicationadherence.data.room.entities.Medication;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MedActivationWorker extends Worker {
    public MedActivationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public Result doWork() {
        Repository repository = new Repository(getApplicationContext());
        List<Medication> medList = repository.getMedList();
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_YEAR);
        c.clear();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_YEAR, day);
        for(Medication m : medList){
            m.setStatus(m.getStartDate() <= c.getTimeInMillis() && (m.getEndDate() >= c.getTimeInMillis() || m.getEndDate() == -1));//use < for non-inclusive, but change in DailyMedListViewModel too
            repository.getmMedicationDAO().update(m);
        }
        Calendar curr = Calendar.getInstance();
        Calendar first = Calendar.getInstance();
        first.clear();
        first.set(Calendar.YEAR, curr.get(Calendar.YEAR));
        first.set(Calendar.MONTH, curr.get(Calendar.MONTH));
        first.set(Calendar.DAY_OF_YEAR, curr.get(Calendar.DAY_OF_YEAR));
        if (first.getTimeInMillis() < curr.getTimeInMillis())
            first.add(Calendar.DAY_OF_YEAR, 1);
        long initialDelay = first.getTimeInMillis() - curr.getTimeInMillis();
        try {
            WorkManager workManager = WorkManager.getInstance(getApplicationContext());
            OneTimeWorkRequest activateWorkRequest = new OneTimeWorkRequest.Builder(MedActivationWorker.class).setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS).addTag("activation").build();
            if (workManager.getWorkInfosByTag("activation").get() != null && workManager.getWorkInfosByTag("activation").get().size() > 1) {
                workManager.cancelAllWorkByTag("activation");
            }
            if (workManager.getWorkInfosByTag("activation").get() == null || workManager.getWorkInfosByTag("activation").get().size() == 0) {
                workManager.enqueue(activateWorkRequest);
            }
        } catch (InterruptedException | ExecutionException e){e.printStackTrace();}
        return Result.success();
    }
}
