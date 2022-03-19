package main;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;

public class HttpServer {

  public static final String WEB_PROJECT_ROOT;

  static{
    URL webrootURL = HttpServer.class.getClassLoader().getResource("pages");
    WEB_PROJECT_ROOT = Optional.ofNullable(webrootURL)
        .orElseThrow(() -> new IllegalStateException("can't not find resource."))
        .getFile();
  }

  public static void main(String[] args) {
    new HttpServer().await();
  }

  private void await() {
    ServerSocket serverSocket;
    try {
      serverSocket = new ServerSocket(8080, 1, InetAddress.getByName("127.0.0.1"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    while (true) {
      try (Socket accept = serverSocket.accept();
          InputStream inputStream = accept.getInputStream();
          OutputStream outputStream = accept.getOutputStream()) {
        //解析用户的请求
        Socket2Uri request = new Socket2Uri();
        request.setInputStream(inputStream);
        request.parseSocket();

        Uri2Socket uri2Socket = new Uri2Socket(outputStream, request);
        uri2Socket.handleRequest();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    //关闭服务器
//    try {
//      serverSocket.close();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
  }
}
