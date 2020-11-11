package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class Main {

    static ArrayList<Shard> originalShards = new ArrayList<>();
    static int numberOfNodes;
    static int numberOfShards;
    static int numberOfLoops;
    static int randomPercent;
    static int numberOfTimestamps;
    static int normalDistributionMean;
    static int normalDistributionSd;
    static String filepath;

    /*
    arg1 - input file path
    arg2 - number of nodes
    arg3 - number of loops
    arg4 - max input load percent change per cycle
    arg5 - number of shards
    arg6 - number of timestamps
    arg7 - mean for normal distribution
    arg8 - sd for normal distribution
     */

    public static void main(String[] args) throws IOException {

        filepath = args[0];
        numberOfNodes = Integer.parseInt(args[1]);
        numberOfLoops = Integer.parseInt(args[2]);
        randomPercent = Integer.parseInt(args[3]);

        if(args.length >= 8){
            numberOfShards = Integer.parseInt(args[4]);
            numberOfTimestamps = Integer.parseInt(args[5]);
            normalDistributionMean = Integer.parseInt(args[6]);
            normalDistributionSd = Integer.parseInt(args[7]);

            System.out.println(
                    "\nścieżka: " + filepath +
                    "\nilość węzłów: " + numberOfNodes +
                    "\nilość shardów: " + numberOfShards +
                    "\nilośc przedziałów: " + numberOfTimestamps +
                    "\nilość pętli: " + numberOfLoops +
                    "\nprocent randomizacji: " + randomPercent +
                    "\nśrednia rozkładu: " + normalDistributionMean +
                    "\nsd rozkładu : " + normalDistributionSd + "\n");

            VectorGenerator vectorGenerator = new VectorGenerator(
                    numberOfShards,
                    numberOfTimestamps,
                    normalDistributionMean,
                    normalDistributionSd,
                    filepath);

            vectorGenerator.generate();
        }
        else {
            System.out.println(
                    "\nścieżka: " + filepath +
                    "\nilość węzłów: " + numberOfNodes +
                    "\nilość pętli: " + numberOfLoops +
                    "\nprocent randomizacji: " + randomPercent + "\n");

        }






        originalShards = loadFile(filepath);

        IncrementalSalp();
        System.out.println("end");
    }


    private static void IncrementalSalp(){

        int numberOfTimeSlices = originalShards.get(0).getVectorSize();

        ArrayList<Node> previousCycleNodes = salp(originalShards);
        ArrayList<Shard> newShards = (ArrayList<Shard>) originalShards.stream().map(Shard::new).collect(Collectors.toList());

        //todo: delete
        double sumSalp = 0.0;
        double sumIncremental = 0.0;
        int salpBetter = 0;
        int incrementalBetter = 0;

        for (int i = 0; i < numberOfLoops; i++) {

            randomizeShardsLoad(newShards,randomPercent);
            ArrayList<Double> normalizedVector = calculateNormalizedVector(newShards);

            //todo: delete
            double salpUbl = calculateUBL(salp(newShards), normalizedVector);

            double prevUBL = calculateUBL(previousCycleNodes, normalizedVector);

            ArrayList<Node> newListOfNodes = new ArrayList<>();
            for (Node node : previousCycleNodes){
                Node newNode = new Node(node.getNo(), numberOfTimeSlices);
                ArrayList<Integer> listOfShardIDs = new ArrayList<>();
                for (Shard shard : node.getListOfShard()){
                    listOfShardIDs.add(shard.getNo());
                }
                for(int shardId : listOfShardIDs){
                    for(Shard newShard : newShards)
                    {
                        if(newShard.getNo() == shardId){
                            newNode.getListOfShard().add(newShard);
                        }
                    }
                }
                newNode.recalculateLoad();
                newNode.setUnbalancedVector(subVectors(newNode.getListOfLoad(),normalizedVector));
                newNode.setAllShardsActive();
                newListOfNodes.add(newNode);
            }

            double newUbl = calculateUBL(newListOfNodes, normalizedVector);
            if(newUbl > prevUBL) {
                ArrayList<Integer> nodesToRealocate = findNodesToReallocate(newListOfNodes);
                reallocateShards(nodesToRealocate, newListOfNodes, normalizedVector, newUbl);
            }
            double newUbl2 = calculateUBL(newListOfNodes, normalizedVector);

            if (newUbl2 < salpUbl) {
                incrementalBetter++;
            }
            else if (salpUbl < newUbl2) {
                salpBetter++;
            }

            //todo: delete
            sumSalp += salpUbl;
            sumIncremental += newUbl2;

            previousCycleNodes = newListOfNodes;
        }

        //todo: delete
        System.out.println("Sum of SALP: " + sumSalp + "\nSum of Incremental: " + sumIncremental);
        System.out.println("SALP better: " + salpBetter + "\nIncremental better: " + incrementalBetter);
    }

    private static void randomizeShardsLoad(ArrayList<Shard> input, int randomPercent){
        Random generator = new Random();
        for (Shard shard : input){
            for (int i=0;i<shard.getVector().size();i++){
                double randomizedFactor = (generator.nextInt(randomPercent) - (randomPercent/2.0)) / 100.0;
                shard.getVector().set(i,shard.getVector().get(i)*(1+randomizedFactor));
            }
            shard.recalculateModule();
        }
    }

    private static void reallocateShards(ArrayList<Integer> nodeIDs, ArrayList<Node> nodes, ArrayList<Double> normalizedVector, Double currentUbl) {

        Node node1 = null;
        Node node2 = null;

        for (Node node : nodes) {
            if (nodeIDs.get(0) == node.getNo()) {
                node1 = node;
            }
            else if (nodeIDs.get(1) == node.getNo()) {
                node2 = node;
            }
        }

        while(node1.hasActiveShard() || node2.hasActiveShard()) {

            Shard shard1 = node1.getMostUnbalancedShard();
            Shard shard2 = node2.getMostUnbalancedShard();

            if (shard1 != null || shard2 != null) {
                double diff1 = 0.0;
                double diff2 = 0.0;
                double diff3 = 0.0;

                if (shard1 != null) {
                    transferShard(shard1, node1, node2, normalizedVector);
                    diff1 = currentUbl - calculateUBL(nodes, normalizedVector);
                    transferShard(shard1, node2, node1, normalizedVector);
                }

                if (shard2 != null) {
                    transferShard(shard2, node2, node1, normalizedVector);
                    diff2 = currentUbl - calculateUBL(nodes, normalizedVector);
                    transferShard(shard2, node1, node2, normalizedVector);
                }

                if (diff1 >= diff2 && diff1 > 0) {
                    transferShard(shard1, node1, node2, normalizedVector);
                    shard1.setActive(false);
                }
                else if (diff1 < diff2 && diff2 > 0) {
                    transferShard(shard2, node2, node1, normalizedVector);
                    shard2.setActive(false);
                }
                else {
                    transferShard(shard1, node1, node2, normalizedVector);
                    transferShard(shard2, node2, node1, normalizedVector);
                    diff3 = currentUbl - calculateUBL(nodes, normalizedVector);
                    if (diff3 < 0) {
                        transferShard(shard1, node2, node1, normalizedVector);
                        transferShard(shard2, node1, node2, normalizedVector);
                    }
                    if (shard1 != null) {
                        shard1.setActive(false);
                    }
                    if (shard2 != null) {
                        shard2.setActive(false);
                    }
                }
            }
        }

    }

    private static void transferShard(Shard shard, Node nodeOut, Node nodeIn, ArrayList<Double> normalizedVector) {
        if (shard != null) {
            nodeOut.getListOfShard().remove(shard);
            nodeIn.getListOfShard().add(shard);
            nodeOut.recalculateLoad();
            nodeOut.setUnbalancedVector(subVectors(nodeOut.getListOfLoad(), normalizedVector));
            nodeIn.recalculateLoad();
            nodeIn.setUnbalancedVector(subVectors(nodeIn.getListOfLoad(), normalizedVector));
        }
    }

    private static ArrayList<Integer> findNodesToReallocate(ArrayList<Node> nodes) {

        int firstNodeId = -1;
        int secondNodeId = -1;
        double unbalancedModule;
        double maxUnbalancedModule = 0.0;

        for (Node node : nodes) {
            unbalancedModule =calculateModule(node.getUnbalancedVector());
            if (unbalancedModule > maxUnbalancedModule) {
                maxUnbalancedModule = unbalancedModule;
                firstNodeId = node.getNo();
            }
        }

        double minModuleDifference = Double.MAX_VALUE;
        for (Node node : nodes) {
            if (node.getNo() != firstNodeId) {
                unbalancedModule = calculateModule(subVectors(nodes.get(firstNodeId).getUnbalancedVector(), node.getUnbalancedVector()));
                if (unbalancedModule < minModuleDifference) {
                    minModuleDifference = unbalancedModule;
                    secondNodeId = node.getNo();
                }
            }
        }

        ArrayList<Integer> result = new ArrayList<>();
        result.add(firstNodeId);
        result.add(secondNodeId);
        return result;

    }

    private static ArrayList<Double> calculateNormalizedVector(ArrayList<Shard> input) {
        ArrayList<Double> sumOfVectors = new ArrayList<>();
        for (int i = 0; i<input.size(); i++){
            ArrayList<Double> currentShard = input.get(i).getVector();
            for (int j = 0; j < input.get(0).getVectorSize(); j++){
                if(i==0){
                    sumOfVectors.add(currentShard.get(j));
                }
                else{
                    sumOfVectors.set(j,sumOfVectors.get(j)+currentShard.get(j));
                }

            }
        }

        ArrayList<Double> sumOfVectorsNorm = new ArrayList<>();
        for (int i = 0; i< input.get(0).getVectorSize(); i++){
            sumOfVectorsNorm.add(sumOfVectors.get(i)/numberOfNodes);
        }
        return sumOfVectorsNorm;
    }

    private static ArrayList<Node> salp(ArrayList<Shard> inputShards){

        int numberOfTimeSlices = inputShards.get(0).getVectorSize();
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

    private static ArrayList<Shard> loadFile(String path){
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

    //package private

    static Double calculateModule(List<Double> list){
        double currentValue = 0.0;
        for (Double element: list ) {
            currentValue += element*element;
        }
        return Math.sqrt(currentValue);
    }

    static ArrayList<Double> addVectors(ArrayList<Double> a, ArrayList<Double> b){
        ArrayList<Double> result = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            result.add(a.get(i) + b.get(i));
        }
        return result;
    }

    static ArrayList<Double> subVectors(ArrayList<Double> a, ArrayList<Double> b){
        ArrayList<Double> result = new ArrayList<>();
        for (int i = 0; i < a.size(); i++) {
            result.add(a.get(i) - b.get(i));
        }
        return result;
    }

}

