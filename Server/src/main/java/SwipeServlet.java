import com.google.gson.Gson;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.ConnectionFactory;
import entities.ResponseMessage;
import entities.SwipeDetails;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

@WebServlet(name = "SwipeServlet", value = "/SwipeServlet")
public class SwipeServlet extends HttpServlet {

  private static final String LEFT = "/left/";
  private static final String RIGHT = "/right/";
  private static final String MISSING_PARA = "Missing parameters!";
  private static final String INVALID_INPUT = "Invalid inputs";
  private static final String WRITE_SUCCESS = "Write successfully";
  private static final String USER_NOT_FOUND = "User not found";
  private static final Integer MIN_ID = 1;
  private static final Integer SWIPER_MAX_ID = 5000;
  private static final Integer SWIPEE_MAX_ID = 1000000;
  private static final Integer LEGAL_CHAR_NUMBER = 256;
  private static final String EXCHANGE_NAME = "swipeFanoutExchange";
  private ObjectPool<Channel> pool;
  public void init() throws ServletException {
    super.init();
    ConnectionFactory connectionFactory = new ConnectionFactory();
//    connectionFactory.setHost("52.40.75.107");
    connectionFactory.setHost("35.91.118.2");
//    connectionFactory.setUsername("newadmin");
//    connectionFactory.setPassword("s0m3p4ssw0rd");
    connectionFactory.setUsername("yiming");
    connectionFactory.setPassword("yiming123");
    try {
      // The channel facory generates new channels on demand, as needed by the GenericObjectPool
      Connection connection = connectionFactory.newConnection();
      pool = new GenericObjectPool<>(new ChannelFactory(connection));
    } catch (TimeoutException | IOException e) {
      System.out.println(e.getMessage());
    }
  }
  private boolean isValidLeftOrRight(String leftOrRight) {
    return leftOrRight.equals(LEFT) || leftOrRight.equals(RIGHT);
  }

  private boolean isValidRangeSwiper(String swiper) {
    try {
      int swiperID = Integer.parseInt(swiper);
      return (swiperID >= MIN_ID && swiperID <= SWIPER_MAX_ID);
    }catch(NumberFormatException e){
      return false;
    }
  }

  private boolean isValidRangeSwipee(String swipee) {
    try {
      int swipeeID = Integer.parseInt(swipee);
      return (swipeeID >= MIN_ID && swipeeID <= SWIPEE_MAX_ID);
    } catch(NumberFormatException e){
      return false;
    }
  }

  private boolean isValidCommentCharNumber(String comment) {
    int commentCharNumber = comment.length();
    return commentCharNumber <= LEGAL_CHAR_NUMBER;
  }


  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    try {
      Gson gson = new Gson();
      res.setContentType("application/json");
      // get left or right
      String leftOrRight = req.getPathInfo();
      // get the body information
      StringBuilder bodyInfo = new StringBuilder();
      String info;
      while ((info = req.getReader().readLine()) != null) {
        bodyInfo.append(info);
      }
      // transfer json to object
      SwipeDetails swipeDetails = (SwipeDetails) gson.fromJson(bodyInfo.toString(), SwipeDetails.class);
      // get each parameter
      String swiperID = swipeDetails.getSwiper();
      String swipeeID = swipeDetails.getSwipee();
      String comment = swipeDetails.getComment();

      if (!isValidLeftOrRight(leftOrRight)) {
        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        res.getWriter().write(gson.toJson(new ResponseMessage(INVALID_INPUT)));
      } else {
        if (swipeeID.isEmpty() || swiperID.isEmpty() || comment.isEmpty()) {
          res.setStatus(HttpServletResponse.SC_NOT_FOUND);
          res.getWriter().write(gson.toJson(new ResponseMessage(MISSING_PARA)));
        } else {
          if (isValidRangeSwiper(swiperID) && isValidRangeSwipee(swipeeID) && isValidCommentCharNumber(
              comment)) {
            res.setStatus(HttpServletResponse.SC_CREATED);
            res.getWriter().write(gson.toJson(new SwipeDetails(swiperID, swipeeID, comment)));
            try {
              // get channel
              Channel currentChannel = pool.borrowObject();
              // declare exchange
              currentChannel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT, false,
                  false, false, null);
              String message = leftOrRight + "/" + swipeeID + "/" + swiperID + "/" + comment;
              currentChannel.basicPublish(EXCHANGE_NAME, "", null,
                  message.getBytes(StandardCharsets.UTF_8));
              // sent message
//              System.out.println(" Server Sent '" + message + "'");
              // put back channel
              pool.returnObject(currentChannel);
            } catch (Exception e){
              System.out.println("Unsuccessfully sent message to Rabbitmq");
            }
          } else if (!isValidCommentCharNumber(comment)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write(gson.toJson(new ResponseMessage(INVALID_INPUT)));
          } else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write(gson.toJson(new ResponseMessage(USER_NOT_FOUND)));
          }
        }
      }
    } catch (Exception e) { // different parameters or no parameters
      Gson gson = new Gson();
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write(gson.toJson(new ResponseMessage(MISSING_PARA)));
    }
  }
}