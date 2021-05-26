package com.northeastern.bsds.store.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class H2DBConnection {

  private Connection h2connection = null;

  public H2DBConnection()
  {
    String h2Url = System.getProperty("h2.url");
    String h2Username = System.getProperty("h2.username");
    String h2Password = System.getProperty("h2.password");

    try {
      Class.forName(System.getProperty("h2.driverClassName"));
      h2connection = DriverManager.getConnection(h2Url, h2Username, h2Password);
    }
    catch (ClassNotFoundException | SQLException e) {
      e.printStackTrace();
    }
  }

  public Connection getH2connection() {
    return h2connection;
  }
}
