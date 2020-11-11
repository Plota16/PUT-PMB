package com.company;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class Node {
    private final int no;
    private final ArrayList<Shard> listOfShard;
    private final ArrayList<Double> listOfLoad;
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

    public Node(Node node) {
        this.unbalancedVector = node.getUnbalancedVector();
        this.no = node.getNo();
        listOfLoad = node.getListOfLoad();
        listOfShard = (ArrayList<Shard>) node.getListOfShard().stream().map(Shard::new).collect(Collectors.toList());
        isActive = node.getIsActive();
    }

    public Shard getMostUnbalancedShard() {
        Double minModule = null;
        Double module;
        Shard resultShard = null;
        for (Shard shard : this.getListOfShard()) {
            if (shard.getIsActive()) {
                module = shard.calculateUnbalancedFactor(this.getUnbalancedVector());
                if (minModule == null) {
                    minModule = module;
                    resultShard = shard;
                }
                else if (module < minModule) {
                    minModule = module;
                    resultShard = shard;
                }
            }
        }
        return resultShard;
    }

    public void setAllShardsActive() {
        for (Shard shard : listOfShard) {
            shard.setActive(true);
        }
    }

    public Boolean hasActiveShard() {
        for (Shard shard : this.getListOfShard()) {
            if (shard.getIsActive()) {
                return true;
            }
        }
        return false;
    }
}
