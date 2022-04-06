## Overview
向轮询器注册新创建的套接字。主要是依据 final NioSocketWrapper 创建事件对象 PollerEvent，并且将事件对象添加进
内部维护的一个线程安全的队列 SynchronizedQueue<PollerEvent>。

方法 register() 的相关源代码如下所示。
```java
public class NioEndpoint {
  /**
   * Cache for poller events
   */
  private SynchronizedStack<PollerEvent> eventCache;

  public class Poller {

    private Selector selector;
    private final SynchronizedQueue<PollerEvent> events =
        new SynchronizedQueue<>();

    private void addEvent(PollerEvent event) {
      events.offer(event);
      if (wakeupCounter.incrementAndGet() == 0) {
        selector.wakeup();
      }
    }

    /**
     * Registers a newly created socket with the poller.
     *
     * @param socketWrapper The socket wrapper
     */
    public void register(final NioSocketWrapper socketWrapper) {
      socketWrapper.interestOps(SelectionKey.OP_READ);//this is what OP_REGISTER turns into.
      PollerEvent event = null;
      if (eventCache != null) {
        event = eventCache.pop();
      }
      if (event == null) {
        event = new PollerEvent(socketWrapper, OP_REGISTER);
      } else {
        event.reset(socketWrapper, OP_REGISTER);
      }
      addEvent(event);
    }
  }
}
```
## Analysis of Process
首先对原始 socket 注册 open_read 事件，然后创建 OP_REGISTER 类型的 poller 事件，交由方法 addEvent() 处理。

在创建 PollerEvent 对象时，并没有直接就使用new 创建一个类对象，而是先试图去对象池里 pop 一个，如果没有
再 new 创建一个对象，如果有，则调用reset 方法，重设对象的属性。

addEvent() 私有方法中，将 PollerEvent 对象添加进线程安全队列，并检测原子类 wakeupCounter 的值，如果加 1 后等于 0 
那么就通过 wakeup() 方法唤醒 Java 的多路复用器 Selector。
