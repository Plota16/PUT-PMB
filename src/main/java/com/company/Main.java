package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class Main {

    final static int numberOfNodes = 3; //parametr

    static ArrayList<Shard> originalShards = new ArrayList<>();
    static int numberOfTimestamps;
    static int numberOfShards;

    public static void main(String[] args) {

        ArrayList<Node> listOfNodesBySalp = salp();
        ArrayList<Node> listOfNodesByIncrementalSalp = IncrementalSalp();
        System.out.println("end");
    }



    public static void randomizeShardsLoad(ArrayList<Shard> input, int randomPercent){
        Random generator = new Random();
        for (Shard shard : input){
            for (int i=0;i<shard.getVector().size();i++){
                double randomizedFactor = (generator.nextInt(randomPercent) - (randomPercent/2.0)) / 100.0;
                shard.getVector().set(i,shard.getVector().get(i)*(1+randomizedFactor));
            }
            shard.recalculateModule();
        }
    }


    public static ArrayList<Node> IncrementalSalp(){
        ArrayList<Node> listOfNodesBySalp = salp();


        double prevUBL = calculateUBL(listOfNodesBySalp, calculateNormalizedVector(originalShards));
        //petla

        ArrayList<Shard> newShards = (ArrayList<Shard>) originalShards.stream().map(Shard::new).collect(Collectors.toList());

        randomizeShardsLoad(newShards,10);

        ArrayList<Double> normalizedVector = calculateNormalizedVector(newShards);



        ArrayList<Node> newListOfNodes = new ArrayList<>();
        for (Node node : listOfNodesBySalp){
            int id = node.getNo();
            Node newNode = new Node(id,numberOfTimestamps);
            ArrayList<Integer> listIfShardIds = new ArrayList<>();
            for (Shard shard : node.getListOfShard()){
                listIfShardIds.add(shard.no);
            }
            for(int shardId : listIfShardIds){
                for(Shard newShard : newShards)
                {
                    if(newShard.no == shardId){
                        newNode.getListOfShard().add(newShard);
                    }
                }
            }
            newNode.recalculateLoad();
            newNode.setUnbalancedVector(subVectors(newNode.getListOfLoad(),normalizedVector));
            newListOfNodes.add(newNode);
        }

        double newUbl = calculateUBL(newListOfNodes, normalizedVector);
        if(newUbl > prevUBL){
            //realocateShards()
        }
        prevUBL = newUbl;


        System.out.println("kaszojad");
        return null;
    }

    private static Double calculateUBL(ArrayList<Node> listOfNodes, ArrayList<Double> normalizedVector){
        double sumOfModules = 0.0;
        for(Node node : listOfNodes){
             sumOfModules += calculateModule(node.getUnbalancedVector());
         }

        double sumOfLoads = 0.0;
        for(Double load : normalizedVector){
            sumOfLoads += load*numberOfNodes;
        }
        return  sumOfModules/sumOfLoads;
    }

    private static ArrayList<Double> calculateNormalizedVector(ArrayList<Shard> input) {
        ArrayList<Double> sumOfVectors = new ArrayList<>();
        for (int i = 0;i<numberOfShards; i++){
            ArrayList<Double> currentShard = input.get(i).getVector();
            for (int j = 0;j<numberOfTimestamps; j++){

                if(i==0){
                    sumOfVectors.add(currentShard.get(j));
                }
                else{
                    sumOfVectors.set(j,sumOfVectors.get(j)+currentShard.get(j));
                }

            }
        }

        ArrayList<Double> sumOfVectorsNorm = new ArrayList<>();
        for (int i = 0;i<numberOfTimestamps; i++){
            sumOfVectorsNorm.add(sumOfVectors.get(i)/numberOfNodes);
        }
        return sumOfVectorsNorm;
    }

    public static ArrayList<Node> salp(){

        originalShards = loadFile();


        numberOfTimestamps = originalShards.get(0).getVectorSize();
        numberOfShards = originalShards.size();


        ArrayList<Double> sumOfVectorsNormalized = calculateNormalizedVector(originalShards);

        originalShards.sort((lhs, rhs) -> Integer.compare(rhs.getModule(), lhs.getModule()));

        ArrayList<Node> listOfNodes = new ArrayList<>();
        for(int i=0;i<numberOfNodes; i++){
            listOfNodes.add(new Node(i,numberOfTimestamps));
        }

        for (Shard shard: originalShards) {
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
            listOfNodes.get(bestNode).recalculateLoad();
            if(calculateModule(listOfNodes.get(bestNode).getListOfLoad()) > calculateModule(sumOfVectorsNormalized)){
                listOfNodes.get(bestNode).setIsActive(false);
            }
        }

        for(Node node : listOfNodes){
            node.setUnbalancedVector(subVectors(node.getListOfLoad(),sumOfVectorsNormalized));
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

