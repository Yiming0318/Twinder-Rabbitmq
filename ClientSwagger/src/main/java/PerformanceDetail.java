public class PerformanceDetail {
  private long startTime;
  private String requestType;
  private long latency;
  private int statusCode;

  public PerformanceDetail(long startTime, String requestType, long latency, int statusCode) {
    this.startTime = startTime;
    this.requestType = requestType;
    this.latency = latency;
    this.statusCode = statusCode;
  }


  public synchronized long getStartTime() {
    return this.startTime;
  }

  public synchronized void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public synchronized String getRequestType() {
    return this.requestType;
  }

  public synchronized void setRequestType(String requestType) {
    this.requestType = requestType;
  }

  public synchronized long getLatency() {
    return this.latency;
  }

  public synchronized void setLatency(long latency) {
    this.latency = latency;
  }

  public synchronized int getStatusCode() {
    return this.statusCode;
  }

  public synchronized void setStatusCode(int statusCode) {
    this.statusCode = statusCode;
  }



}
