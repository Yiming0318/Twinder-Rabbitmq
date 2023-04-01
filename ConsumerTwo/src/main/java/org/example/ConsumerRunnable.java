package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.example.UserMatchesDao;
public class ConsumerRunnable implements Runnable {
  private final Connection conn;
  private final String exchangeName;
  private final Map<String, Set<String>> hashmap;
  private final String queueName;
  private final UserMatchesDao userMatchesDao;

  public ConsumerRunnable(Connection conn, String exchangeName, String queueName, Map<String, Set<String>> hashmap, UserMatchesDao userMatchesDao) {
    this.conn = conn;
    this.exchangeName = exchangeName;
    this.hashmap = hashmap;
    this.queueName = queueName;
    this.userMatchesDao = userMatchesDao;
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
//        System.out.println(" [x] Received '" + message + "'");
        updateHashmap(message);
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      };
      channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean countNumber(Set<String>swipeeIds){
    int count = 0;
    for (String id : swipeeIds) {
      count++;
    }
    return count >= 100;
  }

  private void addOrUpdateSwipeeIds(String userId, String swipeeId) {
    synchronized (hashmap) {
      Set<String> swipeeIds = hashmap.getOrDefault(userId, new HashSet<>());
      if (!countNumber(swipeeIds)) {
        swipeeIds.add(swipeeId);
      }
      hashmap.put(userId, swipeeIds);
    }
  }

  private void updateHashmap(String message) {
    String[] parts = message.split("/+");
    String leftOrRight = parts[1];
    String userId = parts[2];
    String swipeeId = parts[3];

    if (leftOrRight.equals("right")) {
      addOrUpdateSwipeeIds(userId, swipeeId);
    }
  }
//  private void updateHashmap(String message) {
//    String[] parts = message.split("/+");
//    String leftOrRight = parts[1];
//    String userId = parts[2];
//    String swipeeId = parts[3];
//    synchronized (hashmap) {
//      Set<String> swipeeIds = hashmap.getOrDefault(userId, new HashSet<>());
//      if (leftOrRight.equals("right")) {
//        if (!countNumber(swipeeIds)){
//          swipeeIds.add(swipeeId);
//        }
//        hashmap.put(userId, swipeeIds);
//        userMatchesDao.saveUserMatches(userId, swipeeIds);
//      }
//      }
//    }
}

