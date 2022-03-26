import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerSocketChannelTest {

  public static void main(String[] args) throws IOException {
    ServerSocketChannelTest ssct = new ServerSocketChannelTest();
//    ssct.noWhile();
//    ssct.havewhile();
    ssct.whileNoneBlock();
  }

  private void noWhile() throws IOException {
    ServerSocketChannel ssc = ServerSocketChannel.open();
    ssc.socket().bind(new InetSocketAddress(8080));

    SocketChannel accept = ssc.accept();
    System.out.println("get the accept socket:" + accept.socket());
  }

  private void havewhile() throws IOException {
    ServerSocketChannel ssc = ServerSocketChannel.open();
    ssc.socket().bind(new InetSocketAddress(8080));

    int count = 0;

    while (true) {
      SocketChannel accept = ssc.accept();
      count ++;
      System.out.println("get the accept socket" + count + ":" + accept.socket());
    }
  }

  private void whileNoneBlock() throws IOException {
    ServerSocketChannel ssc = ServerSocketChannel.open();
    ssc.socket().bind(new InetSocketAddress(8080));
    ssc.configureBlocking(false);

    int count = 0;

    while (true) {
      SocketChannel accept = ssc.accept();
      if (accept != null) {
        count ++;
        System.out.println("get the accept socket" + count + ":" + accept.socket());
        System.exit(0);
      } else {
        System.out.println("get no socket");
      }
    }
  }
}
