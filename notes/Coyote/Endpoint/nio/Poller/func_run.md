## Overview
将套接字添加到轮询器的后台线程检查轮询器中的触发事件，并在事件发生时将关联的套接字交给适当的处理器。该方法的部分源代码
如下图所示。
```java
public class Poller {
  /**
   * The background thread that adds sockets to the Poller, checks the
   * poller for triggered events and hands the associated socket off to an
   * appropriate processor as events occur.
   */
  @Override
  public void run() {
    // Loop until destroy() is called
    while (true) {
      boolean hasEvents = false;
      try {
        if (!close) {
          hasEvents = events();
          if (wakeupCounter.getAndSet(-1) > 0) {
            // If we are here, means we have other stuff to do
            // Do a non blocking select
            keyCount = selector.selectNow();
          } else {
            keyCount = selector.select(selectorTimeout);
          }
          wakeupCounter.set(0);
        }
        if (close) {
          events();
          timeout(0, false);
          try {
            selector.close();
          } catch (IOException ioe) {
            log.error(sm.getString("endpoint.nio.selectorCloseFail"), ioe);
          }
          break;
        }
        // Either we timed out or we woke up, process events first
        if (keyCount == 0) {
          hasEvents = (hasEvents | events());
        }
      } catch (Throwable x) {
        ExceptionUtils.handleThrowable(x);
        log.error(sm.getString("endpoint.nio.selectorLoopError"), x);
        continue;
      }
      Iterator<SelectionKey> iterator =
          keyCount > 0 ? selector.selectedKeys().iterator() : null;
      // Walk through the collection of ready keys and dispatch
      // any active event.
      while (iterator != null && iterator.hasNext()) {
        SelectionKey sk = iterator.next();
        iterator.remove();
        NioSocketWrapper socketWrapper = (NioSocketWrapper) sk.attachment();
        // Attachment may be null if another thread has called
        // cancelledKey()
        if (socketWrapper != null) {
          processKey(sk, socketWrapper);
        }
      }
      // Process timeouts
      timeout(keyCount,hasEvents);
    }
    getStopLatch().countDown();
  }
}
```

## Functions Used
selector.selectNow() 方法来获取是否有注册在原始 socket 上的事件发生。

selector.select(timeout) 方法，这个方法是阻塞方法，调用之后 poller 线程会一直处于等待状态，一直等待到有事件发生
或者超时。这样可以避免 cpu 空闲轮询造成使用率过高(极端情况下会导致 java 进程占用 cpu 100% 的现象)。select(timeout) 
表示会阻塞 poller 线程 timeout 毫秒，然后进入下一个循环。

从代码中我们可以看到，方法 wakeupCounter.getAndSet(-1) 会将原子类 wakeupCounter 设置为 -1，并返回上一状态
的数值。如果上一状态 <= 0，说明队列中没有 PollerEvent，那么就调用方法 selector.select(timeout)，poller 线程
进入等待状态，交出 CPU，持续 1000 毫秒。在此状态下时，wakeupCounter 为 -1，如果此时有 PollerEvent 注册进来，在
方法 register 中，会调用 addEvent() 方法，该方法会检测 wakeupCounter，如果为 -1，就调用 selector 的 wakeup()
方法唤醒 poller 线程。
