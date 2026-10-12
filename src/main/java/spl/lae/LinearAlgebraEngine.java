package spl.lae;

import parser.*;
import memory.*;
import scheduling.*;

import java.util.ArrayList;
import java.util.List;

public class LinearAlgebraEngine {

    private SharedMatrix leftMatrix = new SharedMatrix();
    private SharedMatrix rightMatrix = new SharedMatrix();
    private TiredExecutor executor;

    public LinearAlgebraEngine(int numThreads) {
        this.executor = new TiredExecutor(numThreads);
    }

    public ComputationNode run(ComputationNode computationRoot) {
        computationRoot.associativeNesting();
        ComputationNode node = computationRoot.findResolvable();
        while(node!=null){
            loadAndCompute(node);
            node = computationRoot.findResolvable();
        }
        return computationRoot;
    }

    public void loadAndCompute(ComputationNode node) {
        List<Runnable> tasks = null;
        List<ComputationNode> children = node.getChildren();
        leftMatrix.loadRowMajor(children.get(0).getMatrix());

        if(children.size()>1){
            if(node.getNodeType()== ComputationNodeType.MULTIPLY){
                rightMatrix.loadRowMajor(children.get(1).getMatrix());
            } else{
                rightMatrix.loadRowMajor(children.get(1).getMatrix());
            }
        }
        if(node.getNodeType()==ComputationNodeType.ADD){
            tasks = createAddTasks();
        }   else if(node.getNodeType()==ComputationNodeType.MULTIPLY){
            tasks = createMultiplyTasks();
        } else if(node.getNodeType()==ComputationNodeType.NEGATE){
            tasks = createNegateTasks();
        } else if(node.getNodeType()==ComputationNodeType.TRANSPOSE){
            tasks = createTransposeTasks();
        }
        if(tasks!=null){
            executor.submitAll(tasks);
        }
        node.resolve(leftMatrix.readRowMajor());    
    }   

    public List<Runnable> createAddTasks() {
        List<Runnable> tasks = new ArrayList<>();
        int numRows = leftMatrix.length();

        for(int i = 0; i< numRows; i++){
            final int rowIndex = i;
            tasks.add(() ->{
                SharedVector rowLeft = leftMatrix.get(rowIndex);
                SharedVector rowRight = rightMatrix.get(rowIndex);
                rowLeft.add(rowRight);
            });
        }
        return tasks;
    }


    public List<Runnable> createMultiplyTasks() {
        List<Runnable> tasks = new ArrayList<>();
        int numRows = leftMatrix.length();

        for(int i = 0; i< numRows; i++){
            final int rowIndex = i;
            tasks.add(() -> {
                        SharedVector row = leftMatrix.get(rowIndex);
                        row.vecMatMul(rightMatrix);
                    });
        }
        return tasks;
    }

    public List<Runnable> createNegateTasks() {
        List<Runnable> tasks = new ArrayList<>();
        int numRows = leftMatrix.length();

        for(int i = 0; i< numRows; i++){
            final int rowIndex = i;
            tasks.add(() ->{
                leftMatrix.get(rowIndex).negate();
            });
        }
        return tasks;
    }

    public List<Runnable> createTransposeTasks() {
        List<Runnable> tasks = new ArrayList<>();
        int numVectors = leftMatrix.length(); 

        for (int i = 0; i < numVectors; i++) {
            final int index = i;
            tasks.add(() -> {
                leftMatrix.get(index).transpose();
            });
        }
        return tasks;
    }

    public String getWorkerReport() {
        return executor.getWorkerReport();
    }
    public void shutdown() throws InterruptedException {
    executor.shutdown();
    }
}
