package com.company;

import java.util.ArrayList;

public class Node {
    int no;
    ArrayList<Shard> listOfShard;
    ArrayList<Double> listOfLoad;
    Boolean isActive;

    public Boolean getIsActive(){
        return isActive;
    }

    public void setIsActive(boolean bool){
        this.isActive = bool;
    }

    public void addNewShardToLoad(int index){
        for (int i=0; i< listOfLoad.size();i++){
            listOfLoad.set(i,listOfLoad.get(i)+listOfShard.get(index).getVector().get(i));
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
        this.no = no;
        listOfLoad = new ArrayList<>();
        listOfShard = new ArrayList<>();
        for (int i = 0; i< timestampNo; i++) {
            listOfLoad.add(0.0);
        }
        isActive = true;
    }
}
