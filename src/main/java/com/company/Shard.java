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

    public Shard(Shard shard){
        this.no = shard.getNo();
        this.vector = (ArrayList<Double>) shard.getVector().clone();
        this.module = shard.getDoubleModule();
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

    public double getDoubleModule(){
        return module;
    }

    public void recalculateModule(){
        this.module = calculateModule();
    }

    public Double calculateModule(){
        Double currentValue = 0.0;
        for (Double element: vector ) {
            currentValue += element*element;
        }
        return Math.sqrt(currentValue);
    }


}
