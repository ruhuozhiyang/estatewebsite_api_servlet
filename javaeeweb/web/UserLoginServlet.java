package web;

import java.io.IOException;
import java.util.Date;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserLoginServlet extends HttpServlet{

  public void doPost(HttpServletRequest request, HttpServletResponse response){

    try {
      String name = request.getParameter("email");
      String password = request.getParameter("password");
      System.out.println("name:" + name);
      System.out.println("password:" + password);

      response.getWriter().println("<h1>Hello Servlet1!</h1>");
      response.getWriter().println(new Date());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}

