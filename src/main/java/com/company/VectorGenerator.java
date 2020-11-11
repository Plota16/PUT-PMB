package com.company;

import org.apache.commons.math3.distribution.NormalDistribution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.concurrent.atomic.AtomicBoolean;

public class VectorGenerator {


    private int M;
    private int n;
    private double mean;
    private double sd;
    private String filepath;

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }


    public int getM() {
        return M;
    }

    public void setM(int m) {
        M = m;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public double getMean() {
        return mean;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public double getSd() {
        return sd;
    }

    public void setSd(double sd) {
        this.sd = sd;
    }


    VectorGenerator(int M, int n, double mean, double sd, String filepath){
        this.M = M;
        this.n = n;
        this.mean = mean;
        this.sd = sd;
        this.filepath = filepath;
    }


    public void generate() throws IOException{
        NormalDistribution normalDistribution = new NormalDistribution(mean, sd);

        File file = new File(filepath);
        file.createNewFile();
        BufferedWriter writer = new BufferedWriter(new FileWriter(file, false));

        for (int i = 0; i < M; i++) {
            double[] distributionArray = normalDistribution.sample(n);

            double[] fixedDistributionArray = Arrays.stream(distributionArray).map(t -> {
                if (t > 100) {
                    t = 100.0;
                }
                if (t < 0) {
                    t = 0;
                }
                return t;
            }).toArray();

            OptionalDouble test = Arrays.stream(fixedDistributionArray).filter(s -> s > 100).findFirst();
            if (test.isPresent()) {
                System.out.println("Double większy od 100: " + test.getAsDouble());
            }
//            System.out.println(Arrays.toString(fixedDistributionArray));


            AtomicBoolean isFirstRow = new AtomicBoolean(true);

            Arrays.stream(fixedDistributionArray).forEach(item -> {
                try {
                    if (isFirstRow.get()) {
                        writer.write(String.valueOf(item));
                        isFirstRow.set(false);
                    } else {
                        writer.write(", " + item);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.write("\r\n");
        }

        writer.close();
    }
}
