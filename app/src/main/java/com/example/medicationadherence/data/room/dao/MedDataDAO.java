package com.example.medicationadherence.data.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.medicationadherence.data.room.entities.MedData;

import java.util.List;

@Dao
public interface MedDataDAO {
    @Insert
    void insert(MedData... medData);

    @Query("SELECT * FROM MedData")
    List<MedData> getAllMedData();

    @Query("SELECT dosage FROM MedData WHERE name = :name group by lower(dosage)")
    List<String> getDosages(String name);

    @Query("select name from meddata group by lower(name)")
    List<String> getNames();

    @Query("SELECT count(*) from meddata")
    int getSize();

//    @Query("select distinct id from (select id from meddata where name = :name and dosage = :dosage union all select altId as id from meddata where name = :name and dosage = :dosage)")
//    List<Integer> getIDsFor(String name, String dosage);

    @Query("select distinct id, altId from meddata where name = :name and dosage = :dosage")
    List<IntTuple> getIDsFor(String name, String dosage);

    class IntTuple{
        public int id;
        public int altId;

        public IntTuple(int id, int altId) {
            this.id = id;
            this.altId = altId;
        }
    }
}
