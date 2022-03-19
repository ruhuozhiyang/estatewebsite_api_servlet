package main;

import java.io.IOException;
import java.io.InputStream;

public class Socket2Uri {

  private InputStream inputStream;

  private String Uri;

  public String getUri() {
    return Uri;
  }

  public void setUri(String uri) {
    int t = uri.indexOf(" ");
    Uri = uri.substring(t + 1, uri.indexOf(" ", t + 1));
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * socket inputStream -> String.
   * @throws IOException
   */
  public void parseSocket() throws IOException {
    StringBuilder stringBuilder = new StringBuilder();
    byte[] buffer = new byte[1024];
    inputStream.read(buffer);
    for (int j = 0; j < buffer.length; j++) {
      stringBuilder.append((char) buffer[j]);
    }
    this.setUri(stringBuilder.toString());
  }

}
