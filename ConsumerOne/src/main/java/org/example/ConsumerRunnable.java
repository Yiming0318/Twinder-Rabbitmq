package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.example.UserLikesDislikesDao;
public class ConsumerRunnable implements Runnable {
  private final Integer BATCH_SIZE = 10;
  private final Connection conn;
  private final String exchangeName;
  private final Map<String, int[]> hashmap;
  private final String queueName;
  private final UserLikesDislikesDao userLikesDislikesDao;
  private List<String> batch;

  public ConsumerRunnable(Connection conn, String exchangeName, String queueName, ConcurrentHashMap<String, int[]> hashmap, UserLikesDislikesDao userLikesDislikesDao) {
    this.conn = conn;
    this.exchangeName = exchangeName;
    this.hashmap = hashmap;
    this.queueName = queueName;
    this.userLikesDislikesDao = userLikesDislikesDao;
    this.batch = new ArrayList<>();
  }
  @Override
  public void run(){
    try {
      Channel channel = conn.createChannel();
      channel.queueDeclare(queueName, false, false, false, null);
      channel.queueBind(queueName, exchangeName, "");
      System.out.println("Thread: " + Thread.currentThread().getId() + " waiting for messages.");

      // accept only 1 unacknowledged message
      channel.basicQos(10);

      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
//        System.out.println(message);
//        System.out.println(" [x] Received '" + message + "'");
        updateHashmap(message);
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      };
      channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void updateHashmap(String message) {
    String[] parts = message.split("/+");
    String leftOrRight = parts[1];
    String userId = parts[2];
//    System.out.println(leftOrRight);
//    System.out.println(userId);
//    synchronized (hashmap) {
//      int[] currentCounts = hashmap.getOrDefault(userId, new int[]{0, 0});
//      int numOfDislike = currentCounts[0];
//      int numOfLike = currentCounts[1];
//      if (leftOrRight.equals("left")) {
//        numOfDislike++;
//      } else {
//        numOfLike++;
//      }
//      hashmap.put(userId, new int[]{numOfDislike, numOfLike});
//    }

      hashmap.compute(userId, (key, currentCounts) -> {
        if (currentCounts == null) {
          currentCounts = new int[]{0, 0};
        }
        int numOfDislike = currentCounts[0];
        int numOfLike = currentCounts[1];
        if (leftOrRight.equals("left")) {
          numOfDislike++;
        } else {
          numOfLike++;
        }
        return new int[]{numOfDislike, numOfLike};
      });
    }

}

