package dao.imple;

import dao.DAO;
import entity.Admin;
import entity.Person;
import entity.User;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class AdminDAO<T extends Person> implements DAO<T> {

  private Connection connection = null;
  public AdminDAO(Connection c) {
    if (connection == null) {
      connection = c;
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
    Admin admin = null;
    String email = getFieldOrSuperValue(t, "email", 's');
    String password = getFieldOrSuperValue(t, "password", 's');
    try (Statement s = connection.createStatement()) {
      String sql = "select * from user where email = " + "'" + email + "' and password = '" + password + "';";
      System.out.println(sql);
      ResultSet rs = s.executeQuery(sql);
      if (rs.next()) {
        admin = new Admin();
        admin.setUser(rs.getString("user"));
        admin.setEmail(rs.getString("email"));
        admin.setPassword(rs.getString("password"));
        admin.setTelNumber(rs.getString("tel_number"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return (T) admin;
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
