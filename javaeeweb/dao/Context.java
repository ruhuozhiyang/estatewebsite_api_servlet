package dao;

import dao.imple.UserDAO;
import entity.Person;
import java.sql.Connection;
import java.sql.SQLException;

public class Context<T extends Person> {

  private DAO<T> dao;
  private Connection c = null;
  private T entity;

  public Context(T t) {
    entity = t;
    try {
      if (c != null) {
        c = null;
      }
      c = new JDBC().getConnection();
    } catch (SQLException e) {
      e.printStackTrace();
    }
    dao = new UserDAO<>(c);
  }

  public T login() {
    return dao.isExist(entity);
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    if (c != null) {
      c.close();
    }
  }
}
