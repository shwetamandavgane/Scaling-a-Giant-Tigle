package com.northeastern.bsds.store.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class AppHikariDataSource {

  private static HikariConfig config = new HikariConfig();
  private static HikariDataSource ds;

  static {
    config.setJdbcUrl(System.getProperty( "mysql.url") );
    config.setUsername( System.getProperty("mysql.username") );
    config.setPassword( System.getProperty("mysql.password") );
    config.addDataSourceProperty( "cachePrepStmts" , "true" );
    config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
    config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
   // config.setDataSourceClassName("com.mysql.jdbc.jdbc2.optional.MysqlDataSource");
    config.addDataSourceProperty("autoReconnect", true);
    config.addDataSourceProperty("useServerPrepStmts", true);
    config.addDataSourceProperty("cacheResultSetMetadata", true);
    config.setMaximumPoolSize(30);
    config.setConnectionTimeout(3000);
    ds = new HikariDataSource( config );
  }

  private AppHikariDataSource() {}

  public static Connection getConnection() throws SQLException {
    return ds.getConnection();
  }
}