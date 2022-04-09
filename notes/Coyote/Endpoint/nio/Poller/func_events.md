## Overview
该方法是用来处理队列 SynchronizedQueue<PollerEvent> 中的事件对象 PollerEvent 的，该方法的部分源码如下所示。
```java
public class Poller {
  /**
   * Processes events in the event queue of the Poller.
   *
   * @return <code>true</code> if some events were processed,
   *   <code>false</code> if queue was empty
   */
  public boolean events() {
    boolean result = false;

    PollerEvent pe = null;
    for (int i = 0, size = events.size(); i < size && (pe = events.poll()) != null; i++ ) {
      result = true;
      NioSocketWrapper socketWrapper = pe.getSocketWrapper();
      SocketChannel sc = socketWrapper.getSocket().getIOChannel();
      int interestOps = pe.getInterestOps();
      if (sc == null) {
        log.warn(sm.getString("endpoint.nio.nullSocketChannel"));
        socketWrapper.close();
      } else if (interestOps == OP_REGISTER) {
        try {
          sc.register(getSelector(), SelectionKey.OP_READ, socketWrapper);
        } catch (Exception x) {
          log.error(sm.getString("endpoint.nio.registerFail"), x);
        }
      } else {
        final SelectionKey key = sc.keyFor(getSelector());
        if (key == null) {
          // The key was cancelled (e.g. due to socket closure)
          // and removed from the selector while it was being
          // processed. Count down the connections at this point
          // since it won't have been counted down when the socket
          // closed.
          socketWrapper.close();
        } else {
          final NioSocketWrapper attachment = (NioSocketWrapper) key.attachment();
          if (attachment != null) {
            // We are registering the key to start with, reset the fairness counter.
            try {
              int ops = key.interestOps() | interestOps;
              attachment.interestOps(ops);
              key.interestOps(ops);
            } catch (CancelledKeyException ckx) {
              cancelledKey(key, socketWrapper);
            }
          } else {
            cancelledKey(key, socketWrapper);
          }
        }
      }
      if (running && eventCache != null) {
        pe.reset();
        eventCache.push(pe);
      }
    }
    return result;
  }
}
```

## Analysis
如果队列为空，就会返回 false；如果队列不为空，就会遍历队列，进行操作，并且返回 true。

在创建 PollerEvent 时候已设置其 interestOps 属性为 OP_REGISTER，表明 PollerEvent 的下一兴趣事件为注册事件，
也就是注册进 Selector。

遍历队列时，先依次获取 PollerEvent 对应的 NioSocketWrapper， SocketChannel 和 interestOps。

我们可以看见 if 判断代码如下。
```markdown
if (interestOps == OP_REGISTER) {
    try {
      sc.register(getSelector(), SelectionKey.OP_READ, socketWrapper);
    } catch (Exception x) {
      log.error(sm.getString("endpoint.nio.registerFail"), x);
    }
}
```
如果 PollerEvent 的 interestOps 为 OP_REGISTER，那么就通过 SocketChannel.register() 将 SocketChannel 注册
进 Selector，且监测的事件为 SelectionKey.OP_READ，并添加附件 socketWrapper，关于 SocketChannel.register() 
的用法见 [register](../../common/sever_socket_channel.md)。

那为什么会出现以下除了 interestOps == OP_REGISTER 的另外两种 if 可能情况呢？
```markdown
if (sc == null) {
    log.warn(sm.getString("endpoint.nio.nullSocketChannel"));
    socketWrapper.close();
} else if (interestOps == OP_REGISTER) {

} else {
    final SelectionKey key = sc.keyFor(getSelector());
    if (key == null) {
      // The key was cancelled (e.g. due to socket closure)
      // and removed from the selector while it was being
      // processed. Count down the connections at this point
      // since it won't have been counted down when the socket
      // closed.
      socketWrapper.close();
    } else {
      final NioSocketWrapper attachment = (NioSocketWrapper) key.attachment();
      if (attachment != null) {
        // We are registering the key to start with, reset the fairness counter.
        try {
          int ops = key.interestOps() | interestOps;
          attachment.interestOps(ops);
          key.interestOps(ops);
        } catch (CancelledKeyException ckx) {
          cancelledKey(key, socketWrapper);
        }
      } else {
        cancelledKey(key, socketWrapper);
      }
    }
}
```

最后在用完 PollerEvent 后，再将其放回对象池，对应代码如下。
```markdown
if (running && eventCache != null) {
    pe.reset();
    eventCache.push(pe);
}
```

其实，events() 方法的主体就是 for 循环遍历队列 SynchronizedQueue<PollerEvent>，处理其中 PollerEvent 对象，将
SocketChannel 注册进 Selector，处理完后，队列为空。