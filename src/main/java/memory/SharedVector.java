package memory;

import java.io.EOFException;
import java.util.concurrent.locks.ReadWriteLock;

public class SharedVector {

    private double[] vector;
    private VectorOrientation orientation;
    private ReadWriteLock lock = new java.util.concurrent.locks.ReentrantReadWriteLock();

    public SharedVector(double[] vector, VectorOrientation orientation) {
        this.vector = vector.clone();
        this.orientation= orientation;
    }

    public double get(int index) {
        readLock();
        try {
            return vector[index];
        } finally {
            readUnlock();
        }
    }

    public int length() {
        return vector.length;
    }

    public VectorOrientation getOrientation() {
        readLock();
        try{
            return orientation;
        } finally{
            readUnlock();
        }
    }

    public void writeLock() {
        lock.writeLock().lock();
    }

    public void writeUnlock() {
        lock.writeLock().unlock();
    }

    public void readLock() {
        lock.readLock().lock();
    }

    public void readUnlock() {
        lock.readLock().unlock();
    }

    public void transpose() {
        writeLock();
        try{
            if (this.orientation == VectorOrientation.COLUMN_MAJOR)
                this.orientation = VectorOrientation.ROW_MAJOR;
            else
                this.orientation = VectorOrientation.COLUMN_MAJOR;
        } finally{
            writeUnlock();
        }
    }

    public void add(SharedVector other) {
        if(System.identityHashCode(this) <= System.identityHashCode(other)){
            this.writeLock();
            other.readLock();
        }
        else{
            other.readLock();
            this.writeLock();
        }
        try{
        if(this.vector.length != other.vector.length){
            throw new IllegalArgumentException("Vectors must have the same size");
        }
        if(this.orientation != other.orientation){
            throw new IllegalArgumentException("Vectors must have the same orientation");
        }
        
        for(int i = 0; i < vector.length; i++)
        this.vector[i] = this.vector[i] + other.vector[i];
        } finally{
            this.writeUnlock();
            other.readUnlock();
        }
        
    }

    public void negate() {
        writeLock();
        try{
            for(int i = 0; i < vector.length; i++){
                vector[i] = -vector[i];
            }
        }finally{
            writeUnlock();
        }
    }

    public double dot(SharedVector other) {
        double ret = 0;
        if(System.identityHashCode(this) <= System.identityHashCode(other)){
            this.readLock();
            other.readLock();
        }
        else{
            other.readLock();
            this.readLock();
        }
        try{
            if(this.vector.length != other.vector.length){
                throw new IllegalArgumentException("Vectors must have the same size");
            }
            if(this.orientation == other.orientation){
                throw new IllegalArgumentException("Vectors can't have the same orientation");
            }
            for(int i = 0; i < vector.length; i++){
                ret = ret + this.vector[i]*other.vector[i];
            }
        }finally{
            this.readUnlock();
            other.readUnlock();
        }
        return ret;
    }

    public void vecMatMul(SharedMatrix matrix) {
       this.writeLock();
       try{
            if(this.orientation != VectorOrientation.ROW_MAJOR){
                throw new IllegalStateException("Vector must be ROW_MAJOR for matrix multiplication");
            }
            int numRows = matrix.length();
            if(this.vector.length != numRows){
                throw new IllegalArgumentException("Vector length must match matrix number of rows");
            }
            int numCol = matrix.get(0).length();
            double[] res = new double[numCol];
            for (int i = 0; i < numRows; i++) {
                double scalar = this.vector[i];
                SharedVector row = matrix.get(i);
                row.readLock();
                try{
                    for (int j = 0; j < numCol; j++) {
                        res[j] = res[j] + scalar * row.get(j);
                    }
                }finally{
                    row.readUnlock();
                }
            }
            this.vector = res;

        } finally {
            this.writeUnlock();
        }
    }
}
