import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Utilities {

  private static final String LEFT = "left";
  private static final String RIGHT = "right";
  private static final int MAX_CHAR_NUMBER = 256;
  private static final String candidateChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
  public static ArrayList<PerformanceDetail> pdList = new ArrayList<>();


  /**
   * The performanceDetailAdd function adds a PerformanceDetail object to the list of performance
   * details.
   *
   * @param pd Store the performance detail that is being added to the list
   * @return Nothing
   */
  public static synchronized void performanceDetailAdd(PerformanceDetail pd) {
    pdList.add(pd);
  }

  /**
   * The generateRandomInt function generates a random integer between the minValue and maxValue
   * parameters.
   *
   * @param minValue Set the minimum value that can be generated
   * @param maxValue Set the maximum value of the random number
   * @return A random integer between the minvalue and maxvalue
   */
  public static int generateRandomInt(Integer minValue, Integer maxValue) {
//    Random rn = new Random();
    // minValue + rn.nextInt(maxValue - minValue + 1)
//    return minValue + rn.nextInt(maxValue - minValue + 1);
    return minValue + ThreadLocalRandom.current().nextInt(maxValue - minValue + 1);
  }

  /**
   * The generateLeftOrRight function generates a random number between 0 and 1. If the random
   * number is less than or equal to 0.5, it returns LEFT; otherwise, it returns RIGHT.
   *
   * @return String left or right
   */
  public static String generateLeftOrRight() {
    int randomNumber = generateRandomInt(0, 1);
    if (randomNumber <= 0.5) {
      return LEFT;
    } else {
      return RIGHT;
    }
  }

  /**
   * The generateComment function generates a random comment string of length between 1 and
   * MAX_CHAR_NUMBER.
   *
   * @return A random string of characters
   */
  public static String generateComment() {
    StringBuilder sb = new StringBuilder();
//    Random random = new Random();
    int commentLength = generateRandomInt(1, MAX_CHAR_NUMBER);
    for (int i = 0; i < commentLength; i++) {
//      sb.append(candidateChars.charAt(random.nextInt(candidateChars
//          .length())));
      sb.append(candidateChars.charAt(ThreadLocalRandom.current().nextInt(candidateChars
          .length())));
    }
    return sb.toString();
  }

  /**
   * The median function returns the median value of a list of numbers. If the list has an odd
   * number of values, it returns the middle value from sorted order. If the list has an even number
   * of values, it returns the average of the two middle values from sorted order.
   *
   * @param values Pass in the sorted values
   * @return The median value of a list of values
   */
  public static long median(ArrayList<Long> values) {
    if (values.size() % 2 == 1) {
      return values.get((values.size() + 1) / 2 - 1);
    } else {
      long lower = values.get(values.size() / 2 - 1);
      long upper = values.get(values.size() / 2);

      return (lower + upper) / 2;
    }
  }

  /**
   * The mean function computes the arithmetic mean of a list of numbers.
   *
   * @param values Store the values
   * @return The mean of the values in the arraylist
   */
  public static double mean(ArrayList<Long> values) {
    long sum = 0;
    for (long number : values) {
      sum += number;
    }
    return (double) sum / values.size();
  }

  /**
   * The p99 function returns the 99th percentile of a set of values.
   *
   * @param values Pass in the sorted values
   * @return The value in the list that is at index (int)(0
   */
  public static double p99(ArrayList<Long> values) {
    return values.get((int) (0.99 * values.size()));
  }

  /**
   * The min function returns the minimum value in a list of longs.
   *
   * @param values Pass in the sorted values
   * @return The value of the smallest element in the list
   */
  public static long min(ArrayList<Long> values) {
    return Collections.min(values);
  }

  /**
   * The max function returns the maximum value in a list of longs.
   *
   * @param values Pass in the sorted values
   * @return The largest value in the arraylist
   */
  public static long max(ArrayList<Long> values) {
    return Collections.max(values);
  }

  /**
   * The getLatencies function takes a list of PerformanceDetail objects and returns an ArrayList
   * containing the latencies for each of those objects. The returned ArrayList is sorted in
   * ascending order.
   *
   * @param pdList Get the latencies of all the performance details
   * @return A list of latencies sorted in ascending order
   */
  public static ArrayList<Long> getLatencies(ArrayList<PerformanceDetail> pdList) {
    ArrayList<Long> latencyList = new ArrayList<Long>();
    for (PerformanceDetail pd : pdList) {
      latencyList.add(pd.getLatency());
    }
    Collections.sort(latencyList);
    return latencyList;
  }


}
