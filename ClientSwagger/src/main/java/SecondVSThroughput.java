import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SecondVSThroughput {

  public static Long START_TIME = 0L;

  /**
   * The printOutSecondsVSThroughput function prints out the number of requests that were made in
   * each second.
   *
   * @param filename Specify the file that contains the log
   * @return A map of seconds and the number of requests that were made in that second
   */
  public static Map<Integer, Integer> printOutSecondsVSThroughput(String filename) {
    ArrayList<Long> results = getRequestFinishedTimeList(filename);
    // Calculate how many seconds do we have
    int count = 0;
    int second = 1;
    Map<Integer, Integer> timeMap = new HashMap<>();
    ArrayList<Long> difference = new ArrayList<Long>();
    for (int i = 0; i < results.size(); i++) {
      difference.add(results.get(i) - START_TIME);
    }
    for (long diff : difference) {
      int seconds = (int) diff / 1000;
      if (timeMap.containsKey(seconds)) {
        timeMap.put(seconds, timeMap.get(seconds) + 1);
      } else {
        timeMap.put(seconds, 1);
      }
    }
//    for (Map.Entry<Integer, Integer> entry : timeMap.entrySet()) {
//      System.out.println(entry.getKey() + " second: " + entry.getValue() + " requests");
//    }
    return timeMap;
  }

  /**
   * The getRequestFinishedTimeList function takes in a file name and returns an ArrayList of Longs.
   * The function reads the file line by line, splits each line by commas, and adds the start time
   * to a list. It also adds the latency to another list which is then sorted.  The function returns
   * both lists after they are sorted.
   *
   * @param fileName Specify the file that contains the data
   * @return A list of request finished times
   */
  private static ArrayList<Long> getRequestFinishedTimeList(String fileName) {
    String line = "";
    String comma = ",";
    ArrayList<Long> startTimeList = new ArrayList<>();
    ArrayList<Long> results = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
      // Skip the first line with headers
      br.readLine();
      while ((line = br.readLine()) != null) {
        // split the line by comma
        String[] row = line.split(comma);
        // get start time
        long startTime = Long.parseLong(row[0]);
        startTimeList.add(startTime);
        // get latency
        int latency = Integer.parseInt(row[3]);
        long sum = startTime + latency;
        results.add(sum);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    Collections.sort(startTimeList);
    Collections.sort(results);
    START_TIME = startTimeList.get(0);
    return results;
  }
}

