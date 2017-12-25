package th.ac.cmru.computer.cmcas;

import android.content.Context;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtility {
    private ChkAnsData dbHelper;

    public FileUtility(Context context) {
        dbHelper = new ChkAnsData(context);
    }

    public boolean importCSV(String path) {
        try {
            CSVReader reader = new CSVReader(new FileReader(path));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null)
                dbHelper.insertStdName(dbHelper.getWritableDatabase(), nextLine);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public boolean exportCSV(String course_id, String path){
        String[][] student_score = dbHelper.selectStdScore(dbHelper.getReadableDatabase(), course_id);

        CSVWriter writer;
        try {
            writer = new CSVWriter(new FileWriter(path));

            for(String[] score:student_score) {
                writer.writeNext(new String[]{score[0],score[1],score[2],score[3]});
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
