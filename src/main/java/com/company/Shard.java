package com.company;

import java.util.ArrayList;

public class Shard {
    int no;
    ArrayList<Double> vector;
    Double module;

    public Shard(int  no, ArrayList<Double> vector){
        this.no = no;
        this.vector = vector;
        module = calculateModule();
    }

    public int getNo(){
        return no;
    }

    public ArrayList<Double> getVector(){
        return vector;
    }

    public int getVectorSize(){
        return vector.size();
    }

    public int getModule(){
        return (int) Math.round(module);
    }

    public Double calculateModule(){
        Double currentValue = 0.0;
        for (Double element: vector ) {
            currentValue += element*element;
        }
        return Math.sqrt(currentValue);
    }


}
