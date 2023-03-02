public class Counter {
  private int successfulCount = 0;
  private int failedCount = 0;
  private int threadCount = 0;

  public synchronized void threadInc() {
    this.threadCount++;
  }
  public synchronized void successInc() {
    this.successfulCount++;
  }
  public synchronized void failInc() {
    this.failedCount++;
  }
  public synchronized int getThreadCount() {
    return this.threadCount;
  }

  public synchronized int getSuccessfulCount() {
    return this.successfulCount;
  }

  public synchronized int getFailedCount() {
    return this.failedCount;
  }


}
