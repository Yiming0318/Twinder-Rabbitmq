package org.example;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.example.ConsumerRunnable;

public class ConsumerOne {
  private static final String AWS_ACCESS_KEY = "ASIAX7WX3UVXUG3RM7WB";
  private static final String AWS_SECRET_KEY = "2nSCg+1uNVqjE9i4ZXyCcedfr+vauCV4dX5Y4hYH";
  private static final String AWS_SESSION_TOKEN = "FwoGZXIvYXdzEM///////////wEaDBGGoWMQMk1cjXniuyLIAQrnNY9BG8DyQDGtE0OQeSbj9UCr/eNVmSeDQYu90bPiwoZwY75jbwtibO4IFuKuTnvLwKIgohWV31wDH4vtqjWWwrN+DcTJCmiDVQb9I4jUlKzhmKV8gTmQECXtih7L0KOEcB4Rw4xeE5xs0c3IXLZEHLVufGPCMhsPhseyK7G2zJq7nvtUI31iwTKSlM9tHShzIM0GvPsYxjRmpPIZCONo/XQK9zlCkwMZLB4/mIWr9kH5e26M8PHNBv7fDxtYDmf752NzVFskKNeWnaEGMi0bKpWM03mbLL/CguQX4i/eXn558LK0RNOTM14cAWBwgf+0OQ7TGk15hbW6JDA=";

  private static final String AWS_REGION = "us-west-2";
  private static final int NUM_THREADS = 200;
  private static final ConcurrentHashMap<String, int[]>  HASH_MAP = new ConcurrentHashMap<>();
  private static final String QUEUE_NAME = "likeDislikeQueue";
  private static final String EXCHANGE_NAME = "swipeFanoutExchange";

  public static void main(String[] args) throws IOException {
    ConnectionFactory connectionFactory = new ConnectionFactory();
    connectionFactory.setHost("35.90.1.27");
//    connectionFactory.setUsername("newadmin");
//    connectionFactory.setPassword("s0m3p4ssw0rd");
    connectionFactory.setUsername("yiming");
    connectionFactory.setPassword("yiming123");
    UserLikesDislikesDao userLikesDislikesDao = new UserLikesDislikesDao(AWS_ACCESS_KEY, AWS_SECRET_KEY, AWS_REGION, AWS_SESSION_TOKEN);
    try {
      Connection connection = connectionFactory.newConnection();
      ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
      for (int i = 0; i < NUM_THREADS; i++) {
        threadPool.execute(
            new ConsumerRunnable(connection, EXCHANGE_NAME, QUEUE_NAME, HASH_MAP, userLikesDislikesDao));
      }
      userLikesDislikesDao.scheduleBatchUpdates(HASH_MAP);
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
  }
}
