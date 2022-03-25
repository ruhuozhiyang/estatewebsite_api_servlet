主要是利用 NioSocketWrapper 创建了一个事件PollerEvent，并且将事件添加进内部维护的一个线程安全的队列 events ，在这个
过程中，势必是需要 PollerEvent 类对象的，但并没有直接就使用new 创建一个类对象，而是先试图去对象池里 pop 一个，如果没有
再new 创建一个对象，如果有，则调用reset 方法，重设对象的属性。

```java
public class NioEndpoint {
  /**
   * Cache for poller events
   */
  private SynchronizedStack<PollerEvent> eventCache;

  public class Poller {

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