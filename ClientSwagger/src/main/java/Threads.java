import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;


public class Threads {
  private static String POST = "POST";
  private static String GET = "GET";
  private static final int MIN_ID = 1;
  private static final int SWIPER_MAX_ID = 5000;
  private static final int SWIPEE_MAX_ID = 1000000;
  private static final int STATUS_CREATED = 201;
//  private static String BASE_PATH = "http://localhost:8080/Server_war/";
//  private static String BASE_PATH = "http://34.219.21.135:8080/Server_war/";
  private static String BASE_PATH = "http://a2loadbalancer-637467734.us-west-2.elb.amazonaws.com/Server_war/";
  private static final int RETRYTIMES = 5;
  public static void threadsWithEqualAmountRequest(Integer numThreads, Integer numRequests, Counter counter, CountDownLatch completed, CountDownLatch postThreadsStarted)
      throws InterruptedException {
    int avgRequest = numRequests / numThreads;
    for (int i = 0; i < numThreads ; i++) {
      singleThread(counter, avgRequest, completed, postThreadsStarted);
    }
    completed.await();
  }
  public static void threadsWithDifferentAmountRequest(Integer numThreads, Integer numRequests, Counter counter, CountDownLatch completed, CountDownLatch postThreadsStarted)
      throws InterruptedException {
    int avgRequest = numRequests / numThreads;
    int lastThreadRequest = numRequests - (avgRequest * (numThreads - 1));
    for (int i = 0; i < numThreads - 1 ; i++) {
      singleThread(counter, avgRequest, completed, postThreadsStarted);
    }
    singleThread(counter, lastThreadRequest, completed, postThreadsStarted);
    completed.await();
  }

  private static void singleThread(Counter counter, int threadRequest,
      CountDownLatch completed, CountDownLatch postThreadsStarted) {
    ApiClient client = new ApiClient();
    client.setBasePath(BASE_PATH);
    Runnable thread = () -> {
      postThreadsStarted.countDown();
      for(int j = 0; j < threadRequest; j++ ) {
        try {
          request(client, counter);
        } catch (ApiException e) {
          throw new RuntimeException(e);
        }
      }counter.threadInc(); completed.countDown();};
    new Thread(thread).start();
  }


 private static void request(ApiClient client, Counter counter) throws ApiException {
   // Get random data
   String swipeDirection = Utilities.generateLeftOrRight();
   int swiperId = Utilities.generateRandomInt(MIN_ID, SWIPER_MAX_ID);
   int swipeeId = Utilities.generateRandomInt(MIN_ID, SWIPEE_MAX_ID);
   String comment = Utilities.generateComment();

   SwipeDetails body = new SwipeDetails(); // SwipeDetails | response details
   body.setSwiper(String.valueOf(swiperId));
   body.setSwipee(String.valueOf(swipeeId));
   body.setComment(comment);
   SwipeApi apiInstance = new SwipeApi();
   apiInstance.setApiClient(client);

   try {
     long start = System.currentTimeMillis();
     // Execute the method.
     ApiResponse<Void> response = apiInstance.swipeWithHttpInfo(body, swipeDirection);
     long end = System.currentTimeMillis();
     int statusCode = response.getStatusCode();
     long latency = end - start;
     PerformanceDetail pd = new PerformanceDetail(start,POST,latency,statusCode);
     Utilities.performanceDetailAdd(pd);
     counter.successInc();
   } catch (ApiException e) {
     long start = System.currentTimeMillis();
     System.out.println("Retry Start...");
     int statusCode = retry(counter, apiInstance, swipeDirection, body, RETRYTIMES);
     long end = System.currentTimeMillis();
     long latency = end - start;
     System.out.println("Retry End!");
     PerformanceDetail pd = new PerformanceDetail(start,POST,latency,statusCode);
     Utilities.performanceDetailAdd(pd);
   }
 }


  private static int retry(Counter counter, SwipeApi apiInstance, String swipeDirection, SwipeDetails body, int retryTimes )
      throws ApiException {
    int statusCode = 404;
    for (int i = 0; i < retryTimes ; i++) {
      ApiResponse<Void> response = apiInstance.swipeWithHttpInfo(body, swipeDirection);
      statusCode = response.getStatusCode();
      if (statusCode == STATUS_CREATED){
        return statusCode;
      }
    }
    counter.failInc();
    return statusCode;
  }
}
