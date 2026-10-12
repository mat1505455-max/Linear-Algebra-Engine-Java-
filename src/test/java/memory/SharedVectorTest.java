package memory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SharedVectorTest {

    private double[] data;
    private SharedVector vector;

    @BeforeEach
    void setUp() {
        data = new double[]{1.0, 2.0, 3.0};
        vector = new SharedVector(data, VectorOrientation.ROW_MAJOR);
    }

    @Test
    @DisplayName("Constructor should clone the input array to prevent external modification")
    void testConstructorCloning() {
        data[0] = 100.0;
        assertNotEquals(100.0, vector.get(0), "Vector should encapsulate its data");
    }

    @Test
    @DisplayName("get and length should return correct values")
    void testGetAndLength() {
        assertEquals(3, vector.length());
        assertEquals(1.0, vector.get(0));
        assertEquals(2.0, vector.get(1));
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> vector.get(5));
    }

    @Test
    @DisplayName("transpose should toggle orientation")
    void testTranspose() {
        assertEquals(VectorOrientation.ROW_MAJOR, vector.getOrientation());
        vector.transpose();
        assertEquals(VectorOrientation.COLUMN_MAJOR, vector.getOrientation());
        vector.transpose();
        assertEquals(VectorOrientation.ROW_MAJOR, vector.getOrientation());
    }

    @Test
    @DisplayName("negate should multiply all elements by -1")
    void testNegate() {
        vector.negate();
        assertEquals(-1.0, vector.get(0));
        assertEquals(-2.0, vector.get(1));
        assertEquals(-3.0, vector.get(2));
    }

    @Test
    @DisplayName("add should correctly sum two vectors of same size and orientation")
    void testAddSuccess() {
        SharedVector other = new SharedVector(new double[]{10, 20, 30}, VectorOrientation.ROW_MAJOR);
        vector.add(other);
        
        assertEquals(11.0, vector.get(0));
        assertEquals(22.0, vector.get(1));
        assertEquals(33.0, vector.get(2));
    }

    @Test
    @DisplayName("add should throw exception on size or orientation mismatch")
    void testAddMismatches() {
        // Mismatch size
        SharedVector small = new SharedVector(new double[]{1.0}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> vector.add(small));

        // Mismatch orientation
        SharedVector colVector = new SharedVector(new double[]{1.0, 2.0, 3.0}, VectorOrientation.COLUMN_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> vector.add(colVector));
    }

    @Test
    @DisplayName("dot product should calculate correctly")
    void testDotProduct() {
        // For dot product, orientation must be different
        SharedVector other = new SharedVector(new double[]{4.0, 5.0, 6.0}, VectorOrientation.COLUMN_MAJOR);
        double result = vector.dot(other); // (1*4 + 2*5 + 3*6) = 4 + 10 + 18 = 32
        assertEquals(32.0, result);
    }

    @Test
    @DisplayName("dot product should throw exception if orientations are the same")
    void testDotProductSameOrientation() {
        SharedVector sameOrient = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.ROW_MAJOR);
        assertThrows(IllegalArgumentException.class, () -> vector.dot(sameOrient));
    }

    @Test
    @DisplayName("vecMatMul should correctly multiply a vector by a matrix")
    void testVecMatMulWithRealMatrix() {
        double[] vecData = {1.0, 2.0};
        SharedVector v = new SharedVector(vecData, VectorOrientation.ROW_MAJOR);
        double[][] matData = {
        {10.0, 20.0, 30.0},
        {40.0, 50.0, 60.0}
        };
        SharedMatrix m = new SharedMatrix(matData);
        v.vecMatMul(m);
         assertEquals(3, v.length(), "The result vector should have 3 elements (number of columns)");
        assertEquals(90.0, v.get(0), "Element 0 should be 90.0");
        assertEquals(120.0, v.get(1), "Element 1 should be 120.0");
        assertEquals(150.0, v.get(2), "Element 2 should be 150.0");
    }

    @Test
    @DisplayName("vecMatMul should throw IllegalArgumentException if sizes don't match")
    void testVecMatMulSizeMismatch() {
        SharedVector v = new SharedVector(new double[]{1, 2}, VectorOrientation.ROW_MAJOR);
        double[][] matData = { {1}, {2}, {3} };
        SharedMatrix m = new SharedMatrix(matData);
        assertThrows(IllegalArgumentException.class, () -> v.vecMatMul(m));
    }
    @Test
    @DisplayName("vecMatMul should throw IllegalStateException if vector is not ROW_MAJOR")
    void testVecMatMulOrientationMismatch() {
        
        SharedVector v = new SharedVector(new double[]{1, 2, 3}, VectorOrientation.COLUMN_MAJOR);
        SharedMatrix m = new SharedMatrix(new double[][]{{1, 2, 3}});
        
        
        assertThrows(IllegalStateException.class, () -> v.vecMatMul(m));
    }

    @Test
    @DisplayName("Should handle empty vectors without crashing")
    void testEmptyVector() {
        SharedVector empty = new SharedVector(new double[]{}, VectorOrientation.ROW_MAJOR);
        assertEquals(0, empty.length());
        assertDoesNotThrow(() -> empty.negate());
        assertDoesNotThrow(() -> empty.transpose());
    }

    @Test
    @DisplayName("add should handle negative numbers correctly")
    void testAddWithNegatives() {
        SharedVector v1 = new SharedVector(new double[]{5.0, -2.0}, VectorOrientation.ROW_MAJOR);
        SharedVector v2 = new SharedVector(new double[]{-10.0, 2.0}, VectorOrientation.ROW_MAJOR);
        v1.add(v2);
        assertEquals(-5.0, v1.get(0));
        assertEquals(0.0, v1.get(1));
    }

}