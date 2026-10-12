package spl.lae;

import parser.ComputationNode;
import parser.InputParser;
import parser.OutputWriter;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java -jar target/lga-1.0.jar <num_threads> <input_file> <output_file>");
            return;
        }

        int numThreads = 0;
        String inputFile = args[1];
        String outputFile = args[2];

        try {
            numThreads = Integer.parseInt(args[0]);
            if (numThreads <= 0) {
                throw new NumberFormatException("Threads must be positive");
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Number of threads must be a positive integer.");
            return;
        }

        System.out.println("LAE Initialized: Threads=" + numThreads + ", Input=" + inputFile);
        
        LinearAlgebraEngine engine = new LinearAlgebraEngine(numThreads);
        InputParser parser = new InputParser();

        try {
            ComputationNode rootNode = parser.parse(inputFile);

            long startTime = System.currentTimeMillis();
            ComputationNode resultNode = engine.run(rootNode);
            long endTime = System.currentTimeMillis();

            System.out.println("Computation finished in " + (endTime - startTime) + "ms.");
            
            System.out.println(engine.getWorkerReport());

            OutputWriter.write(resultNode.getMatrix(), outputFile);
            System.out.println("Result saved to " + outputFile);

        } catch (Exception e) {
            System.err.println("An error occurred during execution: " + e.getMessage());
            e.printStackTrace();
            try {
                OutputWriter.write(e.getMessage(), outputFile);
            } catch (IOException ioException) {
                System.err.println("Failed to write error to output file.");
            }
        } finally {
            try {
                engine.shutdown();
            } catch (Exception e) {
                System.err.println("Error shutting down executor: " + e.getMessage());
            }
        }
    }
}