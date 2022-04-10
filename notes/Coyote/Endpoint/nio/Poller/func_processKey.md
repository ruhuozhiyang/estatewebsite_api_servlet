## 1. Overview
部分源代码如下所示。
```java
class Poller {
  protected void processKey(SelectionKey sk, NioSocketWrapper socketWrapper) {
    try {
      if (close) {
        cancelledKey(sk, socketWrapper);
      } else if (sk.isValid()) {
        if (sk.isReadable() || sk.isWritable()) {
          if (socketWrapper.getSendfileData() != null) {
            processSendfile(sk, socketWrapper, false);
          } else {
            unreg(sk, socketWrapper, sk.readyOps());
            boolean closeSocket = false;
            // Read goes before write
            if (sk.isReadable()) {
              if (socketWrapper.readOperation != null) {
                if (!socketWrapper.readOperation.process()) {
                  closeSocket = true;
                }
              } else if (socketWrapper.readBlocking) {
                synchronized (socketWrapper.readLock) {
                  socketWrapper.readBlocking = false;
                  socketWrapper.readLock.notify();
                }
              } else if (!processSocket(socketWrapper, SocketEvent.OPEN_READ, true)) {
                closeSocket = true;
              }
            }
            if (!closeSocket && sk.isWritable()) {
              if (socketWrapper.writeOperation != null) {
                if (!socketWrapper.writeOperation.process()) {
                  closeSocket = true;
                }
              } else if (socketWrapper.writeBlocking) {
                synchronized (socketWrapper.writeLock) {
                  socketWrapper.writeBlocking = false;
                  socketWrapper.writeLock.notify();
                }
              } else if (!processSocket(socketWrapper, SocketEvent.OPEN_WRITE, true)) {
                closeSocket = true;
              }
            }
            if (closeSocket) {
              cancelledKey(sk, socketWrapper);
            }
          }
        }
      } else {
        // Invalid key
        cancelledKey(sk, socketWrapper);
      }
    } catch (CancelledKeyException ckx) {
      cancelledKey(sk, socketWrapper);
    } catch (Throwable t) {
      ExceptionUtils.handleThrowable(t);
      log.error(sm.getString("endpoint.nio.keyProcessingError"), t);
    }
  }
}
```
## 2. Analysis
判断 SelectionKey 是否有效，如果有效就进一步判断，如果可读 or 可写，就继续处理。

如果 sendFile != null，那么就处理它，否则，调用方法 unreg() unregister 掉 SocketChannel 的 interest set，也
就是不再监测 SocketChannel 就绪的事件，因为接着就要处理 SocketChannel 中的数据了。

我们主要看处理读 或 处理写的步骤，见 [processSocket](./func_processSocket.md)。