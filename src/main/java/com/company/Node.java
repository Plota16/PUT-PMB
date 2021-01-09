package com.company;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Node {
    private final int no;
    private final ArrayList<Shard> listOfShard;
    private ArrayList<Double> listOfLoad;
    private ArrayList<Double> unbalancedVector;
    private Boolean isActive;
    private ArrayList<Double> averageDelay;

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

    public double getSumLoad() {
        return listOfLoad.stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    public double estimateNodeLatency(double power, double taskFrequencyFactor, int timestampsCount) {
        double p = getSumLoad() / (power * timestampsCount);
        return getSumLoad() * (p / (100 - p)) * ((taskFrequencyFactor * taskFrequencyFactor + 1) / 2);
    }

    public void estimateNodeLatency2() {
        double p_i = 0.0;
        double Ca_i = Main.getSD(this.getListOfLoad()) / Main.getMean(this.getListOfLoad());
        ArrayList<Double> p_ij = new ArrayList<Double>();
        for (int j = 0; j < averageDelay.size(); j++) {
            double sum = 0;
            for (Shard shard : this.getListOfShard()) {
                sum += shard.getVector().get(j);
            }
            p_ij.add(sum / Main.power);
            p_i += (sum / Main.power);
        }
        p_i /= averageDelay.size();

        for (int j = 0; j < averageDelay.size(); j++) {
            double Ca_ij = p_ij.get(j) / p_i * Ca_i;
            double Cs_ij = p_ij.get(j) / p_i * Main.C_Si;
            double result = (p_ij.get(j) / (1 - p_ij.get(j))) * ((Ca_ij * Ca_ij + Cs_ij * Cs_ij)/2) * Main.E_S;
            averageDelay.set(j, result);

            //sprawdzanie czy akumulacja zadań nieobsłużonych nastepuje
            double p_lj = 1 / ((Ca_ij * Ca_ij + Cs_ij * Cs_ij) * Main.E_S + 1);
            if (p_ij.get(j) > p_lj) {
                System.out.println("p_ij: " + p_ij.get(j) + "; p_lj: " + p_lj);
            }
        }
    }

    public void recalculateLoad(){
        for (int i = 0; i < listOfLoad.size(); i++) {
            listOfLoad.set(i, 0.0);
        }
        for (Shard shard : listOfShard){
            for (int i = 0; i<listOfLoad.size();i++){
                double value = listOfLoad.get(i) + shard.getVector().get(i);
                listOfLoad.set(i, value);
            }
        }
    }

    public Double getAverageSum() {
        double result = 0;
        for (int i = 0; i < this.averageDelay.size(); i++) {
            result += averageDelay.get(i);
        }
        return result;
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
        averageDelay = new ArrayList<>();
        for (int i = 0; i< timestampNo; i++) {
            listOfLoad.add(0.0);
            averageDelay.add(0.0);
        }
        isActive = true;
    }

    public ArrayList<Double> getAverageDelay() {
        return averageDelay;
    }

    public void setAverageDelay(ArrayList<Double> averageDelay) {
        this.averageDelay = averageDelay;
    }

    public Node(Node node) {
        this.unbalancedVector = node.getUnbalancedVector();
        this.no = node.getNo();
        listOfLoad = node.getListOfLoad();
        listOfShard = (ArrayList<Shard>) node.getListOfShard().stream().map(Shard::new).collect(Collectors.toList());
        isActive = node.getIsActive();
        averageDelay = node.getAverageDelay();
    }

    public Shard getMostUnbalancedShard() {
        double minModule = 0.0;
        double module;
        Shard resultShard = null;
        boolean intialized = false;
        for (Shard shard : listOfShard) {
            if (shard.getIsActive()) {
                module = shard.calculateUnbalancedFactor(unbalancedVector);
                if (!intialized) {
                    minModule = module;
                    resultShard = shard;
                    intialized = true;
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

    public void checkIfPowerIsSufficient() {
        for (int i = 0; i < listOfLoad.size(); i++) {
            if (listOfLoad.get(i) > Main.power) {
                System.out.println("Load: " + listOfLoad.get(i) + "; Power: " + Main.power);
            }
        }
    }
}
