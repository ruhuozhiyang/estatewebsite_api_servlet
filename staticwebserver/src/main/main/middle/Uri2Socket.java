package main.middle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import main.HttpServer;
import main.HttpStatus;

public class Uri2Socket implements ServletResponse {

  private final static String HTTP_VERSION = "HTTP/1.1";

  private OutputStream outputStream;

  public Uri2Socket(OutputStream outputStream) {
    System.out.println("+1");
    this.outputStream = outputStream;
  }

  public void handleRequest() throws IOException {
    File staticResource = new File(HttpServer.PAGES_PATH + Socket2Uri.Uri);
    if (staticResource.exists() && staticResource.isFile()) {
      outputStream.write(responseToByte(HttpStatus.OK));
    } else {
      // 这一步有点后端路由的味道，也就是没有让前端二次请求指定页面数据.
      staticResource = new File(HttpServer.PAGES_PATH + "/404.html");
      outputStream.write(responseToByte(HttpStatus.NOT_FOUND));
    }
    write(staticResource);
  }

  /**
   * 将指定资源文件写回给Socket.
   * @param file resource file.
   */
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

  /**
   * 写响应头内容.
   * @param status http status.
   * @return byte[].
   */
  public byte[] responseToByte(HttpStatus status) {
    return new StringBuilder().append(HTTP_VERSION).append(" ")
        .append(status.getStatus_code()).append(" ")
        .append(status.getDesc()).append("\r\n\r\n")
        .toString().getBytes();
  }


  @Override
  public String getCharacterEncoding() {
    return null;
  }

  @Override
  public String getContentType() {
    return null;
  }

  @Override
  public ServletOutputStream getOutputStream() throws IOException {
    return null;
  }

  @Override
  public PrintWriter getWriter() throws IOException {
    return new PrintWriter(outputStream, true);
  }

  @Override
  public void setCharacterEncoding(String s) {

  }

  @Override
  public void setContentLength(int i) {

  }

  @Override
  public void setContentLengthLong(long l) {

  }

  @Override
  public void setContentType(String s) {

  }

  @Override
  public void setBufferSize(int i) {

  }

  @Override
  public int getBufferSize() {
    return 0;
  }

  @Override
  public void flushBuffer() throws IOException {

  }

  @Override
  public void resetBuffer() {

  }

  @Override
  public boolean isCommitted() {
    return false;
  }

  @Override
  public void reset() {

  }

  @Override
  public void setLocale(Locale locale) {

  }

  @Override
  public Locale getLocale() {
    return null;
  }
}
