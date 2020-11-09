package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class Main {

    static ArrayList<Shard> originalShards = new ArrayList<>();
    static int numberOfNodes;
    static int numberOfTimeSlices;
    static int numberOfShards;

    /*
    arg1 - input file path
    arg2 - number of nodes
     */
    public static void main(String[] args) {

        originalShards = loadFile(args[0]);
        numberOfNodes = Integer.parseInt(args[1]);
        numberOfTimeSlices = originalShards.get(0).getVectorSize();
        numberOfShards = originalShards.size();

        ArrayList<Node> listOfNodesBySalp = salp(originalShards);
        ArrayList<Node> listOfNodesByIncrementalSalp = IncrementalSalp(originalShards);
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


    public static ArrayList<Node> IncrementalSalp(ArrayList<Shard> inputShards){
        ArrayList<Node> previousCycleNodes = salp(inputShards);

        double prevUBL = calculateUBL(previousCycleNodes, calculateNormalizedVector(inputShards));
        //petla

        ArrayList<Shard> newShards = (ArrayList<Shard>) inputShards.stream().map(Shard::new).collect(Collectors.toList());

        randomizeShardsLoad(newShards,10);

        ArrayList<Double> normalizedVector = calculateNormalizedVector(newShards);

        ArrayList<Node> newListOfNodes = new ArrayList<>();
        for (Node node : previousCycleNodes){
            Node newNode = new Node(node.getNo(), numberOfTimeSlices);
            ArrayList<Integer> listOfShardIDs = new ArrayList<>();
            for (Shard shard : node.getListOfShard()){
                listOfShardIDs.add(shard.no);
            }
            for(int shardId : listOfShardIDs){
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
            System.out.println("realokacja");
            //realocateShards();
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
        for (int i = 0; i<input.size(); i++){
            ArrayList<Double> currentShard = input.get(i).getVector();
            for (int j = 0; j < numberOfTimeSlices; j++){

                if(i==0){
                    sumOfVectors.add(currentShard.get(j));
                }
                else{
                    sumOfVectors.set(j,sumOfVectors.get(j)+currentShard.get(j));
                }

            }
        }

        ArrayList<Double> sumOfVectorsNorm = new ArrayList<>();
        for (int i = 0; i< numberOfTimeSlices; i++){
            sumOfVectorsNorm.add(sumOfVectors.get(i)/numberOfNodes);
        }
        return sumOfVectorsNorm;
    }

    public static ArrayList<Node> salp(ArrayList<Shard> inputShards){

        ArrayList<Shard> shards = (ArrayList<Shard>) inputShards.stream().map(Shard::new).collect(Collectors.toList());

        ArrayList<Double> sumOfVectorsNormalized = calculateNormalizedVector(shards);

        shards.sort((lhs, rhs) -> Integer.compare(rhs.getModule(), lhs.getModule()));

        ArrayList<Node> resultNodes = new ArrayList<>();
        for(int i=0; i<numberOfNodes; i++){
            resultNodes.add(new Node(i, numberOfTimeSlices));
        }

        for (Shard shard: shards) {
            double result = 0.0;
            int bestNode = 0;
            for (Node node: resultNodes) {
                if(node.getIsActive()){
                    Double moduleA = calculateModule(subVectors(node.getListOfLoad(),sumOfVectorsNormalized));
                    Double moduleB = calculateModule(subVectors(addVectors(node.getListOfLoad(),shard.getVector()),sumOfVectorsNormalized));

                    if((moduleA - moduleB) > result){
                        result = moduleA - moduleB;
                        bestNode = resultNodes.indexOf(node);
                    }
                }
            }
            resultNodes.get(bestNode).getListOfShard().add(shard);
            resultNodes.get(bestNode).recalculateLoad();
            if(calculateModule(resultNodes.get(bestNode).getListOfLoad()) > calculateModule(sumOfVectorsNormalized)){
                resultNodes.get(bestNode).setIsActive(false);
            }
        }

        for(Node node : resultNodes){
            node.setUnbalancedVector(subVectors(node.getListOfLoad(),sumOfVectorsNormalized));
        }

        return resultNodes;
    }

    public static ArrayList<Shard> loadFile(String path){
        ArrayList<Shard> result = new ArrayList<>();
        int counter = 0;
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(path));
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

