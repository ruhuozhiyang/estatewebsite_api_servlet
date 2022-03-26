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
  
  @Override
  protected boolean setSocketOptions(SocketChannel socket) { }
  
}
```

Poller 本质上就是一个Selector。内部维护一个线程安全的Queue，SynchronizedQueue<PollerEvent>。

Poller 通过内部的 Selector 对象不断地向内核查询 Channel 的状态，一旦状态变成可读就生成任务类 SocketProcessor，
然后交给 Executor 去处理。

Poller 的另一个重要任务是循环遍历检查自己所管理的 SocketChannel 是否已经超时，如果有超时就关闭这个 SocketChannel。
```java
public class NioEndPoint extends AbstractJsseEndpoint<NioChannel,SocketChannel> {
  
  public class Poller implements Runnable {
    private Selector selector;
    private final SynchronizedQueue<PollerEvent> events = new SynchronizedQueue<>();

    public Poller() throws IOException {
      this.selector = Selector.open();
    }
    
    /**
     * Registers a newly created socket with the poller.
     *
     * @param socketWrapper The socket wrapper
     */
    public void register(final NioSocketWrapper socketWrapper) {}

    /**
     * Processes events in the event queue of the Poller.
     *
     * @return <code>true</code> if some events were processed,
     *   <code>false</code> if queue was empty
     */
    public boolean events() {}
    /**
     * The background thread that adds sockets to the Poller, checks the
     * poller for triggered events and hands the associated socket off to an
     * appropriate processor as events occur.
     */
    @Override
    public void run() {}
    
  }
}
```
#### Selector(选择器)
是Java NIO中的组件，它能够检测一到多个NIO通道，并能知道通道是否为事件做好准备（例如写事件）。
这样，一个单独的线程可以管理多个channel，从而管理多个网络连接。
为了将Channel和Selector配合使用，必须将Channel注册到Selector上，通过**SelectableChannel的register方法**。
与Selector一起使用时，Channel必须处于**非阻塞模式**下。这意味着FileChannel与Selector不能一起使用。

#### SelectionKey 又是什么？
就是Selector和Channel之间的桥梁。SelectableChannel的register方法所要用到的参数例如SelectionKey.OP_READ，
所属一个"interest集合"，意思是在通过Selector监听Channel时对什么事件感兴趣。可以监听四种不同类型的事件:
1.Connect; 2.Accept; 3.Read; 4.Write。通道触发了一个事件意思是该事件已经就绪。
这四种事件使用SelectionKey的四个常量来表示，为SelectionKey.OP_CONNECT、SelectionKey.OP_ACCEPT、
SelectionKey.OP_READ、SelectionKey.OP_WRITE。

#### The member of Poller: SynchronizedQueue<PollerEvent>
PollerEvent的toString打印输出为:
offer:Poller event: socket [org.apache.tomcat.util.net.NioChannel@267ebd99:
java.nio.channels.SocketChannel[connected local=/0:0:0:0:0:0:0:1:8080 remote=/0:0:0:0:0:0:0:1:56243]],
socketWrapper [org.apache.tomcat.util.net.NioEndpoint$NioSocketWrapper@46b243d9:
org.apache.tomcat.util.net.NioChannel@267ebd99:
java.nio.channels.SocketChannel[connected local=/0:0:0:0:0:0:0:1:8080 remote=/0:0:0:0:0:0:0:1:56243]],
interestOps [256]

#### Poller对象中的几个很重要方法：register() 、events() 和 run()。
启动阶段，NioEndpoint 使用 Poller 对象主要完成以下工作：
1. startInternal()中，创建后台 Poller 线程，并启动 Poller 线程工作（执行 run 方法）；
2. setSocketOptions()中，将接收到的 SocketChannel 封装成 NioSocketWrapper 并注册进 Poller 中；

Poller类中的 [register](./Coyote/Poller/func_register.md) 方法。

Poller类中的 [events](./Coyote/Poller/func_events.md) 方法。

Poller类的 [run](./Coyote/Poller/func_run.md) 方法。


层层封装: SocketChannel -> NioChannel -> NioSocketWrapper -> PollerEvent
