package dao.imple;

import dao.DAO;
import entity.Person;
import java.util.List;

public class AdminDAO<T extends Person> implements DAO<T> {

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
    return null;
  }
}
