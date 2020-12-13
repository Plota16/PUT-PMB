package com.company;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import static java.lang.Math.*;


public class Main {

    public static final String OUTPUT_TXT = "output2.txt";
    static ArrayList<Shard> originalShards = new ArrayList<>();
    static ArrayList<Shard> originalShards2 = new ArrayList<>();
    static int numberOfNodes;
    static int numberOfShards;
    static int numberOfLoops;
    static int randomPercent;
    static int numberOfTimestamps;
    static int normalDistributionMean;
    static int normalDistributionSd;
    static double power;
    static double taskFrequencyFactor;
    static String filepath;

    //todo: delete2
    static int numberOfReallocations;
    static int numberOfReallocationsOld;

    /*
    arg1 - input file path
    arg2 - number of nodes
    arg3 - number of loops
    arg4 - max input load percent change per cycle
    arg5 - number of shards
    arg6 - number of timestamps
    arg7 - mean for normal distribution
    arg8 - sd for normal distribution
    arg9 - power
    arg10 - task frequency factor
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
            power = Double.parseDouble(args[8]);
            taskFrequencyFactor = Double.parseDouble(args[9]);

            System.out.println(
                    "\nścieżka: " + filepath +
                    "\nilość węzłów: " + numberOfNodes +
                    "\nilość shardów: " + numberOfShards +
                    "\nilośc przedziałów: " + numberOfTimestamps +
                    "\nilość pętli: " + numberOfLoops +
                    "\nprocent randomizacji: " + randomPercent +
                    "\nśrednia rozkładu: " + normalDistributionMean +
                    "\nsd rozkładu : " + normalDistributionSd +
                    "\nmoc : " + power +
                    "\nwspółczynnik częstości przedkładana zadań : " + taskFrequencyFactor + "\n");

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
        originalShards2 = loadFile(filepath);

        IncrementalSalp();
        System.out.println("end");
    }


    private static void IncrementalSalp(){

        ArrayList<Node> previousCycleNodes = salp(originalShards);
        ArrayList<Node> salpNodes = salp(originalShards);

        //todo: delete
        double sumSalp = 0.0;
        double sumIncremental = 0.0;
        int salpBetter = 0;
        int incrementalBetter = 0;
        int equal = 0;
        double salpUbl = 0;
        double newUbl2 = 0;
        double newListOfNodesEstimatedLatency = 0;
        double salpNodesEstimatedLatency = 0;

        for (int i = 0; i < numberOfLoops; i++) {

            ArrayList<Double> sumVector = calculateSumVector(originalShards);
            double prevUBL = calculateUBL(previousCycleNodes, sumVector);

            randomizeShardsLoad(originalShards,randomPercent);

            ArrayList<Double> normalizedVector = calculateNormalizedVector(originalShards);
            sumVector = calculateSumVector(originalShards);

            for (int j = 0; j < numberOfNodes; j++) {
                salpNodes.get(j).recalculateLoad();
                salpNodes.get(j).setUnbalancedVector(subVectors(salpNodes.get(j).getListOfLoad(), normalizedVector));
                previousCycleNodes.get(j).recalculateLoad();
                previousCycleNodes.get(j).setUnbalancedVector(subVectors(previousCycleNodes.get(j).getListOfLoad(), normalizedVector));
            }

            //todo: delete
            salpUbl = calculateUBL(salpNodes, sumVector);

            ArrayList<Node> newListOfNodes = previousCycleNodes;

            double newUbl = calculateUBL(newListOfNodes, sumVector);

            if(newUbl > prevUBL) {
                ArrayList<Integer> nodesToRealocate = findNodesToReallocate(newListOfNodes);
                reallocateShards(nodesToRealocate, newListOfNodes, normalizedVector, sumVector);
            }

            newUbl2 = calculateUBL(newListOfNodes, sumVector);

//            CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(numberOfLoops), false);
//            CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(randomPercent), false);
//            CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(numberOfShards), false);
//            CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(numberOfNodes), false);
//            CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(salpUbl), false);
//            CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(newUbl2), false);
//            CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(sumSalp), false);
//            CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(sumIncremental), false);
//            CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(numberOfReallocationsOld != numberOfReallocations), true);

            numberOfReallocationsOld = numberOfReallocations;

            if (newUbl2 < salpUbl) {
                incrementalBetter++;
            }
            else if (salpUbl < newUbl2) {
                salpBetter++;
            }
            else {
                equal++;
            }

            //todo: delete
            sumSalp += salpUbl;
            sumIncremental += newUbl2;

            newListOfNodesEstimatedLatency += estimateLatency(newListOfNodes);
            salpNodesEstimatedLatency += estimateLatency(salpNodes);

            previousCycleNodes = newListOfNodes;
        }

        //todo: delete
        System.out.println("Sum of SALP: " + sumSalp + "\nSum of Incremental: " + sumIncremental);
        System.out.println("SALP better: " + salpBetter + "\nIncremental better: " + incrementalBetter + "\nEqual: " + equal);
        System.out.println("Number of reallocations: " + numberOfReallocations);

        CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(newListOfNodesEstimatedLatency), false);
        CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(salpNodesEstimatedLatency), true);

//        CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(numberOfLoops), false);
//        CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(randomPercent), false);
//        CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(numberOfShards), false);
//        CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(numberOfNodes), false);
//        CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(sumSalp), false);
//        CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(sumIncremental), false);
//        CreateCSV.saveToCsv(OUTPUT_TXT, String.valueOf(numberOfReallocations), true);

    }

    private static double estimateLatency(ArrayList<Node> nodes) {
        return nodes.stream()
                .map(n -> n.estimateNodeLatency(power, taskFrequencyFactor, numberOfTimestamps))
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private static void randomizeShardsLoad(ArrayList<Shard> input, int randomPercent){
        Random generator = new Random();
        for (Shard shard : input){

            Shard shardToProcess = null;

            for (Shard shard1 : originalShards2) {
                if (shard1.getNo() == shard.getNo()) {
                    shardToProcess = shard1;
                }
            }

            for (int i=0; i < shard.getVector().size(); i++){
                double randomizedFactor = (generator.nextInt(randomPercent) - (randomPercent/2.0)) / 100.0;
                shard.getVector().set(i, shardToProcess.getVector().get(i) * (1 + randomizedFactor));
            }
            shard.recalculateModule();
        }
    }

    private static void reallocateShards(ArrayList<Integer> nodeIDs, ArrayList<Node> nodes, ArrayList<Double> normalizedVector, ArrayList<Double> sumVector) {

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

            double currentUbl = calculateUBL(nodes, sumVector);

            Shard shard1 = node1.getMostUnbalancedShard();
            Shard shard2 = node2.getMostUnbalancedShard();

            if (shard1 != null || shard2 != null) {
                double diff1 = 0.0;
                double diff2 = 0.0;
                double diff3 = 0.0;

                if (shard1 != null) {
                    transferShard(shard1, node1, node2, normalizedVector);
                    diff1 = currentUbl - calculateUBL(nodes, sumVector);
                    transferShard(shard1, node2, node1, normalizedVector);
                }

                if (shard2 != null) {
                    transferShard(shard2, node2, node1, normalizedVector);
                    diff2 = currentUbl - calculateUBL(nodes, sumVector);
                    transferShard(shard2, node1, node2, normalizedVector);
                }

                if (diff1 >= diff2 && diff1 > 0) {
                    transferShard(shard1, node1, node2, normalizedVector);
                    shard1.setActive(false);
                    numberOfReallocations++;
                }
                else if (diff1 < diff2 && diff2 > 0) {
                    transferShard(shard2, node2, node1, normalizedVector);
                    shard2.setActive(false);
                    numberOfReallocations++;
                }
                else {
                    transferShard(shard1, node1, node2, normalizedVector);
                    transferShard(shard2, node2, node1, normalizedVector);
                    diff3 = currentUbl - calculateUBL(nodes, sumVector);
                    if (diff3 < 0) {
                        transferShard(shard1, node2, node1, normalizedVector);
                        transferShard(shard2, node1, node2, normalizedVector);
                    }
                    else {
                        numberOfReallocations++;
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
            sumOfVectorsNorm.add(Round_off(sumOfVectors.get(i)/numberOfNodes, 10));
        }
        return sumOfVectorsNorm;
    }

    private static ArrayList<Double> calculateSumVector(ArrayList<Shard> input) {
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
            sumOfVectorsNorm.add(sumOfVectors.get(i));
        }
        return sumOfVectorsNorm;
    }

    private static ArrayList<Node> salp(ArrayList<Shard> shards){

        int numberOfTimeSlices = shards.get(0).getVectorSize();

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
                    double moduleA = calculateModule(subVectors(node.getListOfLoad(),sumOfVectorsNormalized));
                    double moduleB = calculateModule(subVectors(addVectors(node.getListOfLoad(),shard.getVector()),sumOfVectorsNormalized));

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

    private static double calculateUBL(ArrayList<Node> listOfNodes, ArrayList<Double> loadVector){
        double sumOfModules = 0.0;
        for(Node node : listOfNodes){
            sumOfModules += calculateModule(node.getUnbalancedVector());
        }

        double sumOfLoads = 0.0;
        for(double load : loadVector){
            sumOfLoads += load;
        }

        return Round_off(sumOfModules/sumOfLoads, 10);
    }

    //package private

    static double calculateModule(List<Double> list){
        double currentValue = 0.0;
        for (double element: list ) {
            currentValue += Math.abs(element);
        }
        return currentValue;
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

    static double Round_off(double N, double n) {
        int h;
        double b, d, e, i, j, m, f;
        b = N;

        // Counting the no. of digits to the left of decimal point
        // in the given no.
        for (i = 0; b >= 1; ++i)
            b = b / 10;

        d = n - i;
        b = N;
        b = b * pow(10, d);
        e = b + 0.5;
        if ((float) e == (float) ceil(b)) {
            f = (ceil(b));
            h = (int) (f - 2);
            if (h % 2 != 0) {
                e = e - 1;
            }
        }
        j = floor(e);
        m = pow(10, d);
        j = j / m;
        return j;
    }
}

