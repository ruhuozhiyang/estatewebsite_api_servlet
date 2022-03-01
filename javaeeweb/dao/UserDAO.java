package dao;

import entity.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

  private final String url = "jdbc:mysql://127.0.0.1:3306/apartment?characterEncoding=UTF-8";
  private final String user = "root";
  private final String password = "***";

  public UserDAO() {
    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(url, user, password);
  }

  public int getTotal() {
    int total = 0;
    try (Connection c = getConnection(); Statement s = c.createStatement();) {
      String sql = "select count(*) from user";
      ResultSet rs = s.executeQuery(sql);
      while (rs.next()) {
        total = rs.getInt(1);
      }
      System.out.println("total:" + total);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return total;
  }

  public void add(User user) {

    String sql = "insert into user values(?,?,?,?)";
    try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql);) {

      ps.setString(1, user.getUser());
      ps.setString(2, user.getTelNumber());
      ps.setString(3, user.getEmail());
      ps.setString(4, user.getPassword());
      ps.execute();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void update(User user) {

    String sql = "update user set user= ?, email = ?, telNumber = ?, password = ?";
    try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql);) {
      ps.setString(1, user.getUser());
      ps.setString(2, user.getEmail());
      ps.setString(3, user.getTelNumber());
      ps.setString(4, user.getPassword());
      ps.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public void delete(String user) {

    try (Connection c = getConnection(); Statement s = c.createStatement();) {
      String sql = "delete from user where user = " + user;
      s.execute(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public User get(String email) {
    User user1 = null;

    try (Connection c = getConnection(); Statement s = c.createStatement();) {
      String sql = "select * from user where email = " + "'" + email + "';";
      System.out.println(sql);
      ResultSet rs = s.executeQuery(sql);
      if (rs.next()) {
        user1 = new User();
        user1.setUser(rs.getString("user"));
        user1.setEmail(rs.getString("email"));
        user1.setPassword(rs.getString("password"));
        user1.setTelNumber(rs.getString("tel_number"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return user1;

  }

  public List<User> list() {
    return list(0, Short.MAX_VALUE);
  }

  public List<User> list(int start, int count) {

    List<User> users = new ArrayList<User>();
    String sql = "select * from user order by user desc limit ?,? ";
    try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql);) {
      ps.setInt(1, start);
      ps.setInt(2, count);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        User user1 = new User();
        user1.setUser(rs.getString("user"));
        user1.setEmail(rs.getString("email"));
        user1.setPassword(rs.getString("password"));
        user1.setTelNumber(rs.getString("tel_number"));
        users.add(user1);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return users;
  }

}
