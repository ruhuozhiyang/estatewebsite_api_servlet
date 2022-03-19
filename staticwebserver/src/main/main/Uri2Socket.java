package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public class Uri2Socket {

  private OutputStream outputStream;

  private Socket2Uri socket2Uri;

  public Uri2Socket(OutputStream outputStream, Socket2Uri socket2Uri) {
    this.outputStream = outputStream;
    this.socket2Uri = socket2Uri;
  }

  public void handleRequest() throws IOException {
    File staticResource = new File(HttpServer.WEB_PROJECT_ROOT + socket2Uri.getUri());
    System.out.println(HttpServer.WEB_PROJECT_ROOT + socket2Uri.getUri());
    System.out.println(staticResource.isFile());
    System.out.println(staticResource.exists());
    if (staticResource.exists() && staticResource.isFile()) {
      outputStream.write(responseToByte(200));
    } else {
      staticResource = new File(HttpServer.WEB_PROJECT_ROOT + "/404.html");
      outputStream.write(responseToByte(404));
    }
    write(staticResource);
  }

  private void write(File file) {
    try (FileInputStream fis = new FileInputStream(file)) {
      byte[] cache = new byte[1024];
      int read;
      while ((read = fis.read(cache, 0, 1024)) != -1) {
        outputStream.write(cache, 0, read);
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private byte[] responseToByte(int status) {
    return new StringBuilder().append("HTTP_1_1").append(" ")
        .append(status).append(" ")
        .append("ok").append("\r\n\r\n")
        .toString().getBytes();
  }
}
