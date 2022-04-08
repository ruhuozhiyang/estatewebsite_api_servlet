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

## Analysis of the run function
关于 Java 中多路复用器的介绍见 [Selector](./model_selector.md)。

从代码中我们可以看到，方法 wakeupCounter.getAndSet(-1) 会将原子类 wakeupCounter 设置为 -1，并返回上一状态
的数值。

如果上一状态 > 0，说明队列中有 PollerEvent，那么就调用方法 selector.selectNow()，获取是否有注册在原始 socket 上
的事件发生，如果没有就会立即返回 0，不会阻塞。

如果上一状态 <= 0，说明队列中没有 PollerEvent，那么就调用方法 selector.select(timeout)，poller 线程会阻塞
进入等待状态，交出 CPU，持续 1000 毫秒。在此状态下时，wakeupCounter 为 -1，如果此时有 PollerEvent 注册进来，在
方法 register 中，会调用 addEvent() 方法，该方法会检测 wakeupCounter，如果为 -1，就调用 selector 的 
[wakeup()](./model_selector.md) 方法唤醒 poller 线程，跳出阻塞等待状态，继续向下执行。

**selector.select(timeout)，这个方法是阻塞方法，调用之后 poller 线程会一直处于等待状态，一直等待到有事件发生
或者超时。这样可以避免 cpu 空闲轮询【也就是一直去检查是否有事件发生，可是此时 PollerEvent 数量为 0，没必要】造成的过高
使用率(极端情况下会导致 java 进程占用 cpu 100% 的现象)。select(timeout) 表示会阻塞 poller 线程 timeout 毫秒，
然后进入下一个循环。**

也可以这么说，wakeupCounter = -1 是 selector 正处于阻塞状态的标识。

经过上面的操作后，原子类 wakeupCounter 又被设置为 0，继续向下执行本次循环的剩余代码。

再执行代码如下:
```markdown
// Either we timed out or we woke up, process events first

if (keyCount == 0) {
  hasEvents = (hasEvents | events());
}

java中|可以作为逻辑运算符或，它与||最大的不同在于，|两侧的布尔表达式，程序先执行第一个表达式，但是无论第一个表达式是否
为真，程序都会去执行第二个表达式。
```
这个操作就是为了保证 wakeup 唤醒 selector 后，此时事件队列中已经有 PollerEvent 了，那么就需要首先再处理一下新添的
事件 PollerEvent，更新 hasEvents 的状态。

接下去执行如下代码:
```markdown
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
```
如果 keyCount > 0，说明有事件准备好了，那么就通过 selector.selectedKeys() 获取所有的已就绪的键，然后遍历处理。
