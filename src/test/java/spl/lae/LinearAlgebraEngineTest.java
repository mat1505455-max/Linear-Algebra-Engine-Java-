package spl.lae;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import parser.ComputationNode;
import parser.ComputationNodeType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinearAlgebraEngineTest {

    private LinearAlgebraEngine engine;
    private final int NUM_THREADS = 4;

    @BeforeEach
    void setUp() {
        engine = new LinearAlgebraEngine(NUM_THREADS);
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        engine.shutdown();
    }

   
    private ComputationNode createOpNode(ComputationNodeType type, ComputationNode... children) {
        return new ComputationNode(type, new ArrayList<>(Arrays.asList(children)));
    }

    @Test
    void testBasicAddition() {
        double[][] dataA = {{1.0, 2.0}, {3.0, 4.0}};
        double[][] dataB = {{10.0, 20.0}, {30.0, 40.0}};

        ComputationNode nodeA = new ComputationNode(dataA);
        ComputationNode nodeB = new ComputationNode(dataB);
        ComputationNode addNode = createOpNode(ComputationNodeType.ADD, nodeA, nodeB);

        engine.run(addNode);

        double[][] result = addNode.getMatrix();
        assertEquals(11.0, result[0][0]);
        assertEquals(22.0, result[0][1]);
        assertEquals(44.0, result[1][1]);
    }

    @Test
    void testNegate() {
        double[][] data = {{5.0, -10.0}, {0.0, 7.0}};
        ComputationNode node = new ComputationNode(data);
        ComputationNode negNode = createOpNode(ComputationNodeType.NEGATE, node);

        engine.run(negNode);

        double[][] result = negNode.getMatrix();
        assertEquals(-5.0, result[0][0]);
        assertEquals(10.0, result[0][1]);
        assertEquals(-7.0, result[1][1]);
    }

    @Test
    void testThreeWayAddition() {
        double[][] m = {{1, 1}, {1, 1}};
        ComputationNode n1 = new ComputationNode(m);
        ComputationNode n2 = new ComputationNode(m);
        ComputationNode n3 = new ComputationNode(m);

        ComputationNode root = createOpNode(ComputationNodeType.ADD, n1, n2, n3);

        engine.run(root);

        double[][] result = root.getMatrix();
      
        assertEquals(3.0, result[0][0]);
        assertEquals(3.0, result[1][1]);
    }

    @Test
    void testMultiplication() {
    
        double[][] identity = {{1, 0}, {0, 1}};
        double[][] data = {{5, 6}, {7, 8}};

        ComputationNode n1 = new ComputationNode(data);
        ComputationNode n2 = new ComputationNode(identity);
        ComputationNode mulNode = createOpNode(ComputationNodeType.MULTIPLY, n1, n2);

        engine.run(mulNode);

        double[][] result = mulNode.getMatrix();
        assertEquals(5.0, result[0][0]);
        assertEquals(6.0, result[0][1]);
        assertEquals(8.0, result[1][1]);
    }

  
    @Test
    void testAlreadyResolvedNode() {
        double[][] data = {{42}};
        ComputationNode matrixNode = new ComputationNode(data);

      
        ComputationNode resultNode = engine.run(matrixNode);

        assertEquals(42.0, resultNode.getMatrix()[0][0]);
    }

    
    @Test
    void testLoadStress() {
        int rows = 50;
        double[][] data = new double[rows][2];
        for(int i=0; i<rows; i++) {
            data[i][0] = i;
            data[i][1] = i;
        }

        ComputationNode n1 = new ComputationNode(data);
        ComputationNode n2 = new ComputationNode(data);
        ComputationNode root = createOpNode(ComputationNodeType.ADD, n1, n2);

        engine.run(root);

        double[][] result = root.getMatrix();
        assertEquals(0.0, result[0][0]);
        assertEquals(98.0, result[49][0]); // 49 + 49
    }

    @Test
    void testWorkerReport() {
        double[][] data = {{1, 1}, {1, 1}};
        ComputationNode root = createOpNode(ComputationNodeType.NEGATE, new ComputationNode(data));
        
        engine.run(root);
        String report = engine.getWorkerReport();
        
        assertNotNull(report);
        assertTrue(report.contains("Worker Report"));
    }
}