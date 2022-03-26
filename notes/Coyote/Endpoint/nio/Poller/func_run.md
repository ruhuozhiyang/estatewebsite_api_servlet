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

    /**
     * The background thread that adds sockets to the Poller, checks the
     * poller for triggered events and hands the associated socket off to an
     * appropriate processor as events occur.
     */