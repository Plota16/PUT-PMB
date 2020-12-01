package com.company;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class CreateCSV {

    static void saveToCsv (String filename, String value, boolean isNewLine) {
        try {

            FileWriter fw = new FileWriter(filename, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(value);
            if (isNewLine) {
                bw.newLine();
            } else {
                bw.write(", ");
            }
            bw.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

}
