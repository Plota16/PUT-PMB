package com.company;

import java.util.ArrayList;

public class Node {
    private int no;
    private ArrayList<Shard> listOfShard;
    private ArrayList<Double> listOfLoad;
    private ArrayList<Double> unbalancedVector;
    private Boolean isActive;

    public ArrayList<Double> getUnbalancedVector(){
        return unbalancedVector;
    }

    public void setUnbalancedVector(ArrayList<Double> input){
        this.unbalancedVector = input;
    }

    public Boolean getIsActive(){
        return isActive;
    }

    public void setIsActive(boolean bool){
        this.isActive = bool;
    }

    public void recalculateLoad(){
        for (int i = 0; i< listOfLoad.size(); i++) {
            listOfLoad.set(i,0.0);
        }
        for (Shard shard : listOfShard){
            for (int i = 0; i<listOfLoad.size();i++){
                listOfLoad.set(i,listOfLoad.get(i)+shard.getVector().get(i));
            }
        }
    }

    public ArrayList<Double> getListOfLoad() {
        return listOfLoad;
    }

    public int getNo(){
        return no;
    }

    public ArrayList<Shard> getListOfShard(){
        return listOfShard;
    }

    public Node(int no, int timestampNo) {
        this.unbalancedVector = new ArrayList<>();
        this.no = no;
        listOfLoad = new ArrayList<>();
        listOfShard = new ArrayList<>();
        for (int i = 0; i< timestampNo; i++) {
            listOfLoad.add(0.0);
        }
        isActive = true;
    }
}
