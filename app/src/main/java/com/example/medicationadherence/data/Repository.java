package com.example.medicationadherence.data;

import android.content.Context;
import android.os.AsyncTask;

import com.example.medicationadherence.data.room.MedicationDatabase;
import com.example.medicationadherence.data.room.dao.DoctorDAO;
import com.example.medicationadherence.data.room.dao.InstructionsDAO;
import com.example.medicationadherence.data.room.dao.MedDataDAO;
import com.example.medicationadherence.data.room.dao.MedicationDAO;
import com.example.medicationadherence.data.room.dao.MedicationLogDAO;
import com.example.medicationadherence.data.room.dao.ScheduleDAO;
import com.example.medicationadherence.data.room.entities.Doctor;
import com.example.medicationadherence.data.room.entities.Instructions;
import com.example.medicationadherence.data.room.entities.MedData;
import com.example.medicationadherence.data.room.entities.Medication;
import com.example.medicationadherence.data.room.entities.MedicationLog;
import com.example.medicationadherence.data.room.entities.Schedule;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class Repository {
    private DoctorDAO mDoctorDAO;
    private InstructionsDAO mInstructionsDAO;
    private MedicationDAO mMedicationDAO;
    private MedicationLogDAO mMedicationLogDAO;
    private ScheduleDAO mScheduleDAO;
    private MedDataDAO medDataDAO;

    public Repository(Context application){
        MedicationDatabase medDB = MedicationDatabase.getDatabase(application);
        mDoctorDAO = medDB.getDoctorsDao();
        mInstructionsDAO = medDB.getInstructionsDao();
        mMedicationDAO = medDB.getMedicationDao();
        mMedicationLogDAO = medDB.getMedicationLogDao();
        mScheduleDAO = medDB.getScheduleDao();
        medDataDAO = medDB.getMedDataDAO();
    }

    public MedicationDAO getmMedicationDAO() {
        return mMedicationDAO;
    }

    public List<Schedule> getScheduleForMed(Long id){
        try {
            return new GetScheduleFMTask(mScheduleDAO, id).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Medication> getMedList(){
        try {
            return new GetMedListTask(mMedicationDAO).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ScheduleDAO.ScheduleCard> getScheduleCard(){
        try {
            return new GetCardTask(mScheduleDAO).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public long insert(Doctor doctor){
        try {
            return new InsertAsyncTask(mDoctorDAO).execute(doctor).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<Doctor> getDoctors(){
        try {
            return new GetAllDoctorsAsyncTask(mDoctorDAO).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insert(Instructions instructions){
        new InsertAsyncTask(mInstructionsDAO).execute(instructions);
    }

    public long insert(Medication medication){
        try {
            return new InsertAsyncTask(mMedicationDAO).execute(medication).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void insert(MedicationLog medicationLog){
        new InsertAsyncTask(mMedicationLogDAO).execute(medicationLog);
    }

    public void insert(Schedule schedule){
        new InsertAsyncTask(mScheduleDAO).execute(schedule);
    }

    public void insert(MedData medData){
        new InsertAsyncTask(medDataDAO).execute(medData);
    }

    public void deleteAll(){
        new DeleteAsyncTask(mDoctorDAO, mInstructionsDAO, mMedicationDAO, mMedicationLogDAO, mScheduleDAO).execute();
    }

    public List<Doctor> getDocWithName(String name){
        try {
            return new GWNAsyncTask(mDoctorDAO).execute(name).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateDoctor(Long id, String doctorName, String practiceName, String address, String phone){
        new UpdateDoctorAsyncTask(mDoctorDAO, id, doctorName, practiceName, address, phone).execute();
    }

    public void updateMedication(Long id, String medImageURL, String name, boolean status, Long doctorID, String dosage, long startDate, long endDate, int containerVolume, double cost, long lateTime){
        new UpdateMedicationTask(mMedicationDAO, id, medImageURL, name, status, doctorID, dosage, startDate, endDate, containerVolume, cost, lateTime).execute();
    }

    public Doctor getDocWithID(Long doctorID){
        try {
            return (Doctor)new GetWIDTask(mDoctorDAO).execute(doctorID).get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Medication getMedWithID(Long medicationID){
        try {
            return (Medication)new GetWIDTask(mMedicationDAO).execute(medicationID).get();
        } catch (ExecutionException | InterruptedException e){
            e.printStackTrace();
        }
        return null;
    }

    public void remove(Schedule schedule){
        new RemoveAsyncTask(mScheduleDAO).execute(schedule);
    }

    public void remove(Medication medication){
        new RemoveAsyncTask(mMedicationDAO).execute(medication);
    }

    public void remove(Doctor doctor){
        new RemoveAsyncTask(mDoctorDAO).execute(doctor);
    }

    public int getMissed(Long medicationID, long startDate, long endDate){
        try {
            return new GetMissedTask(mMedicationLogDAO, medicationID, startDate, endDate).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getLate(Long medicationID, long startDate, long endDate){
        try {
            return new GetLateTask(mMedicationLogDAO, medicationID, startDate, endDate).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getOnTime(Long medicationID, long startDate, long endDate){
        try {
            return new GetOnTimeTask(mMedicationLogDAO, medicationID, startDate, endDate).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public long getEarliestLog(){
        try {
            return new GetEarliestTask(mMedicationLogDAO).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<MedicationDAO.IDName> getMedIDs(){
        try {
            return new GetMedIDTask(mMedicationDAO).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<MedicationLog> getDailyLogs(long date, long nextDay){
        try {
            return new GetDailyLogTask(mMedicationLogDAO, date, nextDay).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateLog(Long medicationID, long date, long oldTimeLate, long newTimeLate, boolean taken){
        new UpdateLogTask(mMedicationLogDAO, medicationID, date, oldTimeLate, newTimeLate, taken).execute();
    }

    public List<MedData> getAllMedData(){
        try {
            return new GetAllMedData(medDataDAO).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getDosages(String name){
        try {
            return new GetDosages(medDataDAO, name).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> getNames(){
        try {
            return new GetMedNames(medDataDAO).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getMedDataSize(){
        try {
            return new GetMedDataSize(medDataDAO).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public List<MedDataDAO.IntTuple> getIDsFor(String name, String dosage){
        try {
            return new GetRXCUIs(medDataDAO, name, dosage).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class GetRXCUIs extends AsyncTask<Void, Void, List<MedDataDAO.IntTuple>>{
        private MedDataDAO medDataDAO;
        private String name;
        private String dosage;

        GetRXCUIs(MedDataDAO medDataDAO, String name, String dosage) {
            this.medDataDAO = medDataDAO;
            this.name = name;
            this.dosage = dosage;
        }

        @Override
        protected List<MedDataDAO.IntTuple> doInBackground(Void... voids) {
            return medDataDAO.getIDsFor(name, dosage);
        }
    }

    private static class GetAllMedData extends AsyncTask<Void, Void, List<MedData>>{
        private MedDataDAO medDataDAO;

        GetAllMedData(MedDataDAO medDataDAO) {
            this.medDataDAO = medDataDAO;
        }

        @Override
        protected List<MedData> doInBackground(Void... voids) {
            return medDataDAO.getAllMedData();
        }
    }

    private static class GetDosages extends AsyncTask<String, Void, List<String>>{
        private MedDataDAO medDataDAO;
        private String name;

        GetDosages(MedDataDAO medDataDAO, String name) {
            this.medDataDAO = medDataDAO;
            this.name = name;
        }

        @Override
        protected List<String> doInBackground(String... strings) {
            return medDataDAO.getDosages(name);
        }
    }

    private static class GetMedNames extends AsyncTask<Void, Void, List<String>>{
        private MedDataDAO medDataDAO;

        public GetMedNames(MedDataDAO medDataDAO) {
            this.medDataDAO = medDataDAO;
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            return medDataDAO.getNames();
        }
    }

    private static class GetMedDataSize extends AsyncTask<Void, Void, Integer>{
        private MedDataDAO medDataDAO;

        public GetMedDataSize(MedDataDAO medDataDAO) {
            this.medDataDAO = medDataDAO;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return medDataDAO.getSize();
        }
    }

    private static class GWNAsyncTask extends AsyncTask<String, Void, List<Doctor>>{
        private DoctorDAO doctorDAO;

        GWNAsyncTask(DoctorDAO doctorDAO) {
            this.doctorDAO = doctorDAO;
        }

        @Override
        protected List<Doctor> doInBackground(String... strings) {
            return doctorDAO.getWithName(strings[0]);
        }
    }

    private static class GetDailyLogTask extends AsyncTask<Void, Void, List<MedicationLog>>{
        private MedicationLogDAO medicationLogDAO;
        private long date;
        private long nextDay;

        GetDailyLogTask(MedicationLogDAO medicationLogDAO, long date, long nextDay) {
            this.medicationLogDAO = medicationLogDAO;
            this.date = date;
            this.nextDay = nextDay;
        }

        @Override
        protected List<MedicationLog> doInBackground(Void... voids) {
            return medicationLogDAO.getDailyLogs(date, nextDay);
        }
    }

    private static class UpdateLogTask extends AsyncTask<Void, Void, Void>{
        private MedicationLogDAO medicationLogDAO;
        private Long medicationID;
        private long date;
        private long oldTimeLate;
        private long newTimeLate;
        private boolean taken;

        UpdateLogTask(MedicationLogDAO medicationLogDAO, Long medicationID, long date, long oldTimeLate, long newTimeLate, boolean taken) {
            this.medicationLogDAO = medicationLogDAO;
            this.medicationID = medicationID;
            this.date = date;
            this.oldTimeLate = oldTimeLate;
            this.newTimeLate = newTimeLate;
            this.taken = taken;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            medicationLogDAO.update(medicationID, date, oldTimeLate, newTimeLate, taken);
            return null;
        }
    }

    private static class InsertAsyncTask extends AsyncTask<Object, Void, Long> {
        private DoctorDAO doctorDAO;
        private InstructionsDAO instructionsDAO;
        private MedicationDAO medicationDAO;
        private MedicationLogDAO medicationLogDAO;
        private ScheduleDAO scheduleDAO;
        private MedDataDAO medDataDAO;

        InsertAsyncTask(InstructionsDAO instructionsDAO) {
            this.instructionsDAO = instructionsDAO;
        }

        InsertAsyncTask(DoctorDAO doctorDAO) {
            this.doctorDAO = doctorDAO;
        }

        InsertAsyncTask(MedicationDAO medicationDAO) {
            this.medicationDAO = medicationDAO;
        }

        InsertAsyncTask(MedicationLogDAO medicationLogDAO) {
            this.medicationLogDAO = medicationLogDAO;
        }

        InsertAsyncTask(ScheduleDAO scheduleDAO) {
            this.scheduleDAO = scheduleDAO;
        }

        public InsertAsyncTask(MedDataDAO medDataDAO) {
            this.medDataDAO = medDataDAO;
        }

        @Override
        protected Long doInBackground(Object... objects) {
            if(doctorDAO != null){
                return doctorDAO.insert((Doctor) objects[0]);
            } else if (instructionsDAO != null){
                instructionsDAO.insert((Instructions) objects[0]);
            } else if (medicationDAO != null){
                return medicationDAO.insert((Medication) objects[0]);
            } else if (medicationLogDAO != null){
                medicationLogDAO.insert((MedicationLog) objects[0]);
            } else if (scheduleDAO != null){
                scheduleDAO.insert((Schedule) objects[0]);
            } else if (medDataDAO != null){
                medDataDAO.insert((MedData) objects[0]);
            }
            return (long) -1;
        }
    }

    private static class RemoveAsyncTask extends AsyncTask<Object, Void, Void> {
        private DoctorDAO doctorDAO;
        private MedicationDAO medicationDAO;
        private ScheduleDAO scheduleDAO;

        RemoveAsyncTask(DoctorDAO doctorDAO) {
            this.doctorDAO = doctorDAO;
        }

        RemoveAsyncTask(MedicationDAO medicationDAO) {
            this.medicationDAO = medicationDAO;
        }

        RemoveAsyncTask(ScheduleDAO scheduleDAO) {
            this.scheduleDAO = scheduleDAO;
        }

        @Override
        protected Void doInBackground(Object... objects) {
            if(doctorDAO != null){
                doctorDAO.delete((Doctor) objects[0]);
            } else if (medicationDAO != null){
                medicationDAO.delete((Medication) objects[0]);
            } else if (scheduleDAO != null){
                scheduleDAO.delete((Schedule) objects[0]);
            }
            return null;
        }
    }

    private static class GetWIDTask extends AsyncTask<Object, Void, Object> {
        private DoctorDAO doctorDAO;
        private MedicationDAO medicationDAO;

        GetWIDTask(DoctorDAO doctorDAO) {
            this.doctorDAO = doctorDAO;
        }

        GetWIDTask(MedicationDAO medicationDAO){
            this.medicationDAO = medicationDAO;
        }

        @Override
        protected Object doInBackground(Object... objects) {
            Object lng = objects[0];
            if (doctorDAO != null) {
                return doctorDAO.getWithID((Long) lng);
            } else if (medicationDAO != null) {
                return medicationDAO.getMedWithID((Long) lng);
            }
            return (long) -1;
        }
    }

    private static class DeleteAsyncTask extends AsyncTask<Void, Void, Void> {
        private DoctorDAO doctorDAO;
        private InstructionsDAO instructionsDAO;
        private MedicationDAO medicationDAO;
        private MedicationLogDAO medicationLogDAO;
        private ScheduleDAO scheduleDAO;

        DeleteAsyncTask(DoctorDAO doctorDAO, InstructionsDAO instructionsDAO, MedicationDAO medicationDAO, MedicationLogDAO medicationLogDAO, ScheduleDAO scheduleDAO) {
            this.doctorDAO = doctorDAO;
            this.instructionsDAO = instructionsDAO;
            this.medicationDAO = medicationDAO;
            this.medicationLogDAO = medicationLogDAO;
            this.scheduleDAO = scheduleDAO;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            doctorDAO.clearTable();
            instructionsDAO.clearTable();
            medicationLogDAO.clearTable();
            scheduleDAO.clearTable();
            medicationDAO.clearTable();
            return null;
        }
    }

    private static class GetMedListTask extends AsyncTask<Void, Void, List<Medication>>{
        private MedicationDAO medicationDAO;

        GetMedListTask(MedicationDAO medicationDAO) {
            this.medicationDAO = medicationDAO;
        }

        @Override
        protected List<Medication> doInBackground(Void... voids) {
            return medicationDAO.getAllMedications();
        }
    }

    private static class GetScheduleFMTask extends AsyncTask<Void, Void, List<Schedule>>{
        private ScheduleDAO scheduleDAO;
        private Long id;

        GetScheduleFMTask(ScheduleDAO scheduleDAO, Long id) {
            this.scheduleDAO = scheduleDAO;
            this.id = id;
        }

        @Override
        protected List<Schedule> doInBackground(Void... voids) {
            return scheduleDAO.getScheduleForMed(id);
        }
    }

    private static class GetCardTask extends AsyncTask<Void, Void, List<ScheduleDAO.ScheduleCard>>{
        private ScheduleDAO scheduleDAO;

        GetCardTask(ScheduleDAO scheduleDAO) {
            this.scheduleDAO = scheduleDAO;
        }

        @Override
        protected List<ScheduleDAO.ScheduleCard> doInBackground(Void... voids) {
            return scheduleDAO.loadScheduled();
        }
    }

    private static class GetAllDoctorsAsyncTask extends AsyncTask<Void , Void, List<Doctor>>{
        DoctorDAO doctorDAO;

        GetAllDoctorsAsyncTask(DoctorDAO doctorDAO) {
            this.doctorDAO = doctorDAO;
        }

        @Override
        protected List<Doctor> doInBackground(Void... voids) {
            return doctorDAO.getAllDoctors();
        }
    }

    private static class UpdateDoctorAsyncTask extends AsyncTask<Void, Void, Void>{
        private DoctorDAO doctorDAO;
        private Long id;
        private String doctorName;
        private String practice;
        private String address;
        private String phone;

        UpdateDoctorAsyncTask(DoctorDAO doctorDAO, Long id, String doctorName, String practice, String address, String phone) {
            this.doctorDAO = doctorDAO;
            this.id = id;
            this.doctorName = doctorName;
            this.practice = practice;
            this.address = address;
            this.phone = phone;
        }

        @Override
        protected Void doInBackground(Void... objects) {
            doctorDAO.update(id, doctorName, practice, address, phone);
            return null;
        }
    }

    private static class UpdateMedicationTask extends AsyncTask<Void, Void, Void>{
        private MedicationDAO medicationDAO;
        private Long id;
        private String name;
        private boolean status;
        private Long doctorID;
        private String dosage;
        private long startDate;
        private long endDate;
        private int containerVolume;
        private double cost;
        private String medImageURL;
        long lateTime;

        UpdateMedicationTask(MedicationDAO medicationDAO, Long id, String medImageURL, String name, boolean status, Long doctorID, String dosage, long startDate, long endDate, int containerVolume, double cost, long lateTime) {
            this.medicationDAO = medicationDAO;
            this.id = id;
            this.name = name;
            this.status = status;
            this.doctorID = doctorID;
            this.dosage = dosage;
            this.startDate = startDate;
            this.endDate = endDate;
            this.containerVolume = containerVolume;
            this.cost = cost;
            this.medImageURL = medImageURL;
            this.lateTime = lateTime;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            medicationDAO.update(id, medImageURL, name, status, doctorID, dosage, startDate, endDate, containerVolume, cost, lateTime);
            return null;
        }
    }

    private static class GetMissedTask extends AsyncTask<Void, Void, Integer>{
        private MedicationLogDAO medicationLogDAO;
        private Long id;
        private long startDate;
        private long endDate;

        GetMissedTask(MedicationLogDAO medicationLogDAO, Long id, long startDate, long endDate) {
            this.medicationLogDAO = medicationLogDAO;
            this.id = id;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return medicationLogDAO.getMissed(id, startDate, endDate);
        }
    }

    private static class GetLateTask extends AsyncTask<Void, Void, Integer>{
        private MedicationLogDAO medicationLogDAO;
        private Long id;
        private long startDate;
        private long endDate;

        GetLateTask(MedicationLogDAO medicationLogDAO, Long id, long startDate, long endDate) {
            this.medicationLogDAO = medicationLogDAO;
            this.id = id;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return medicationLogDAO.getLate(id, startDate, endDate);
        }
    }

    private static class GetOnTimeTask extends AsyncTask<Void, Void, Integer>{
        private MedicationLogDAO medicationLogDAO;
        private Long id;
        private long startDate;
        private long endDate;

        GetOnTimeTask(MedicationLogDAO medicationLogDAO, Long id, long startDate, long endDate) {
            this.medicationLogDAO = medicationLogDAO;
            this.id = id;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return medicationLogDAO.getOnTime(id, startDate, endDate);
        }
    }

    private static class GetEarliestTask extends AsyncTask<Void, Void, Long>{
        private MedicationLogDAO medicationLogDAO;

        GetEarliestTask(MedicationLogDAO medicationLogDAO) {
            this.medicationLogDAO = medicationLogDAO;
        }

        @Override
        protected Long doInBackground(Void... voids) {
            return medicationLogDAO.getEarliestLog();
        }
    }

    private static class GetMedIDTask extends AsyncTask<Void, Void, List<MedicationDAO.IDName>>{
        private MedicationDAO medicationDAO;

        GetMedIDTask(MedicationDAO medicationDAO) {
            this.medicationDAO = medicationDAO;
        }

        @Override
        protected List<MedicationDAO.IDName> doInBackground(Void... voids) {
            return medicationDAO.getMedIDs();
        }
    }
}
