import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.StatsApi;
import io.swagger.client.api.MatchesApi;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

class GetThread extends Thread {
  private final CountDownLatch completed;
  private final CountDownLatch postThreadsStarted;
  private static final int GET_REQUESTS_PER_SECOND = 5;

  private final ArrayList<Long> latencies = new ArrayList<>();
  private static final int MIN_ID = 1;
  private static final int SWIPER_MAX_ID = 5000;
//  private static String BASE_PATH = "http://localhost:8080/Server_war/";
  private static String BASE_PATH = "http://a2loadbalancer-637467734.us-west-2.elb.amazonaws.com/Server_war/";

  public GetThread(CountDownLatch completed, CountDownLatch postThreadsStarted) {
    this.completed = completed;
    this.postThreadsStarted = postThreadsStarted;
  }

  @Override
  public void run() {
    try {

      ApiClient client = new ApiClient();
      // Set your base path here
      client.setBasePath(BASE_PATH);

      while (postThreadsStarted.getCount() != 0 && completed.getCount() > 0) {
        for (int i = 0; i < GET_REQUESTS_PER_SECOND; i++) {
          // Generate random data and call the appropriate GET API
          String userId = String.valueOf(Utilities.generateRandomInt(MIN_ID, SWIPER_MAX_ID));
          StatsApi statsApi = new StatsApi();
          MatchesApi matchesApi = new MatchesApi();
          long start = System.currentTimeMillis();
          if (Math.random() < 0.5) {
            // Call the matches API
            matchesApi.matchesWithHttpInfo(userId);
          } else {
            // Call the stats API
            statsApi.matchStatsWithHttpInfo(userId);
          }
          long end = System.currentTimeMillis();
          latencies.add(end - start);
        }

        // Sleep for 1 second before making the next batch of GET requests
        Thread.sleep(1000);
      }
      // Calculate and print min, mean, and max latencies
      if (!latencies.isEmpty()) {
//        System.out.println("GetThread latencies array size: " + latencies.size());
        System.out.println("GetThread min latency in milliseconds: " + Utilities.min(latencies));
        System.out.println("GetThread mean latency in milliseconds: " + Utilities.mean(latencies));
        System.out.println("GetThread max latency in milliseconds: " + Utilities.max(latencies));
      } else {
        System.out.println("No latencies recorded.");
      }

    } catch (InterruptedException | ApiException e) {
      e.printStackTrace();
    }
  }
}
