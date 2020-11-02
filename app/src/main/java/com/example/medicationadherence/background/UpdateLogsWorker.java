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
import com.example.medicationadherence.data.room.dao.ScheduleDAO;
import com.example.medicationadherence.data.room.entities.MedicationLog;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class UpdateLogsWorker extends Worker {
    public UpdateLogsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public Result doWork() {
        Repository repository = new Repository(getApplicationContext());
        List<ScheduleDAO.ScheduleCard> scheduleCards = getScheduleCard(repository);
        Calendar c2 = Calendar.getInstance();
        int year = c2.get(Calendar.YEAR);
        int month = c2.get(Calendar.MONTH);
        int day = c2.get(Calendar.DAY_OF_YEAR);
        c2.clear();
        c2.set(Calendar.YEAR, year);
        c2.set(Calendar.MONTH, month);
        c2.set(Calendar.DAY_OF_YEAR, day-1);
        List<MedicationLog> dailyLogs = getDailyLogs(repository, c2.getTimeInMillis());
        Calendar c = Calendar.getInstance();
        for(ScheduleDAO.ScheduleCard s : scheduleCards){
            if(s.days[(c2.get(Calendar.DAY_OF_WEEK) + 6) % 7] && s.startDate <= c2.getTimeInMillis() && (s.endDate >= c2.getTimeInMillis() || s.endDate == -1) && (s.active || s.endDate == c2.getTimeInMillis())) {
                int pos = -1;
                for (int i = 0; i < dailyLogs.size(); i++) {
                    MedicationLog m = dailyLogs.get(i);
                    c.setTimeInMillis(m.getDate());
                    int hour = c.get(Calendar.HOUR_OF_DAY);
                    int minute = c.get(Calendar.MINUTE);
                    c.clear();
                    c.set(Calendar.HOUR_OF_DAY, hour);
                    c.set(Calendar.MINUTE, minute);

                    Calendar c1 = Calendar.getInstance();
                    c1.setTimeInMillis(s.timeOfDay);
                    hour = c1.get(Calendar.HOUR_OF_DAY);
                    minute = c1.get(Calendar.MINUTE);
                    c1.clear();
                    c1.set(Calendar.HOUR_OF_DAY, hour);
                    c1.set(Calendar.MINUTE, minute);

                    if (c.getTimeInMillis() == c1.getTimeInMillis())
                        pos = i;
                }
                if (pos == -1) {
                    Calendar temp = Calendar.getInstance();
                    temp.setTimeInMillis(s.timeOfDay);
                    c2.set(Calendar.HOUR_OF_DAY, temp.get(Calendar.HOUR_OF_DAY));
                    c2.set(Calendar.MINUTE, temp.get(Calendar.MINUTE));
                    repository.insert(new MedicationLog(s.medicationID, c2.getTimeInMillis(), false, 0));
                }
            }
            c2.set(Calendar.HOUR_OF_DAY, 0);
            c2.set(Calendar.MINUTE, 0);
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
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UpdateLogsWorker.class).setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS).addTag("update").build();
            if (workManager.getWorkInfosByTag("update").get() != null && workManager.getWorkInfosByTag("update").get().size() > 1) {
                workManager.cancelAllWorkByTag("update");
            }
            if (workManager.getWorkInfosByTag("update").get() == null || workManager.getWorkInfosByTag("update").get().size() == 0) {
                workManager.enqueue(workRequest);
            }
        } catch (InterruptedException | ExecutionException e){e.printStackTrace();}
        return Result.success();
    }

    private List<MedicationLog> getDailyLogs(Repository repository, long date){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_YEAR);
        c.clear();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_YEAR, day+1);
        return repository.getDailyLogs(date, c.getTimeInMillis());
    }

    private List<ScheduleDAO.ScheduleCard> getScheduleCard(Repository repository){
        return repository.getScheduleCard();
    }
}
