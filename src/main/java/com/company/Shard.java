package com.company;

import java.util.ArrayList;

public class Shard {
    private final int no;
    private final ArrayList<Double> vector;
    private Double module;
    private Boolean isActive;

    public Shard(int  no, ArrayList<Double> vector){
        this.no = no;
        this.vector = vector;
        module = Main.calculateModule(vector);
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
        this.module = Main.calculateModule(vector);
    }

    public Double calculateUnbalancedFactor(ArrayList<Double> unbalancedVector) {
        ArrayList<Double> unbalancedVector2 = new ArrayList<>();

        for (int i = 0; i < unbalancedVector.size(); i++) {
            unbalancedVector2.add(unbalancedVector.get(i));
        }

        for (int i = 0; i < unbalancedVector2.size(); i++) {
            if (unbalancedVector2.get(i) < 0) {
                unbalancedVector2.set(i, 0.0);
            }
        }

        return Main.calculateModule(Main.subVectors(unbalancedVector2, vector));
    }

}
