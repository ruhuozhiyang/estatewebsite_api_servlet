The Acceptor class implements the runnable interface and override the run function. The run function
is important as it describes the process of receiving a request from the client.
```java
class Acceptor implements Runnable {
  @Override
  public void run() {
    try {
      // Loop until we receive a shutdown command
      while (!stopCalled) {
        try {
          //if we have reached max connections, wait
          endpoint.countUpOrAwaitConnection();

          // Endpoint might have been paused while waiting for latch
          // If that is the case, don't accept new connections
          if (endpoint.isPaused()) {
            continue;
          }

          U socket = null;
          try {
            // Accept the next incoming connection from the server
            // socket
            socket = endpoint.serverSocketAccept();
          } catch (Exception ioe) {

          }
          // Successful accept, reset the error delay
          errorDelay = 0;

          // Configure the socket
          if (!stopCalled && !endpoint.isPaused()) {
            // setSocketOptions() will hand the socket off to
            // an appropriate processor if successful
            if (!endpoint.setSocketOptions(socket)) {
              endpoint.closeSocket(socket);
            }
          } else { }
        } catch (Throwable t) { }
      }
    } finally {}
  }
}
```
acceptor 的run方法中调用了 AbstractEndpoint 中一些重要的方法，如上代码所见，调用了endpoint 的
countUpOrAwaitConnection(), isPaused(), serverSocketAccept(), setSocketOptions(socket),
closeSocket(socket) 方法。

我们可以看见，此处调用endpoint.serverSocketAccept()获取的socket就是供后续setSocketOptions()方法使用的socket。

于是追踪 serverSocketAccept() 方法，此处以 NioEndpoint 的 serverSocketAccept 方法为例。
```java
public class NioEndPoint extends AbstractJsseEndpoint<NioChannel,SocketChannel> {
  @Override
  protected SocketChannel serverSocketAccept() throws Exception {
    SocketChannel result = serverSock.accept();

    // Bug does not affect Windows. Skip the check on that platform.
    if (!JrePlatform.IS_WINDOWS) {
//      ...
    }

    return result;
  }
}
```
我们发现，此处 NioEndpoint 返回的 socket 就是通过调用 java.nio.channels.ServerSocketChannel 的accept 方法
得到的。

在阻塞模式下，accept()方法会一直阻塞直到有新的连接到达。ServerSocketChannel 可以设置成非阻塞模式。在非阻塞模式下，
accept() 方法会立刻返回，如果还没有新进来的连接，返回的将是 null。因此，需要检查返回的 SocketChannel 是否是null。
关于 ServerSocketChannel 的介绍和使用可见[链接🔗](https://www.cnblogs.com/binarylei/p/9977580.html) 。