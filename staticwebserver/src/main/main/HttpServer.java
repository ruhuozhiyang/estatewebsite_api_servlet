package main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;
import main.middle.Socket2Uri;
import main.middle.Uri2Socket;
import main.requesthandleimple.DynamicResourceHandle;
import main.requesthandleimple.StaticResourceHandle;

public class HttpServer {

  public static final String PAGES_PATH;
  public static final String SERVLETS_PATH;

  private RequestHandle rh;

  static{
    // pages文件夹放在resources目录下，resources目录是不用编译的。
    URL pagesURL = HttpServer.class.getClassLoader().getResource("pages");
    PAGES_PATH = Optional.ofNullable(pagesURL)
        .orElseThrow(() -> new IllegalStateException("can't not find resource."))
        .getFile();

    URL servletURL = HttpServer.class.getClassLoader().getResource("");
    SERVLETS_PATH = Optional.ofNullable(servletURL)
        .orElseThrow(() -> new IllegalStateException("can't not find servlets content."))
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

    /**
     * the body of the while expression has several creating objects operation,
     * which will consume much more memory space with the client emitting more socket requests.
     */
    while (true) {
      try (Socket accept = serverSocket.accept();
          InputStream inputStream = accept.getInputStream();
          OutputStream outputStream = accept.getOutputStream()) {
        //解析用户的请求
        Socket2Uri request = new Socket2Uri(inputStream);
        Uri2Socket response = new Uri2Socket(outputStream);

        if (Optional.ofNullable(Socket2Uri.Uri).orElse("").startsWith("/servlet/")) {
          rh = new DynamicResourceHandle();
        } else {
          rh = new StaticResourceHandle();
        }
        rh.process(request, response);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
