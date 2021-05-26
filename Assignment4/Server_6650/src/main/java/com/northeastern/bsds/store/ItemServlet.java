package com.northeastern.bsds.store;

import com.google.gson.Gson;
import com.northeastern.bsds.store.db.H2DBConnection;
import com.northeastern.bsds.store.entity.ItemResponse;
import com.northeastern.bsds.store.entity.Items;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(name = "ItemServlet", urlPatterns = {
    "/{storeID}"})
public class ItemServlet extends HttpServlet {

  protected void doPost(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {

  }

  /**
   * Get the top 10 most purchased items at Store N
   * @param request
   * @param response
   * @throws ServletException
   * @throws IOException
   */
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response)
      throws ServletException, IOException {

    int storeId = this.getStoreIdFromRequest(request);
    Connection connection = new H2DBConnection().getH2connection();
    String QUERY = "select sum(itemcount) as \"itemcount\", itemid,storeid from store\n"
        + "    where itemid = ? group by storeid order by \"itemcount\" desc limit 10;";

   // String QUERY = "select * from public.store order by itemcount desc limit 10";
    boolean flag = false;

    PreparedStatement preparedStatement = null;
    ResultSet rs = null;
    List<Items> stores = null;
    try {
      preparedStatement = connection.prepareStatement(QUERY);
      preparedStatement.setInt(1, storeId);
      // Step 3: Execute the query or update query
      rs = preparedStatement.executeQuery();
      stores = new ArrayList<Items>();

      // Step 4: Process the ResultSet object.
      while (rs.next()) {
        flag = true;
        int storeid = rs.getInt("storeid");
        int itemcount = rs.getInt("itemcount");
        int itemid = rs.getInt("itemid");
        Items store = new Items(storeid,itemid,itemcount);
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

    ItemResponse jsonItemResponse = new ItemResponse(stores);
    Gson gson = new Gson();
    String json = gson.toJson(jsonItemResponse);
    //response.getWriter().print(jsonResponse);
    PrintWriter out = response.getWriter();
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    if(storeId==-1){
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      out.print("Please enter valid storeId");
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

  /**
   * @param request
   * @return
   */
  private int getStoreIdFromRequest(HttpServletRequest request) {
    try {
      String urlPath = request.getPathInfo();
      String[] urlParts = urlPath.split("/");
      return Integer.parseInt(urlParts[1]);
    }
    catch(Exception e){
      return -1;
    }
  }
}
