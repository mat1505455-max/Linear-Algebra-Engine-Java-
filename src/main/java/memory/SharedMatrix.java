package memory;

public class SharedMatrix {

    private volatile SharedVector[] vectors = {}; // underlying vectors

    public SharedMatrix() {
        this.vectors = new SharedVector[0];
    }

    public SharedMatrix(double[][] matrix) {
        loadRowMajor(matrix);
    }

    public void loadRowMajor(double[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            this.vectors = new SharedVector[0];
            return;
        }
        int expectedLength = matrix[0].length;
        SharedVector[] temp = new SharedVector[matrix.length];
        for(int i = 0; i < matrix.length; i++){
            if (matrix[i].length != expectedLength) {
            throw new IllegalArgumentException("Inconsistent row length at index " + i + 
                "All rows must have the same length");
            }
            temp[i] = new SharedVector(matrix[i], VectorOrientation.ROW_MAJOR);
        }
        this.vectors = temp;
    }

    public void loadColumnMajor(double[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            this.vectors = new SharedVector[0];
            return;
        }
        int expectedLength = matrix[0].length;
        int colNum = matrix.length;
        SharedVector[] temp = new SharedVector[colNum];
        for(int i = 0; i < colNum; i++){
            if (matrix[i].length != expectedLength) {
            throw new IllegalArgumentException("Inconsistent column length at index " + i);
        }
            temp[i] = new SharedVector(matrix[i], VectorOrientation.COLUMN_MAJOR);
        }
        this.vectors = temp;
    }

    public double[][] readRowMajor() {
        if(vectors == null || vectors.length == 0){
            return new double[0][0];
        }
        acquireAllVectorReadLocks(this.vectors);
        try{
            if(getOrientation() == VectorOrientation.ROW_MAJOR){
                int num = this.vectors[0].length();
                double[][] res = new double[vectors.length][num];
                for(int i = 0; i < res.length; i++){
                    SharedVector temp = vectors[i];
                    for(int j = 0; j < num; j++){
                        res[i][j]= temp.get(j);
                    }
                }
                return res;
            }
            int num = this.vectors[0].length();
            double[][] res = new double[num][vectors.length];
            for(int i = 0; i < res.length; i++){
                for(int j = 0; j < res[i].length; j++){
                    SharedVector temp = vectors[j];
                    res[i][j]= temp.get(i);
                }
            }
            return res;
            
        }finally{
            releaseAllVectorReadLocks(vectors);
        }
        
    }
    

    public SharedVector get(int index) {
        if(index < 0 || index >= vectors.length){
            throw new IndexOutOfBoundsException("Index" + index+ "out of bounds");
        }
        return vectors[index];
    }

    public int length() {
        return this.vectors.length;
    }

    public VectorOrientation getOrientation() {
        if(vectors == null || vectors.length == 0){
           throw new IllegalArgumentException("matrix is empty");
        }
        SharedVector first = vectors[0];
        first.readLock();
        try{
            return first.getOrientation();
        }finally{
            first.readUnlock();
        }
    }

    private void acquireAllVectorReadLocks(SharedVector[] vecs) {
        for (int i = 0; i < vecs.length; i++) {
            if (vecs[i] != null) {
                vecs[i].readLock();
            }
        }
    }

    private void releaseAllVectorReadLocks(SharedVector[] vecs) {
        for (int i = vecs.length - 1; i >= 0; i--) {
            if (vecs[i] != null) {
                vecs[i].readUnlock();
            }
        }
    }

    private void acquireAllVectorWriteLocks(SharedVector[] vecs) {
        for (int i = 0; i < vecs.length; i++) {
            if (vecs[i] != null) {
                vecs[i].writeLock();
            }
        }
    }

    private void releaseAllVectorWriteLocks(SharedVector[] vecs) {
        for (int i = vecs.length - 1; i >= 0; i--) {
            if (vecs[i] != null) {
                vecs[i].writeUnlock();
            }
        }
    }
}
