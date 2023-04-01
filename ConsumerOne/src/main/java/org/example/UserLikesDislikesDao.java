package org.example;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.BatchWriteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.WriteRequest;
import software.amazon.awssdk.services.dynamodb.model.PutRequest;

import java.util.List;
import java.util.stream.Collectors;

public class UserLikesDislikesDao {
  private static final String TABLE_NAME = "UserLikesDislikes";
  private final DynamoDbClient dynamoDbClient;

public UserLikesDislikesDao(String awsAccessKey, String awsSecretKey, String awsRegion, String awsSessionToken) {
  AwsSessionCredentials awsCreds = AwsSessionCredentials.create(awsAccessKey, awsSecretKey, awsSessionToken);
  dynamoDbClient = DynamoDbClient.builder()
      .region(Region.of(awsRegion))
      .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
      .build();
}


  public void batchSaveUserLikesDislikes(Map<String, int[]> userIdToLikesDislikes) {
    List<WriteRequest> writeRequests = new ArrayList<>();

    for (Map.Entry<String, int[]> entry : userIdToLikesDislikes.entrySet()) {
      String userId = entry.getKey();
      int[] likesDislikes = entry.getValue();

      Map<String, AttributeValue> item = new HashMap<>();
      item.put("userId", AttributeValue.builder().s(userId).build());
      item.put("numOfDislikes", AttributeValue.builder().n(Integer.toString(likesDislikes[0])).build());
      item.put("numOfLikes", AttributeValue.builder().n(Integer.toString(likesDislikes[1])).build());

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


  public void scheduleBatchUpdates(ConcurrentHashMap<String, int[]> hashmap) {
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    // the task will be executed for the first time after a 1-second delay and will continue to run every 1 second thereafter.
    scheduler.scheduleAtFixedRate(() -> saveAllUserMatches(hashmap), 2, 2, TimeUnit.SECONDS);
  }


  private void saveAllUserMatches(ConcurrentHashMap<String, int[]> hashmap) {
    int batchSize = 25; // Maximum number of items allowed in a single BatchWriteItem request

    while (!hashmap.isEmpty()) {
      ConcurrentHashMap.KeySetView<String, int[]> keySetView = hashmap.keySet();
      Iterator<String> iterator = keySetView.iterator();
      Map<String, int[]> batch = new HashMap<>();

      for (int i = 0; i < batchSize && iterator.hasNext(); i++) {
        String userId = iterator.next();
        int[] likesDislikes = hashmap.remove(userId);

        if (likesDislikes != null) {
          batch.put(userId, likesDislikes);
        }
      }

      batchSaveUserLikesDislikes(batch);
    }
  }

}



