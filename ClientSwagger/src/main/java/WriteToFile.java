import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public class WriteToFile {

  public static void writeToFile(List<PerformanceDetail> list, String fileName) {
    DateFormat df = new SimpleDateFormat("dd:MM:yy:HH:mm:ss");
    try (FileWriter writer = new FileWriter(fileName)) {
      // write header row
      writer.append("StartTime In Milliseconds").append(",")
          .append("StartTime In Date Format(day:month:year:hour:minute:second").append(",").
          append("RequestType").append(",").
          append("Latency").append(",").append("StatusCode").append("\n");

      // write data rows
      for (PerformanceDetail pd : list) {
        writer.append(String.valueOf(pd.getStartTime())).append(",").
            append(df.format(pd.getStartTime())).append(",").
            append(String.valueOf(pd.getRequestType())).append(",").
            append(String.valueOf(pd.getLatency())).append(",").
            append(String.valueOf(pd.getStatusCode())).append("\n");
      }
    } catch (IOException e) {
      System.out.println("An error occurred while writing to the file.");
      e.printStackTrace();
    }

  }

  public static void writeToPlotFile(Map<Integer, Integer> timeMap, String fileName) {
    try (FileWriter writer = new FileWriter(fileName)) {
      // write header row
      writer.append("Time").append(",").append("Requests").append("\n");

      // write data rows
      for (Map.Entry<Integer, Integer> entry : timeMap.entrySet()) {
        writer.append(String.valueOf(entry.getKey())).append(",").
            append(String.valueOf(entry.getValue())).append("\n");
      }
    } catch (IOException e) {
      System.out.println("An error occurred while writing to the file.");
      e.printStackTrace();
    }
  }
}
