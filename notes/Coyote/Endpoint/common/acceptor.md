## Overview
The Acceptor class implements the runnable interface and override the run function. 
The run function is important as it describes the process of handling a request from the client.

## Members
### function - run
实现接口 Runnable 的方法 run 源代码主体如下。
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
acceptor 的 run 方法调用了 AbstractEndpoint 中的一些方法，如上代码所见，调用了endpoint 的
countUpOrAwaitConnection(), isPaused(), serverSocketAccept(), setSocketOptions(socket),
closeSocket(socket) 方法。

**endpoint.serverSocketAccept() 得到的 socket 供后续 setSocketOptions(socket) 方法使用。**

关于 endpoint.serverSocketAccept() 和 setSocketOptions(socket) 的具体内容，不同的 Endpoint 实现类实现不同，
以 NioEndpoint 为例，可见[此处](../nio/nio_endpoint.md)。