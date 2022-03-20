package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JDBC {

  private final String url = "jdbc:mysql://127.0.0.1:3306/apartment?characterEncoding=UTF-8";
  private final String user = "root";
  private final String password = "***";
  private Connection c = null;

  public JDBC() {
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public Connection getConnection() throws SQLException {
    if (c == null) {
      c = DriverManager.getConnection(url, user, password);
    }
    return c;
  }
}
