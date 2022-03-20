package main.requesthandleimple;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import main.HttpServer;
import main.HttpStatus;
import main.RequestHandle;
import main.middle.RequestFacade;
import main.middle.ResponseFacade;
import main.middle.Socket2Uri;

public class DynamicResourceHandle implements RequestHandle {

  private static final URLClassLoader URL_CLASS_LOADER;

  static {
    try {
      URL servletClassPath = new File(HttpServer.SERVLETS_PATH).toURI().toURL();
      System.out.println(servletClassPath);
      URL_CLASS_LOADER = new URLClassLoader(new URL[]{servletClassPath});
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void process(ServletRequest sr1, ServletResponse sr2) {
    String servletName = this.parseServletName(Socket2Uri.Uri);
    Class servletClass = null;
    try {
      servletClass = URL_CLASS_LOADER.loadClass("main.servlets." + servletName);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    try {
      Servlet servlet = (Servlet) servletClass.newInstance();
      Method m = sr2.getClass().getMethod("responseToByte", HttpStatus.class);
      System.out.println(m);
      sr2.getWriter().println(new String((byte[]) m.invoke(sr2, HttpStatus.OK)));
      servlet.service(new RequestFacade(sr1), new ResponseFacade(sr2));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String parseServletName(String uri) {
    return uri.substring(uri.lastIndexOf("/") + 1);
  }
}
