package dao;

import entity.Person;
import java.util.List;

/**
 * 抽象策略类.
 */
public interface DAO<T extends Person> {

  boolean add(T t);
  boolean delete(T t);
  boolean update(T t);
  List<T> get(T t);
  T isExist(T t);

}
