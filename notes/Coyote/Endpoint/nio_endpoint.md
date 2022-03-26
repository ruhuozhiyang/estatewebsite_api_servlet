The NioEndPoint class extends the abstract class AbstractEndPoint.
It implements the abstract function startInternal and setSocketOptions.

Tomcat的NioEndpoint 实现了I/O多路复用模型。对于 Java 的多路复用器的使用，无非是两步：
1. 创建一个 Seletor，在它身上注册各种感兴趣的事件，然后调用 select 方法，等待感兴趣的事情发生。
2. 感兴趣的事情发生了，比如可以读了，这时便创建一个新的线程从 Channel 中读数据。

The function setSocketOptions processes the specified connection and the param is the SocketChannel.
It firstly gets a NioChannel, through checking the pool of re-usable NioChannel Objects or creating
a new NioChannel Object if the stack pops none nio-channel.
```java
public class NioEndPoint extends AbstractJsseEndpoint<NioChannel,SocketChannel> {

  private Poller poller = null;

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
      // ...
      poller.register(socketWrapper);
      return true;
    } catch (Throwable t) {
      
    }
  }
  
}
```
#### 4.1.3 the member of Endpoint: SynchronizedStack<NioChannel>
类NioEndpoint中的成员变量SynchronizedStack<NioChannel>是NioChannel的对象池，NioChannel对SocketChannel封装，
使SSL与非SSL对外提供相同的处理方式。适用于数据量比较固定的场景，另外这个数据结构本身由数组维护，减少了维护节点的开销。

This is intended as a (mostly) GC-free alternative to java.util.Stack when the requirement is to
create a pool of re-usable objects with no requirement to shrink the pool. The aim is to provide the
bare minimum of required functionality as quickly as possible with minimum garbage.

补充：对象池(a pool of re-usable objects)

**在某些时候，我们需要频繁使用一些临时对象，如果每次使用的时候都申请新的资源，很有可能会引发频繁的 gc 而影响应用的流畅性。
这个时候如果对象有明确的生命周期，那么就可以通过定义一个对象池来高效的完成复用对象。**

比如每个请求任务，都需要用到类。若每次都需要重新new这些类，并不是很合适。而且在大量请求时，频繁创建和销毁这些类，
可能会导致内存抖动，影响性能。这个时候对象池的使用就很有必要了。

对象池通过对其所保存对象的共享与重用，缩减了应用线程反复重建、装载对象的过程所需要的时间，并且也有效地避免了频繁垃圾回收
带来的巨大系统开销。

#### 4.1.5 NioSocketWrapper
socketWrapper is the instance of NioSocketWrapper class, and is registered to the poller instance
by invoking the register function for executors handling further.