package dao.imple;

import dao.DAO;
import entity.Person;
import entity.User;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class UserDAO<T extends Person> implements DAO<T> {

  private Connection connection = null;
  public UserDAO(Connection c) {
    if (connection == null) {
      connection = c;
    }
  }

  public int getTotal() {
    int total = 0;
    try (Statement s = connection.createStatement();) {
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
    try (PreparedStatement ps = connection.prepareStatement(sql);) {

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
    try (PreparedStatement ps = connection.prepareStatement(sql);) {
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

    try (Statement s = connection.createStatement();) {
      String sql = "delete from user where user = " + user;
      s.execute(sql);
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  @Override
  public boolean add(T t) {
    return false;
  }

  @Override
  public boolean delete(T t) {
    return false;
  }

  @Override
  public boolean update(T t) {
    return false;
  }

  @Override
  public List<T> get(T t) {
    return null;
  }

  @Override
  public T isExist(T t) {
    User user = null;
    String email = null;
    String password = null;
    Class<? extends Person> userClass = t.getClass();
    Field[] fields = userClass.getSuperclass().getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      if (fields[i].getName().equals("email")) {
        try {
          fields[i].setAccessible(true);
          email = (String) fields[i].get(t);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
      if (fields[i].getName().equals("password")) {
        try {
          fields[i].setAccessible(true);
          password = (String) fields[i].get(t);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
    try (Statement s = connection.createStatement()) {
      String sql = "select * from user where email = " + "'" + email + "' and password = '" + password + "';";
      System.out.println(sql);
      ResultSet rs = s.executeQuery(sql);
      if (rs.next()) {
        user = new User();
        user.setUser(rs.getString("user"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setTelNumber(rs.getString("tel_number"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return (T) user;
  }
}
