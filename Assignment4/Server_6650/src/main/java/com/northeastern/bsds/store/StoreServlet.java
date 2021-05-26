package com.northeastern.bsds.store;

import com.google.gson.Gson;
import com.northeastern.bsds.store.db.H2DBConnection;
import com.northeastern.bsds.store.entity.ItemResponse;
import com.northeastern.bsds.store.entity.Items;
import com.northeastern.bsds.store.entity.StoreResponse;
import com.northeastern.bsds.store.entity.Stores;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "StoreServlet")
public class StoreServlet extends HttpServlet {

  /**
   * Create H2 table for microservice
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
  protected void doPost(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {

      final String createTableSQL = "create table store\n"
          + "(\n"
          + "\t storeid int,\n"
          + "\t itemcount int,\n"
          + "\t itemid int\n"
          + ");\n"
          + "\n";

      try {
        // Step 1: Establishing a Connection
        Connection connection = new H2DBConnection().getH2connection();
        // Step 2:Create a statement using connection object
        Statement statement = connection.createStatement();
        // Step 3: Execute the query or update query
        statement.execute(createTableSQL);
        statement.close();
        connection.commit();
        connection.close();
      }
      catch(SQLException e){
        e.printStackTrace();
      }
  }


  /**
   * Get the top 10 stores for sales for item N
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {

    int itemId = this.getItemIdFromRequest(request);
    Connection connection = new H2DBConnection().getH2connection();
    String QUERY = "select sum(itemcount) as \"itemcount\", itemid,storeid from store "
        + "where storeid = ? group by itemid order by \"itemcount\" desc limit 5";

    PreparedStatement preparedStatement = null;
    ResultSet rs = null;
    List<Stores> stores = null;
    boolean flag = false;
    try {
      preparedStatement = connection.prepareStatement(QUERY);
      preparedStatement.setInt(1, itemId);
      // Step 3: Execute the query or update query
      rs = preparedStatement.executeQuery();
      stores = new ArrayList<>();
      // Step 4: Process the ResultSet object.
      while (rs.next()) {
        flag = true;

        int itemid = rs.getInt("itemid");
        int itemcount = rs.getInt("itemcount");
        Stores store = new Stores(itemid,itemcount);
        stores.add(store);
      }
    }
    catch(Exception e){
      e.printStackTrace();
    }
    finally {
      try {
        preparedStatement.close();
        rs.close();
        connection.close();
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

    StoreResponse jsonItemResponse = new StoreResponse(stores);
    Gson gson = new Gson();
    String json = gson.toJson(jsonItemResponse);
    //response.getWriter().print(jsonResponse);

    PrintWriter out = response.getWriter();
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");


    if(itemId==-1){
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      out.print("Please enter valid itemId");
    }
    else if(flag == false){
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      out.print("No data found");
    }
    else {
      out.print(json);
    }
    out.flush();
  }

  private int getItemIdFromRequest(HttpServletRequest request) {
    try {
      String urlPath = request.getPathInfo();
      String[] urlParts = urlPath.split("/");
      return Integer.parseInt(urlParts[1]);
    }
    catch (Exception e){
      return -1;
    }
  }
}
