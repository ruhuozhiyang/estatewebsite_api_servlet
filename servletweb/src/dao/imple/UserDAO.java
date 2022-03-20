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
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return total;
  }

  @Override
  public boolean add(T t) {
    String sql = "insert into user values(?,?,?,?)";
    PreparedStatement ps;
    try {
      ps = connection.prepareStatement(sql);
      ps.setString(1, getFieldOrSuperValue(t, "user", 'c'));
      ps.setString(2, getFieldOrSuperValue(t, "telNumber", 'c'));
      ps.setString(3, getFieldOrSuperValue(t, "email", 's'));
      ps.setString(4, getFieldOrSuperValue(t, "password", 's'));
      ps.execute();
      ps.close();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public boolean delete(T t) {
    String sql = "delete from user where user = ?";
    PreparedStatement ps;
    try {
      ps = connection.prepareStatement(sql);
      ps.setString(1, getFieldOrSuperValue(t, "user", 'c'));
      ps.execute();
      ps.close();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public boolean update(T t) {
    String sql = "update user set user= ?, email = ?, tel_number = ?, password = ?";
    PreparedStatement ps;
    try {
      ps = connection.prepareStatement(sql);
      ps.setString(1, getFieldOrSuperValue(t, "user", 'c'));
      ps.setString(2, getFieldOrSuperValue(t, "email", 's'));
      ps.setString(3, getFieldOrSuperValue(t, "telNumber", 'c'));
      ps.setString(4, getFieldOrSuperValue(t, "password", 's'));
      ps.execute();
      ps.close();
      return true;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public List<T> get(T t) {
    return null;
  }

  @Override
  public T isExist(T t) {
    User user = null;
    String email = getFieldOrSuperValue(t, "email", 's');
    String password = getFieldOrSuperValue(t, "password", 's');
    try (Statement s = connection.createStatement()) {
      String sql = "select * from user where email = " + "'" + email + "' and password = '" + password + "';";
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

  private String getFieldOrSuperValue(T t, String field, char f) {
    String res = null;
    Class<? extends Person> userClass = t.getClass();
    Field[] fields = f == 's' ? userClass.getSuperclass().getDeclaredFields() : userClass.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      fields[i].setAccessible(true);
      if (fields[i].getName().equals(field)) {
        try {
          res = (String) fields[i].get(t);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
      }
    }
    return res;
  }
}
