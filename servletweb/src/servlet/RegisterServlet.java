package servlet;

import com.alibaba.fastjson.JSON;
import dao.Context;
import entity.Message;
import entity.User;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RegisterServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
    String email = req.getParameter("email");
    String password = req.getParameter("password");
    String user = req.getParameter("user");
    String telNumber = req.getParameter("telNumber");

    User user_entity = new User();
    user_entity.setPassword(password);
    user_entity.setUser(user);
    user_entity.setEmail(email);
    user_entity.setTelNumber(telNumber);

    Message msg = new Message();
    msg.setInfo("注册成功");
    msg.setData(new Context<>(user_entity).register());
    msg.setSuccess(true);

    setResponse(resp, JSON.toJSONString(msg));

  }

  private void setResponse(HttpServletResponse response, String result) {
    response.setContentType("application/json; charset=UTF-8");
    response.setDateHeader("Expires", 0);
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("pragma", "no-cache");
    try {
      response.getWriter().println(result);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
