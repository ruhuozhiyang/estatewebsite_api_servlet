package main.requesthandleimple;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import main.RequestHandle;

public class StaticResourceHandle implements RequestHandle {
  @Override
  public void process(ServletRequest sr1, ServletResponse sr2) {
    try {
      Method m = sr2.getClass().getMethod("handleRequest");
      m.invoke(sr2);
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }

}
