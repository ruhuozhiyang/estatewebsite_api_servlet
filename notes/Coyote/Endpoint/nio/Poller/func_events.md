然后，方法events()中用完 PollerEvent对象后，再将其放回对象池。

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