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
  private static final int NUM_THREADS = 200;
  private static final Map<String, int[]>  HASH_MAP = new ConcurrentHashMap<>();
  private static final String QUEUE_NAME = "likeDislikeQueue";
  private static final String EXCHANGE_NAME = "swipeFanoutExchange";

  public static void main(String[] args) throws IOException {
    ConnectionFactory connectionFactory = new ConnectionFactory();
//    connectionFactory.setHost("52.40.75.107");
    connectionFactory.setHost("35.91.118.2");
//    connectionFactory.setUsername("newadmin");
//    connectionFactory.setPassword("s0m3p4ssw0rd");
    connectionFactory.setUsername("yiming");
    connectionFactory.setPassword("yiming123");
    try {
      Connection connection = connectionFactory.newConnection();
      ExecutorService threadPool = Executors.newFixedThreadPool(NUM_THREADS);
      for (int i = 0; i < NUM_THREADS; i++) {
        threadPool.execute(
            new ConsumerRunnable(connection, EXCHANGE_NAME, QUEUE_NAME, HASH_MAP));
      }
    } catch (TimeoutException e) {
      e.printStackTrace();
    }
  }
}
