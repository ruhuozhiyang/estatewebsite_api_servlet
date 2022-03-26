## Overview
The NioEndPoint class extends the abstract class AbstractEndPoint, and implements the abstract 
functions like startInternal, setSocketOptions and so on.

Tomcat 的 NioEndpoint 实现了 I/O 多路复用模型。对于 Java 的多路复用器的使用，无非是两步：
1. 创建一个 Selector，在它身上注册各种感兴趣的事件，然后调用 select 方法，等待感兴趣的事情发生。
2. 感兴趣的事情发生了，比如可以读了，这时便创建一个新的线程从 Channel 中读数据。

## Members
### function - initServerSocket
```java
class  NioEndpoint {
  /**
   * Initialize the endpoint.
   */
  @Override
  public void bind() throws Exception {
    initServerSocket();
    // ...
  }

  // Separated out to make it easier for folks that extend NioEndpoint to
  // implement custom [server]sockets
  protected void initServerSocket() throws Exception {
    if (getUseInheritedChannel()) {
      
    } else if (getUnixDomainSocketPath() != null) {

    } else {
      serverSock = ServerSocketChannel.open();
      socketProperties.setProperties(serverSock.socket());
      InetSocketAddress addr = new InetSocketAddress(getAddress(), getPortWithOffset());
      serverSock.bind(addr, getAcceptCount());
    }
    serverSock.configureBlocking(true); //mimic APR behavior
  }
}
```
最后一步将 serverSock: ServerSocketChannel 配置成了阻塞模式，并注释写道，模拟 APR 行为。

### function - startInternal
```java
class NioEndpoint {
  /**
   * Bytebuffer cache, each channel holds a set of buffers (two, except for SSL holds four)
   */
  private SynchronizedStack<NioChannel> nioChannels;
  /**
   * Start the NIO endpoint, creating acceptor, poller threads.
   */
  @Override
  public void startInternal() throws Exception {

    if (!running) {
      running = true;

      /**
       * ...
       * ...
       */

      /**
       * bufferPool属性：可以创建的channels的数量。
       * 如果socket的配置属性bufferPool不为0，那么就可以创建nioChannel的同步栈。
       * 栈中的每一个成员为channel-nioChannel
       */
      if (socketProperties.getBufferPool() != 0) {
        nioChannels = new SynchronizedStack<>(SynchronizedStack.DEFAULT_SIZE,
            socketProperties.getBufferPool());
      }

      // Create worker collection
      if (getExecutor() == null) {
        createExecutor();
      }

      initializeConnectionLatch();

      // Start poller thread
      poller = new Poller();
      Thread pollerThread = new Thread(poller, getName() + "-Poller");
      pollerThread.setPriority(threadPriority);
      pollerThread.setDaemon(true);
      pollerThread.start();

      startAcceptorThread();
    }
  }
}
```
### function - serverSocketAccept
NioEndpoint 的 serverSocketAccept 方法内容见如下部分源代码。
```java
public class NioEndPoint extends AbstractJsseEndpoint<NioChannel,SocketChannel> {
  /**
   * Server socket "pointer".
   */
  private volatile ServerSocketChannel serverSock = null;
  
  @Override
  protected SocketChannel serverSocketAccept() throws Exception {
    SocketChannel result = serverSock.accept();

    // Bug does not affect Windows. Skip the check on that platform.
    if (!JrePlatform.IS_WINDOWS) {
//      ...
    }

    return result;
  }
}
```
可见，NioEndpoint 返回的 socket 就是调用
[java.nio.channels.ServerSocketChannel](../common/sever_socket_channel.md) 的 accept 方法得到的。
请关注 NioEndpoint 中 initServerSocket() 对于 serverSock 的初始化。

### function - setSocketOptions
The function handles the specified connection and the param is the SocketChannel.
It firstly gets a NioChannel, through checking the pool of re-usable NioChannel Objects or creating
a new NioChannel Object if the stack pops none nio-channel.

setSocketOptions 方法的部分源代码如下所示。
```java
public class NioEndPoint extends AbstractJsseEndpoint<NioChannel,SocketChannel> {

  private Poller poller = null;

  /**
   * Bytebuffer cache, each channel holds a set of buffers (two, except for SSL holds four)
   */
  private SynchronizedStack<NioChannel> nioChannels;

  /**
   * Process the specified connection.
   * @param socket The socket channel
   * @return <code>true</code> if the socket was correctly configured
   *  and processing may continue, <code>false</code> if the socket needs to be
   *  close immediately
   */
  @Override
  protected boolean setSocketOptions(SocketChannel socket) {
    NioSocketWrapper socketWrapper = null;
    
    try {
      // Allocate channel and wrapper
      NioChannel channel = null;
      if (nioChannels != null) {
        channel = nioChannels.pop();
      }
      if (channel == null) {
        SocketBufferHandler bufhandler = new SocketBufferHandler(
            socketProperties.getAppReadBufSize(),
            socketProperties.getAppWriteBufSize(),
            socketProperties.getDirectBuffer());
        if (isSSLEnabled()) {
          channel = new SecureNioChannel(bufhandler, this);
        } else {
          channel = new NioChannel(bufhandler);
        }
      }
      NioSocketWrapper newWrapper = new NioSocketWrapper(channel, this);
      channel.reset(socket, newWrapper);
      connections.put(socket, newWrapper);
      socketWrapper = newWrapper;
      
      // Set socket properties
      // Disable blocking, polling will be used
      socket.configureBlocking(false);
      
      // ...
      poller.register(socketWrapper);
      return true;
    } catch (Throwable t) {
      
    }
  }
  
}
```
### object - SynchronizedStack<NioChannel>
SynchronizedStack<NioChannel> 是缓存池，缓存的对象为 NioChannel。
NioChannel 对 SocketChannel 封装，使SSL与非SSL对外提供相同的处理方式。
适用于数据量比较固定的场景，另外这个数据结构本身由数组维护，减少了维护节点的开销。

This is intended as a (mostly) GC-free alternative to java.util.Stack when the requirement is to
create a pool of re-usable objects with no requirement to shrink the pool. The aim is to provide the
bare minimum of required functionality as quickly as possible with minimum garbage.

补充：对象池(a pool of re-usable objects)

> **在某些时候，我们需要频繁使用一些临时对象，如果每次使用的时候都申请新的资源，很有可能会引发频繁的 gc 而影响应用的流畅性。
这个时候如果对象有明确的生命周期，那么就可以通过定义一个对象池来高效的完成复用对象。**
>
> 比如每个请求任务，都需要用到类。若每次都需要重新new这些类，并不是很合适。而且在大量请求时，频繁创建和销毁这些类，
可能会导致内存抖动，影响性能。这个时候对象池的使用就很有必要了。
>
> 对象池通过对其所保存对象的共享与重用，缩减了应用线程反复重建、装载对象的过程所需要的时间，并且也有效地避免了频繁垃圾回收
带来的巨大系统开销。