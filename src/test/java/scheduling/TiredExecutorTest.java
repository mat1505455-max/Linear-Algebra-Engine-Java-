package scheduling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class TiredExecutorTest {

   

    @Test
    void testSubmitSingleTask() throws InterruptedException {
        TiredExecutor executor = new TiredExecutor(2);
        AtomicInteger counter = new AtomicInteger(0);
        
        executor.submit(counter::incrementAndGet);
        
        Thread.sleep(200);
        assertEquals(1, counter.get());
        executor.shutdown();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testSubmitAllBlocksUntilDone() throws InterruptedException {
        TiredExecutor executor = new TiredExecutor(4);
        AtomicInteger counter = new AtomicInteger(0);
        int numTasks = 10;
        
        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < numTasks; i++) {
            tasks.add(() -> {
                try { Thread.sleep(50); } catch (InterruptedException e) {}
                counter.incrementAndGet();
            });
        }

        executor.submitAll(tasks);
        
        assertEquals(numTasks, counter.get(), "All tasks should be finished before submitAll returns");
        executor.shutdown();
    }

 

    @Test
    void testPriorityScheduling() throws InterruptedException {
        TiredExecutor executor = new TiredExecutor(2);
        
        executor.submit(() -> {
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        });

        Thread.sleep(50); 
       
        executor.submit(() -> {
            try { Thread.sleep(200); } catch (InterruptedException e) {}
        });

        Thread.sleep(500); 

        String report = executor.getWorkerReport();
        assertTrue(report.contains("Fatigue:"), "Report should contain fatigue data");
        
        executor.shutdown();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testHighLoad() throws InterruptedException {
        int numThreads = 5;
        TiredExecutor executor = new TiredExecutor(numThreads);
        AtomicInteger totalExecuted = new AtomicInteger(0);
        int totalTasks = 50;

        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < totalTasks; i++) {
            tasks.add(totalExecuted::incrementAndGet);
        }

        executor.submitAll(tasks);
        assertEquals(totalTasks, totalExecuted.get());
        executor.shutdown();
    }

 

    @Test
    void testShutdownEdgeCase() throws InterruptedException {
        TiredExecutor executor = new TiredExecutor(1);
       
        executor.submit(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) {}
        });
        
        long start = System.currentTimeMillis();
        executor.shutdown();
        long duration = System.currentTimeMillis() - start;
        
        assertTrue(duration >= 500, "Shutdown should wait for active tasks to finish");
    }

    @Test
    void testEmptySubmitAll() throws InterruptedException {
        TiredExecutor executor = new TiredExecutor(2);
        assertDoesNotThrow(() -> executor.submitAll(new ArrayList<>()));
        executor.shutdown();
    }

 

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testSubmitAllWithManyTasks() throws InterruptedException {
        int numThreads = 3;
        TiredExecutor executor = new TiredExecutor(numThreads);
        AtomicInteger counter = new AtomicInteger(0);
        int totalTasks = 20;

        List<Runnable> tasks = new ArrayList<>();
        for (int i = 0; i < totalTasks; i++) {
            tasks.add(() -> {
                try { Thread.sleep(10); } catch (InterruptedException e) {}
                counter.incrementAndGet();
            });
        }

        executor.submitAll(tasks);
        assertEquals(totalTasks, counter.get(), "All tasks should complete");
        executor.shutdown();
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void testConcurrentSubmitAll() throws InterruptedException {
        TiredExecutor executor = new TiredExecutor(4);
        AtomicInteger counter = new AtomicInteger(0);
        
        Runnable bulkWriter = () -> {
            List<Runnable> tasks = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                tasks.add(counter::incrementAndGet);
            }
            executor.submitAll(tasks);
        };

        Thread t1 = new Thread(bulkWriter);
        Thread t2 = new Thread(bulkWriter);

        t1.start();
        t2.start();
        
        t1.join();
        t2.join();

        assertEquals(20, counter.get());
        executor.shutdown();
    }

    @Test
    @Timeout(value = 3, unit = TimeUnit.SECONDS)
    void testTaskWithExceptionDoesNotHang() throws InterruptedException {
        TiredExecutor executor = new TiredExecutor(1);
        
  
        executor.submit(() -> {
            throw new RuntimeException("Failure");
        });

   
        AtomicInteger success = new AtomicInteger(0);
        executor.submit(success::incrementAndGet);

        Thread.sleep(300); 
        assertTrue(success.get() > 0, "Executor should recover from task failure and execute next task");
        
        executor.shutdown();
    }
}