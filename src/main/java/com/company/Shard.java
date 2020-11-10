package com.company;

import com.sun.org.apache.xpath.internal.operations.Bool;

import java.util.ArrayList;

public class Shard {
    private int no;
    private ArrayList<Double> vector;
    private Double module;
    private Boolean isActive;

    public Shard(int  no, ArrayList<Double> vector){
        this.no = no;
        this.vector = vector;
        module = calculateModule();
        isActive = true;
    }

    public Shard(Shard shard){
        this.no = shard.getNo();
        this.vector = (ArrayList<Double>) shard.getVector().clone();
        this.module = shard.getDoubleModule();
        this.isActive = shard.getIsActive();
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

    public Boolean getIsActive() { return isActive; }

    public void setActive(Boolean active) {
        isActive = active;
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

    public Double calculateUnbalancedFactor(ArrayList<Double> unbalancedVector) {
        for (int i = 0; i < unbalancedVector.size(); i++) {
            if (unbalancedVector.get(i) < 0) {
                unbalancedVector.set(i, 0.0);
            }
        }

        return Main.calculateModule(Main.subVectors(unbalancedVector, vector));
    }

}
