package org.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;

public class UserMatchesDao {
  private static final String TABLE_NAME = "UserMatches";
  private final DynamoDbClient dynamoDbClient;

  public UserMatchesDao(String awsAccessKey, String awsSecretKey, String awsRegion, String awsSessionToken) {
    AwsSessionCredentials awsCreds = AwsSessionCredentials.create(awsAccessKey, awsSecretKey, awsSessionToken);
    dynamoDbClient = DynamoDbClient.builder()
        .region(Region.of(awsRegion))
        .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
        .build();
  }

  public void batchSaveMatches(Map<String, Set<String>> userIdToLikesDislikes) {
    List<WriteRequest> writeRequests = new ArrayList<>();

    for (Map.Entry<String, Set<String>> entry : userIdToLikesDislikes.entrySet()) {
      String userId = entry.getKey();
      Set<String> swipeeIdsSet = entry.getValue();

      Map<String, AttributeValue> item = new HashMap<>();
      item.put("userId", AttributeValue.builder().s(userId).build());
      item.put("swipeeIdsSet", AttributeValue.builder().ss(swipeeIdsSet).build());

      PutRequest putRequest = PutRequest.builder().item(item).build();
      writeRequests.add(WriteRequest.builder().putRequest(putRequest).build());
    }

    Map<String, List<WriteRequest>> requestItems = new HashMap<>();
    requestItems.put(TABLE_NAME, writeRequests);

    BatchWriteItemRequest batchWriteItemRequest = BatchWriteItemRequest.builder()
        .requestItems(requestItems)
        .build();

    try {
      BatchWriteItemResponse response = dynamoDbClient.batchWriteItem(batchWriteItemRequest);
      // Handle any unprocessed items if necessary
      Map<String, List<WriteRequest>> unprocessedItems = response.unprocessedItems();
      if (!unprocessedItems.isEmpty()) {
        // Retry the unprocessed items or handle them according to your application logic
        System.err.println("Unprocessed items found: " + unprocessedItems);
      }
    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }

//  public void saveUserMatches(String userId, Set<String> swipeeIdsSet) {
//    Map<String, AttributeValue> item = new HashMap<>();
//    item.put("userId", AttributeValue.builder().s(userId).build());
//    item.put("swipeeIdsSet", AttributeValue.builder().ss(swipeeIdsSet).build());
//
//    PutItemRequest putItemRequest = PutItemRequest.builder()
//        .tableName(TABLE_NAME)
//        .item(item)
//        .build();
//
//    try {
//      PutItemResponse response = dynamoDbClient.putItem(putItemRequest);
////      System.out.println(TABLE_NAME +" was successfully updated. The request id is "+response.responseMetadata().requestId());
//    } catch (ResourceNotFoundException e) {
//      System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", TABLE_NAME);
//      System.err.println("Be sure that it exists and that you've typed its name correctly!");
//      System.exit(1);
//    } catch (DynamoDbException e) {
//      System.err.println(e.getMessage());
//      System.exit(1);
//    }
//  }
public void scheduleBatchUpdates(ConcurrentHashMap<String, Set<String>> hashmap) {
  ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  scheduler.scheduleAtFixedRate(() -> saveAllUserMatches(hashmap), 2, 2, TimeUnit.SECONDS);
}

//  private void saveAllUserMatches(Map<String, Set<String>> hashmap) {
//    synchronized (hashmap) {
//      for (Map.Entry<String, Set<String>> entry : hashmap.entrySet()) {
//        saveUserMatches(entry.getKey(), entry.getValue());
//      }
//      hashmap.clear();
//    }
//  }
private void saveAllUserMatches(ConcurrentHashMap<String, Set<String>> hashmap) {
  int batchSize = 25; // Maximum number of items allowed in a single BatchWriteItem request

  while (!hashmap.isEmpty()) {
    ConcurrentHashMap.KeySetView<String, Set<String>> keySetView = hashmap.keySet();
    Iterator<String> iterator = keySetView.iterator();
    Map<String, Set<String>> batch = new HashMap<>();

    for (int i = 0; i < batchSize && iterator.hasNext(); i++) {
      String userId = iterator.next();
      Set<String> swipeeIdsSet = hashmap.remove(userId);

      if (swipeeIdsSet != null) {
        batch.put(userId, swipeeIdsSet);
      }
    }

    batchSaveMatches(batch);
  }
}
}

