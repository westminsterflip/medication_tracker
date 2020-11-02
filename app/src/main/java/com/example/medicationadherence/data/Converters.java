package com.example.medicationadherence.data;

import androidx.room.TypeConverter;

public class Converters{
    @TypeConverter
    public static int fromBoolArray(boolean[] array){
        int out = 0;
        for (int i=0; i<array.length; i++){
            if(array[i])
                out += (1 << i);
        }
        return out;
    }

    @TypeConverter
    public static boolean[] intToBoolArray(int boolInt){
        boolean[] out = new boolean[8];
        for (int i = 0; i < 8; i++){
            out[i] = (boolInt & (1 << i)) !=0;
        }
        return out;
    }
}