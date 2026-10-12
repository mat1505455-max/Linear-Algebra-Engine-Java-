package scheduling;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


import static org.junit.jupiter.api.Assertions.*;

class TiredThreadTest {

    private TiredThread thread;
    private final double FATIGUE_FACTOR = 2.0;

    @BeforeEach
    void setUp() {
        thread = new TiredThread(1, FATIGUE_FACTOR);
        thread.start();
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        thread.shutdown();
        thread.join(1000);
    }

    @Test
    void testInitialState() {
        assertEquals(1, thread.getWorkerId());
        assertEquals(0, thread.getFatigue(), 0.001, "Fatigue should be 0 initially");
    }

    @Test
    void testTaskExecution() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        

        thread.newTask(latch::countDown);
        
        boolean finished = latch.await(2, TimeUnit.SECONDS);
        assertTrue(finished, "Task should have been executed");
    }

    @Test
    void testFatigueIncreasesAfterWork() throws InterruptedException {
        double initialFatigue = thread.getFatigue();
        
        thread.newTask(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        Thread.sleep(200);
        
        assertTrue(thread.getFatigue() > initialFatigue, "Fatigue should increase after work");
    }

    @Test
    void testNewTaskThrowsExceptionWhenFull() {
        thread.newTask(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) {}
        });

        assertThrows(IllegalStateException.class, () -> {
            while(true) {
                thread.newTask(() -> {});
            }
        }, "Should throw IllegalStateException when queue is full");
    }

    @Test
    void testCompareTo() {
        TiredThread freshThread = new TiredThread(2, 1.0);
          assertEquals(0, thread.compareTo(freshThread), "Equal fatigue should return 0");
    }

@Test
void testTaskThrowsException_ThreadStaysAlive() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    
    // משימה שקורסת
    thread.newTask(() -> {
        throw new RuntimeException("Boom!");
    });
    
   
    Thread.sleep(100); 
    thread.newTask(latch::countDown);
    
    assertTrue(latch.await(1, TimeUnit.SECONDS), "Thread should execute next task even if previous one failed");
}
    
}
