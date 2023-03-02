package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConsumerRunnable implements Runnable {
  private final Connection conn;
  private final String exchangeName;
  private final Map<String, Set<String>> hashmap;
  private final String queueName;
  public ConsumerRunnable(Connection conn, String exchangeName, String queueName, Map<String, Set<String>> hashmap) {
    this.conn = conn;
    this.exchangeName = exchangeName;
    this.hashmap = hashmap;
    this.queueName = queueName;
  }
  @Override
  public void run(){
    try {
      Channel channel = conn.createChannel();
      channel.queueDeclare(queueName, false, false, false, null);
      channel.queueBind(queueName, exchangeName, "");
      System.out.println("Thread: " + Thread.currentThread().getId() + " waiting for messages.");

      // accept only 1 unacknowledged message
      channel.basicQos(1);

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
  private void updateHashmap(String message) {
    String[] parts = message.split("/+");
    String leftOrRight = parts[0];
    String userId = parts[1];
    String swipeeId = parts[2];
    synchronized (hashmap) {
      Set<String> swipeeIds = hashmap.getOrDefault(userId, new HashSet<>());
      if (leftOrRight.equals("right")) {
        if (!countNumber(swipeeIds)){
          swipeeIds.add(swipeeId);
        }
      }
      }
    }
}

