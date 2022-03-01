package web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import dao.UserDAO;
import entity.Message;
import entity.User;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserLoginServlet extends HttpServlet{

  public void doPost(HttpServletRequest request, HttpServletResponse response){

    try {
      String email = request.getParameter("email");
      String password = request.getParameter("password");

      response.setContentType("application/json; charset=UTF-8");
      response.setDateHeader("Expires", 0);
      response.setHeader("Cache-Control", "no-cache");
      response.setHeader("pragma", "no-cache");

      User user = new UserDAO().get(email);

      Message msg = new Message();
      msg.setInfo("操作成功");
      msg.setData(user);
      msg.setSuccess(true);

      String result = JSON.toJSONString(msg);

      response.getWriter().println(result);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}

