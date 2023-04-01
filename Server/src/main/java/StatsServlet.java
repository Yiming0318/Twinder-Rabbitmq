import com.google.gson.Gson;
import entities.ResponseMessage;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

@WebServlet(name = "StatsServlet", value = "/StatsServlet")
public class StatsServlet extends HttpServlet {
  private static final String USER_NOT_FOUND = "User not found";
  private DynamoDbClient dynamoDbClient;
  private static final String TABLE_NAME_ONE = "UserLikesDislikes";
  private static final String TABLE_NAME_TWO = "UserMatches";
  private static final String AWS_ACCESS_KEY = "ASIAX7WX3UVXUG3RM7WB";
  private static final String AWS_SECRET_KEY = "2nSCg+1uNVqjE9i4ZXyCcedfr+vauCV4dX5Y4hYH";
  private static final String AWS_SESSION_TOKEN = "FwoGZXIvYXdzEM///////////wEaDBGGoWMQMk1cjXniuyLIAQrnNY9BG8DyQDGtE0OQeSbj9UCr/eNVmSeDQYu90bPiwoZwY75jbwtibO4IFuKuTnvLwKIgohWV31wDH4vtqjWWwrN+DcTJCmiDVQb9I4jUlKzhmKV8gTmQECXtih7L0KOEcB4Rw4xeE5xs0c3IXLZEHLVufGPCMhsPhseyK7G2zJq7nvtUI31iwTKSlM9tHShzIM0GvPsYxjRmpPIZCONo/XQK9zlCkwMZLB4/mIWr9kH5e26M8PHNBv7fDxtYDmf752NzVFskKNeWnaEGMi0bKpWM03mbLL/CguQX4i/eXn558LK0RNOTM14cAWBwgf+0OQ7TGk15hbW6JDA=";
  private static final String AWS_REGION = "us-west-2";

  public void init() throws ServletException {
    super.init();
    // Initialize DynamoDB client
    AwsSessionCredentials awsCreds = AwsSessionCredentials.create(AWS_ACCESS_KEY, AWS_SECRET_KEY,
        AWS_SESSION_TOKEN);
    dynamoDbClient = DynamoDbClient.builder()
        .region(Region.of(AWS_REGION))
        .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
        .build();
  }


  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String path = request.getPathInfo();
    String[] parts = path.split("/");
    if (parts.length == 2) {
      handleStats(request, response, parts[1]);
    } else {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    }
  }

  private Map<String, AttributeValue> fetchUserData(String userId, String tableName) {
    GetItemRequest request = GetItemRequest.builder()
        .tableName(tableName)
        .key(Collections.singletonMap("userId", AttributeValue.builder().s(userId).build()))
        .build();
    GetItemResponse response = dynamoDbClient.getItem(request);
    return response.item();
  }

  private void handleStats(HttpServletRequest req, HttpServletResponse res, String userId)
      throws IOException {
    // Fetch user data from DynamoDB using userId
    Map<String, AttributeValue> item = fetchUserData(userId, TABLE_NAME_ONE);

    // Create a JSON response
    res.setContentType("application/json");
    Gson gson = new Gson();

    if (!item.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_OK);
      int numLikes = Integer.parseInt(item.get("numOfLikes").n());
      int numDislikes = Integer.parseInt(item.get("numOfDislikes").n());
      Map<String, Integer> stats = new HashMap<>();
      stats.put("numOfLikes", numLikes);
      stats.put("numOfDislikes", numDislikes);
      res.getWriter().write(gson.toJson(stats));
    } else {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write(gson.toJson(new ResponseMessage(USER_NOT_FOUND)));
    }
  }
}
