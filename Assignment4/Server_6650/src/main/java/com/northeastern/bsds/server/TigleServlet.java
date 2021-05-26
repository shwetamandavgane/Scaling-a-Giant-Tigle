package com.northeastern.bsds.server;

import com.google.gson.Gson;
import com.northeastern.bsds.server.rabbitmq.EndPoint;
import com.northeastern.bsds.store.db.AppHikariDataSource;
import com.northeastern.bsds.store.db.H2DBConnection;
import com.northeastern.bsds.server.entity.request.Item;
import com.northeastern.bsds.server.entity.request.Request;
import com.northeastern.bsds.store.entity.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet(name = "TigleServlet", urlPatterns = {
    "/purchase/{storeID}/customer/{custID}/date/{date}"})
public class TigleServlet extends HttpServlet {
  Connection H2connection;
  String INSERT_USERS_SQL;
  Integer storeId,custId,date;
  EndPoint endPoint;
  String INSERT_USERS_MYSQL = "INSERT INTO purchase" +
      "  (storeid,custid,itemid,itemcount,date) VALUES " +
      " (?,?,?,?,?);";

  @Override
  public void init() throws ServletException {
    super.init();
    //endPoint = new EndPoint("bsds_queue");
    H2connection = new H2DBConnection().getH2connection();
    INSERT_USERS_SQL = "INSERT INTO public.store" +
        "  (storeid,itemid,itemcount) VALUES " +
        " (?,?,?);";
  }

  protected void doPost(HttpServletRequest request,
      HttpServletResponse response) {
    //Call for converting request payload to Request object
    try {
      boolean isValid = isURLValid(request, response);
      if (isValid) {
        Request requestPayloadObject = getRequestPayloadObject(request);
        try {
          if (addPurchase(requestPayloadObject)) {
            response.setStatus(HttpServletResponse.SC_CREATED);
          }
        } catch (SQLException e) {
          e.printStackTrace();
          response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
        }
      }
      else {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      }
    }
    catch(Exception e){
      response.setStatus(HttpServletResponse.SC_CONFLICT);
      e.printStackTrace();
    }
  }

  /**
   *
   * @param request
   * @param response
   * @throws IOException
   */
  private boolean isURLValid(HttpServletRequest request,HttpServletResponse response)
      throws IOException {
    response.setContentType("text/plain");
    String urlPath = request.getPathInfo();
    String[] urlParts = urlPath.split("/");
    // check we have a valid URL!
    if (urlPath == null || urlPath.isEmpty() || urlParts.length < 7) {
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      return false;
    }

    if (!isUrlValid(urlParts)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return false;
    } else {
      return true;
    }
  }

  /**
   *
   * @param request
   * @return
   */
  private Request getRequestPayloadObject(HttpServletRequest request){
    StringBuffer jb = new StringBuffer();
    try {
      String line;
      BufferedReader reader = request.getReader();
      while ((line = reader.readLine()) != null)
        jb.append(line);
    } catch (Exception e) {
      System.out.println("some exception" + e.getMessage());
    }

    return convertRequestPayloadToRequestObject(jb);
  }

  /**
   *
   * @param jb
   * @return
   */
  private Request convertRequestPayloadToRequestObject(StringBuffer jb){
    Request requestPayload = null;
    try {
      Gson gson = new Gson();
      requestPayload = gson.fromJson(jb.toString(), Request.class);
    }
    catch (Exception e){
      System.out.println("Some exeption " + e.getMessage());
    }
    return requestPayload;
  }


  /**
   * Function to validate the URL params
   * @param urlPath
   * @return
   */
  private boolean isUrlValid(String[] urlPath) {
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/name/newUser/id/1234:"
    // urlParts = [, name, newUser, id, 1234]
    try {
      storeId = Integer.parseInt(urlPath[2]);
      custId = Integer.parseInt(urlPath[4]);

      if (urlPath[6].length() != 8) {
        return false;
      }
      date = Integer.parseInt(urlPath[6]);
    } catch (NumberFormatException e) {
      return false;
    }
    return true;
  }

  /**
   *
   * @param connection
   */
  private void DBClose(Connection connection){
    /*Closing the DB connection for safe closes*/
    try {
      connection.close();
    } catch (SQLException e) {
      System.out.println("Some exeption " + e.getMessage());
    }
  }

  /**
   *
   * @return
   */
  private boolean addPurchase(Request request) throws SQLException {
    Connection con = null;
    try {

      for (Item item : request.getItems()) {
        Message message = new Message(storeId, custId,Integer.parseInt(item.getItemID()),
                                     item.getNumberOfItems(),date);
     //   System.out.println("Message " + message.toString());
        this.sendMessage(message);
        this.insertIteminMemory(Integer.parseInt(item.getItemID()),
           item.getNumberOfItems());
      }
      //con.commit();
    } catch (IOException | TimeoutException e) {
      e.printStackTrace();
    } finally {
      if(con!=null)
        this.DBClose(con);
    }
    return true;
  }

  private String convertMessageObjectToJson(Message message){
    String json = null;
    try {
      Gson gson = new Gson();
      json = gson.toJson(message);
    }
    catch (Exception e){
      System.out.println("Some exeption " + e.getMessage());
    }
    return json;
  }

  public void sendMessage(Message message) throws IOException, TimeoutException {
    //endPoint.getChannel().basicPublish("bsds_exchange","direct_route",
      //  null, convertMessageObjectToJson(message).getBytes());
    try {
        Connection con = AppHikariDataSource.getConnection();
        PreparedStatement  preparedStatement = con.prepareStatement(INSERT_USERS_MYSQL);
        preparedStatement.setInt(1,message.getStoreId());
        preparedStatement.setInt(2,message.getCustId());
        preparedStatement.setInt(3,message.getItemId());
        preparedStatement.setInt(4,message.getItemCount());
        preparedStatement.setInt(5,message.getDate());
        preparedStatement.executeUpdate();
        preparedStatement.close();
        con.close();
      }
      catch (SQLException e) {
        e.printStackTrace();
      }
  }

  /**
   * Insert item to H2db
    * @param itemId
   * @param itemCount
   */
 private void insertIteminMemory(Integer itemId, Integer itemCount){
   PreparedStatement preparedStatement = null;
   try {
     preparedStatement = H2connection.prepareStatement(INSERT_USERS_SQL);

     preparedStatement.setInt(1,storeId);
     preparedStatement.setInt(2,itemId);
     preparedStatement.setInt(3,itemCount);

     preparedStatement.executeUpdate();
     preparedStatement.close();
     H2connection.commit();

   } catch (SQLException e) {
     e.printStackTrace();
   }
 }

 protected void doGet(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {
  }

  @Override
  public void destroy() {
    super.destroy();
    try {
      H2connection.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
