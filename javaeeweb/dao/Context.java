package dao;

import dao.imple.AdminDAO;
import dao.imple.UserDAO;
import entity.Person;
import java.sql.Connection;
import java.sql.SQLException;

public class Context<T extends Person> {

  private DAO<T> dao;
  private Connection c = null;
  private T entity;

  private static final String USER_CLASS = "entity.User";
  private static final String ADMIN_CLASS = "entity.Admin";

  public Context(T t) {
    entity = t;
    try {
      if (c == null) {
        c = new JDBC().getConnection();
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    if (t.getClass().getName().equals(USER_CLASS)) {
      dao = new UserDAO<>(c);
    } else if (t.getClass().getName().equals(ADMIN_CLASS)) {
      dao = new AdminDAO<>(c);
    }
  }

  public T login() {
    return dao.isExist(entity);
  }

  public boolean register() {
    return dao.add(entity);
  }

}
