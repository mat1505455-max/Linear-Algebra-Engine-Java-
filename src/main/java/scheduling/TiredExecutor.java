package scheduling;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TiredExecutor {

    private final TiredThread[] workers;
    private final PriorityBlockingQueue<TiredThread> idleMinHeap = new PriorityBlockingQueue<>();
    private final AtomicInteger inFlight = new AtomicInteger(0);

    public TiredExecutor(int numThreads) {
        workers = new TiredThread[numThreads]; 
        for (int i = 0; i < numThreads; i++) {
            workers[i] = new TiredThread(i, Math.random() + 0.5);
            workers[i].start();
            idleMinHeap.add(workers[i]);
        }
    }
    
    

   public void submit(Runnable task) {
    TiredThread worker = null;
    try {
        worker = idleMinHeap.take();
        inFlight.incrementAndGet();

        TiredThread finalWorker = worker;
        Runnable wrappedTask = () -> {
            try {
                task.run();
            } catch (Exception e) {
                System.err.println("Task failed: " + e.getMessage());
            } finally {
                inFlight.decrementAndGet();
                idleMinHeap.add(finalWorker); 
                synchronized (inFlight) {
                    inFlight.notifyAll(); 
                }
            }
        };
        worker.newTask(wrappedTask);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    } catch (Exception e) {
        
        if (worker != null) {
            inFlight.decrementAndGet();
            idleMinHeap.add(worker);
            synchronized (inFlight) {
                inFlight.notifyAll();
            }
        }
        throw e;
    }
}

    public void submitAll(Iterable<Runnable> tasks) {
        for(Runnable task : tasks){
            submit(task);
        }
        synchronized(inFlight){
            while (inFlight.get()>0) {
                try{
                    inFlight.wait();
                }            
                catch(InterruptedException e){
                Thread.currentThread().interrupt();
                return;
                }
            }
        }
    }

    public void shutdown() throws InterruptedException {
        for(TiredThread worker : workers){
            try{
            worker.shutdown();
            } catch (Exception e){}
        }
        for(TiredThread worker : workers){
            worker.join();
        }
    }

    public synchronized String getWorkerReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Worker Report ---\n");
    
        for (TiredThread w : workers) {
            sb.append("ID: ");
            sb.append(w.getWorkerId());
            sb.append(" | Fatigue: ");
            sb.append(w.getFatigue());
            sb.append(" | Work: ");
            sb.append(w.getTimeUsed() / 1000000);
            sb.append("ms | Idle: ");
            sb.append(w.getTimeIdle() / 1000000);
            sb.append("ms\n");
        }
    return sb.toString();
    }
}
