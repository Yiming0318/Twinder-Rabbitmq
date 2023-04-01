import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Client {
  private final static int NUMTHREADS = 150;
  private final static  int NUMREQUEST = 500000;
  private final static  Double MILLISECONDS = 1000.0;
  public static void main(String[] args) throws InterruptedException {
    Counter counter = new Counter();
    CountDownLatch completed = new CountDownLatch(NUMTHREADS);
    CountDownLatch postThreadsStarted = new CountDownLatch(NUMTHREADS);
    // Start GetThread after all POST threads have started
    GetThread getThread = new GetThread(postThreadsStarted, completed);
    getThread.start();
    long start = System.currentTimeMillis();
    if (NUMREQUEST % NUMTHREADS == 0) {
      Threads.threadsWithEqualAmountRequest(NUMTHREADS, NUMREQUEST, counter, completed, postThreadsStarted);
    } else {
      Threads.threadsWithDifferentAmountRequest(NUMTHREADS, NUMREQUEST, counter, completed, postThreadsStarted);
    }
    long finish = System.currentTimeMillis();
    // Wait for all POST threads to finish
    completed.await();
    // Wait for the GetThread to finish
    getThread.join();
    double seconds = (finish - start) / MILLISECONDS;
    System.out.println("Threads number should be equal to " + NUMTHREADS + " It is: " + counter.getThreadCount());
    System.out.println("Finished all threads, success request " + counter.getSuccessfulCount());
    System.out.println("Finished all threads, failed request " + counter.getFailedCount());
    System.out.println("Finished all threads, spent " + seconds + " seconds");
    System.out.println("######Part2 Stats#######");
    ArrayList<Long> latenciesList;
    latenciesList = Utilities.getLatencies(Utilities.pdList);
    System.out.println("The mean response time in milliseconds: " + Utilities.mean(latenciesList));
    System.out.println("The median response time in milliseconds: " + Utilities.median(latenciesList));
    System.out.println("The total throughput in requests per second: " + NUMREQUEST / seconds);
    System.out.println("The p99 response time in milliseconds: " + Utilities.p99(latenciesList));
    System.out.println("The min response time in milliseconds: " + Utilities.min(latenciesList));
    System.out.println("The max response time in milliseconds: " + Utilities.max(latenciesList));
//    WriteToFile.writeToFile(Utilities.pdList, "Performances-500k-50.csv");
//    Map<Integer,Integer> timeMap = SecondVSThroughput.printOutSecondsVSThroughput("Performances-500k-50.csv");
//    WriteToFile.writeToPlotFile(timeMap,"Plot-500k-50.csv");
  }

}
