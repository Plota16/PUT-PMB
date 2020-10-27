package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class Main {

    public static void main(String[] args) {

        ArrayList<Node> listOfNodesBySalp = salp();
        System.out.println("end");
    }







    public static ArrayList<Node> salp(){

        ArrayList<Shard> shards = loadFile();

        int numberOfNodes = 3;
        int numberOfTimestamps = shards.get(0).getVectorSize();
        int numberOfShards = shards.size();


        ArrayList<Double> sumOfVectors = new ArrayList<>();
        for (int i = 0;i<numberOfShards; i++){
            ArrayList<Double> currentShard = shards.get(i).getVector();
            for (int j = 0;j<numberOfTimestamps; j++){

                if(i==0){
                    sumOfVectors.add(currentShard.get(j));
                }
                else{
                    sumOfVectors.set(j,sumOfVectors.get(j)+currentShard.get(j));
                }

            }

        }

        ArrayList<Double> sumOfVectorsNormalized = new ArrayList<>();
        for (int i = 0;i<numberOfTimestamps; i++){
            sumOfVectorsNormalized.add(sumOfVectors.get(i)/numberOfNodes);
        }

        shards.sort((lhs, rhs) -> Integer.compare(rhs.getModule(), lhs.getModule()));

        ArrayList<Node> listOfNodes = new ArrayList<>();
        for(int i=0;i<numberOfNodes; i++){
            listOfNodes.add(new Node(i,numberOfTimestamps));
        }

        for (Shard shard: shards) {
            double result = 0.0;
            int bestNode = 0;
            for (Node node: listOfNodes) {
                if(node.getIsActive()){
                    Double moduleA = calculateModule(subVectors(node.getListOfLoad(),sumOfVectorsNormalized));
                    Double moduleB = calculateModule(subVectors(addVectors(node.getListOfLoad(),shard.getVector()),sumOfVectorsNormalized));

                    if((moduleA - moduleB) > result){
                        result = moduleA - moduleB;
                        bestNode = listOfNodes.indexOf(node);
                    }
                }
            }
            listOfNodes.get(bestNode).getListOfShard().add(shard);
            listOfNodes.get(bestNode).addNewShardToLoad(listOfNodes.get(bestNode).getListOfShard().size()-1);
            if(calculateModule(listOfNodes.get(bestNode).getListOfLoad()) > calculateModule(sumOfVectorsNormalized)){
                listOfNodes.get(bestNode).setIsActive(false);
            }
        }

        return listOfNodes;
    }



    public static ArrayList<Shard> loadFile(){
        ArrayList<Shard> result = new ArrayList<>();
        int counter = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("output.txt"));
            String line = reader.readLine();
            while (line != null) {
                ArrayList<Double> tmpArray = new ArrayList<>();
                String[] tmp = line.split(", ");
                for (String element: tmp ) {
                    tmpArray.add(Double.valueOf(element));
                }
                result.add(new Shard(counter,tmpArray));
                // read next line
                counter++;
                line = reader.readLine();
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }



    public static Double calculateModule(List<Double> list){
        double currentValue = 0.0;
        for (Double element: list ) {
            currentValue += element*element;
        }
        return Math.sqrt(currentValue);
    }

    public static ArrayList<Double> addVectors(ArrayList<Double> a, ArrayList<Double> b){
        ArrayList<Double> result = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            result.add(a.get(i) + b.get(i));
        }
        return result;
    }

    public static ArrayList<Double> subVectors(ArrayList<Double> a, ArrayList<Double> b){
        ArrayList<Double> result = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            result.add(a.get(i) - b.get(i));
        }
        return result;
    }
}

