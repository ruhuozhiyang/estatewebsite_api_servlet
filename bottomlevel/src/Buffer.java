import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Java NIO 由以下几个核心部分组成：
 * Channels：通道
 * Buffers：缓冲区
 * Selectors：选择器
 *
 * Java NIO中的Buffer用于和NIO通道进行交互。
 * 数据是从通道读入缓冲区，从缓冲区写入到通道中的。
 * 缓冲区本质上是一块可以写入数据，然后可以从中读取数据的内存。
 * 这块内存被包装成NIO Buffer对象，并提供了一组方法，用来方便地访问这块内存。
 * 一旦Buffer满了，需要将其清空（通过读数据或者清除数据）才能继续写数据往里写数据。
 *
 * Java NIO中的一些主要Channel的实现：
 * FileChannel
 * DatagramChannel
 * SocketChannel
 * ServerSocketChannel
 *
 * Java NIO里关键的Buffer实现：
 * 1. ByteBuffer
 * 2. CharBuffer
 * 3. DoubleBuffer
 * 4. FloatBuffer
 * 5. IntBuffer
 * 6. LongBuffer
 * 7. ShortBuffer
 *
 * Buffer的三个重要属性为: capacity, limit, position。
 * position和limit的含义取决于Buffer处于读模式还是写模式。
 *
 * flip方法将Buffer从写模式切换到读模式。调用flip()方法会将position设回0，并将limit设置成之前position的值。
 *
 * Selector允许单线程处理多个 Channel。
 *
 */
public class Buffer {

  public static void main(String[] args) throws IOException {
    Buffer test = new Buffer();
//    test.writeTxt();
//    test.readTxt();
    test.readTxt2();
//    test.test_write_read(3);
  }

  private void test_write_read(int byte_length) {
    /**
     * 写数据到Buffer有两种方式：
     * 1. 从Channel写到Buffer。
     * 2. 通过Buffer的put()方法写到Buffer里。
     *
     * 同样，从Buffer中读取数据有两种方式：
     * 1. 从Buffer读取数据到Channel。
     * 2. 使用get()方法从Buffer中读取数据。
     */
    ByteBuffer buffer = ByteBuffer.allocate(byte_length); // 分配的缓冲区的大小，单位为字节。
    System.out.println("你".getBytes().length); // 3
    buffer.put("你".getBytes());
    buffer.flip();
    byte[] data = new byte[3];
    buffer.get(data);
    System.out.println(new String(data)); // "你"
    buffer.clear();

    buffer.put("nim".getBytes());
    buffer.flip();
    byte[] data1 = new byte[2];
    buffer.get(data1);
    System.out.println(new String(data1)); // "ni"
    buffer.compact();

    buffer.put("er".getBytes());
    buffer.flip();
    byte[] data2 = new byte[3];
    buffer.get(data2);
    System.out.println(new String(data2)); // "mer"
  }

  private void writeTxt() throws IOException {
    FileOutputStream fos=new FileOutputStream("/Users/foiunclekay/Desktop/example1.txt");
    FileChannel outChannel = fos.getChannel();
    ByteBuffer buffer=ByteBuffer.allocate(50);
    buffer.put("hello world".getBytes());
    buffer.flip();
    outChannel.write(buffer);
    outChannel.close();
  }

  private void readTxt() throws IOException {
    FileInputStream fis = new FileInputStream("/Users/foiunclekay/Desktop/example1.txt");
    FileChannel inChannel = fis.getChannel();
    ByteBuffer buffer = ByteBuffer.allocate(9);
    // len is the number of the bytes read from channel to the bytes buffer.
    // it is possible to be zero if the bytes buffer is full,
    // and -1 if the channel has reached the end of the stream.
    int len = inChannel.read(buffer);

    buffer.flip();
    String data=new String(buffer.array(),0,len);
    System.out.println(data);
    buffer.clear();
    inChannel.close();
  }

  private void readTxt2() throws IOException {
    FileInputStream fis = new FileInputStream("/Users/foiunclekay/Desktop/example1.txt");
    FileChannel inChannel = fis.getChannel();
    ByteBuffer buffer = ByteBuffer.allocate(9);

    int len = inChannel.read(buffer);

    while (len != -1) {
      buffer.flip();
      System.out.println(new String(buffer.array(),0, len));
      buffer.clear();
      len = inChannel.read(buffer);
    }
    inChannel.close();
  }
}
