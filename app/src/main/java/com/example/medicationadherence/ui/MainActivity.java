package com.example.medicationadherence.ui;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;
import androidx.work.BackoffPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.medicationadherence.R;
import com.example.medicationadherence.background.MedActivationWorker;
import com.example.medicationadherence.background.UpdateLogsWorker;
import com.example.medicationadherence.data.Repository;
import com.example.medicationadherence.data.room.entities.MedData;
import com.google.android.material.navigation.NavigationView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if(prefs.getBoolean("enableDMOverride", false)) {
            if (prefs.getBoolean("darkModeEnable", false))
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        super.onCreate(savedInstanceState);
        MainViewModel model = new ViewModelProvider(this).get(MainViewModel.class);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.dailyMedListFragment2, R.id.nav_home, R.id.nav_summary, R.id.nav_medications, R.id.wizardDoctorDetailFragment2, R.id.nav_settings)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
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
            OneTimeWorkRequest activateWorkRequest = new OneTimeWorkRequest.Builder(MedActivationWorker.class).setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, OneTimeWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS).addTag("activation").build();
            if (workManager.getWorkInfosByTag("activation").get() != null && workManager.getWorkInfosByTag("activation").get().size() > 1) {
                workManager.cancelAllWorkByTag("activation");
            }
            if (workManager.getWorkInfosByTag("activation").get() == null || workManager.getWorkInfosByTag("activation").get().size() == 0) {
                workManager.enqueue(activateWorkRequest);
            }
        } catch (InterruptedException | ExecutionException e){e.printStackTrace();}
        if (model.getRepository().getMedDataSize() < 1){
            InputStream inputStream = getResources().openRawResource(R.raw.rxterm_trimmed);
            new FillMedDataDB(model.getRepository(), inputStream).execute();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private static class FillMedDataDB extends AsyncTask<Void, Void, Void>{
        private Repository repository;
        private InputStream inputStream;

        FillMedDataDB(Repository repository, InputStream inputStream) {
            this.repository = repository;
            this.inputStream = inputStream;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    String[] sepLine = line.split("\\|");
                    if (!sepLine[1].equals("")){
                        repository.insert(new MedData(Integer.parseInt(sepLine[0]), Integer.parseInt(sepLine[1]), sepLine[2], sepLine[3]));
                    } else {
                        repository.insert(new MedData(Integer.parseInt(sepLine[0]), sepLine[2], sepLine[3]));
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }
    }
}